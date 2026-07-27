package edu.stanford.protege.webprotegeeventshistory.sequence;

import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotegeeventshistory.HighLevelBusinessEvent;
import edu.stanford.protege.webprotegeeventshistory.IntegrationTest;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ProjectSequenceServiceIntegrationTest extends IntegrationTest {

    @Autowired
    private ProjectSequenceService projectSequenceService;

    @Autowired
    private ProjectSequenceSeedMigration seedMigration;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Before
    public void cleanCollections() {
        mongoTemplate.dropCollection(ProjectEventSequence.class);
        mongoTemplate.dropCollection(HighLevelBusinessEvent.class);
    }

    @Test
    public void GIVEN_twoProjects_WHEN_incrementedInterleaved_THEN_eachGetsItsOwnDenseSequence() {
        String projectA = ProjectId.generate().id();
        String projectB = ProjectId.generate().id();

        assertEquals(1, projectSequenceService.next(projectA));
        assertEquals(1, projectSequenceService.next(projectB));
        assertEquals(2, projectSequenceService.next(projectA));
        assertEquals(2, projectSequenceService.next(projectB));
        assertEquals(3, projectSequenceService.next(projectA));
        assertEquals(3, projectSequenceService.next(projectB));
    }

    @Test
    public void GIVEN_concurrentIncrements_WHEN_next_THEN_valuesAreDistinctAndConsecutive() throws InterruptedException {
        String projectId = ProjectId.generate().id();
        int threads = 8;
        int incrementsPerThread = 250;
        int total = threads * incrementsPerThread;

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneGate = new CountDownLatch(threads);
        ConcurrentLinkedQueue<Integer> results = new ConcurrentLinkedQueue<>();
        AtomicInteger failures = new AtomicInteger();

        for (int t = 0; t < threads; t++) {
            pool.submit(() -> {
                try {
                    startGate.await();
                    for (int i = 0; i < incrementsPerThread; i++) {
                        results.add(projectSequenceService.next(projectId));
                    }
                } catch (Exception e) {
                    failures.incrementAndGet();
                } finally {
                    doneGate.countDown();
                }
            });
        }

        startGate.countDown();
        assertTrue("increment tasks did not finish in time", doneGate.await(60, TimeUnit.SECONDS));
        pool.shutdownNow();

        assertEquals(0, failures.get());
        assertEquals(total, results.size());
        // Distinct and dense: exactly the set 1..total, no duplicates, no gaps.
        var distinct = results.stream().collect(Collectors.toSet());
        assertEquals(total, distinct.size());
        var expected = IntStream.rangeClosed(1, total).boxed().collect(Collectors.toSet());
        assertEquals(expected, distinct);
        assertEquals(total, projectSequenceService.getCurrentSequence(projectId));
    }

    @Test
    public void GIVEN_noCounter_WHEN_getCurrentSequence_THEN_returnsZero() {
        assertEquals(0, projectSequenceService.getCurrentSequence(ProjectId.generate().id()));
    }

    @Test
    public void GIVEN_counterAdvanced_WHEN_getCurrentSequence_THEN_returnsHeadWithoutAdvancing() {
        String projectId = ProjectId.generate().id();
        projectSequenceService.next(projectId);
        projectSequenceService.next(projectId);

        assertEquals(2, projectSequenceService.getCurrentSequence(projectId));
        // Reading must not advance the counter.
        assertEquals(2, projectSequenceService.getCurrentSequence(projectId));
        assertEquals(3, projectSequenceService.next(projectId));
    }

    @Test
    public void GIVEN_archivedEvents_WHEN_seed_THEN_counterEqualsPerProjectMaxAndIsIdempotent() {
        String projectA = ProjectId.generate().id();
        String projectB = ProjectId.generate().id();
        saveArchivedEvent(projectA, 3);
        saveArchivedEvent(projectA, 7);
        saveArchivedEvent(projectA, 5);
        saveArchivedEvent(projectB, 2);
        saveArchivedEvent(projectB, 4);

        seedMigration.seed();

        assertEquals(7, projectSequenceService.getCurrentSequence(projectA));
        assertEquals(4, projectSequenceService.getCurrentSequence(projectB));

        // New numbers stay strictly above every archived bookmark.
        assertEquals(8, projectSequenceService.next(projectA));
        assertEquals(5, projectSequenceService.next(projectB));
    }

    @Test
    public void GIVEN_alreadySeeded_WHEN_seedRunsAgain_THEN_existingCounterIsNotOverwritten() {
        String projectId = ProjectId.generate().id();
        saveArchivedEvent(projectId, 5);

        seedMigration.seed();
        // Simulate live traffic advancing the counter past the archived max.
        assertEquals(6, projectSequenceService.next(projectId));
        assertEquals(7, projectSequenceService.next(projectId));

        // A second seed (rolling restart) must not reset the advanced counter back to the max.
        seedMigration.seed();

        assertEquals(7, projectSequenceService.getCurrentSequence(projectId));
        assertEquals(8, projectSequenceService.next(projectId));
    }

    private void saveArchivedEvent(String projectId, int timeStamp) {
        var event = new HighLevelBusinessEvent(UUID.randomUUID().toString(), projectId, timeStamp, new Document());
        mongoTemplate.save(event);
    }
}
