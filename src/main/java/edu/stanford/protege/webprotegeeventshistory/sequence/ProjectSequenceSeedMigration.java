package edu.stanford.protege.webprotegeeventshistory.sequence;

import edu.stanford.protege.webprotegeeventshistory.HighLevelBusinessEvent;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

/**
 * Seeds the per-project {@link ProjectEventSequence} counters from the events already archived
 * in {@code HighLevelEvents}. For each project it sets the counter to {@code max(timeStamp)} of
 * that project's archived events so newly minted ordinals stay strictly above every bookmark a
 * connected client may still hold.
 * <p>
 * The seed is idempotent: it uses {@code $setOnInsert}, so it only writes a counter that does not
 * exist yet and never overwrites one that has already advanced. That makes it safe to run on
 * every (possibly rolling) restart. It runs once the application is ready, before the counter is
 * consulted on the live traffic path.
 */
@Component
public class ProjectSequenceSeedMigration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectSequenceSeedMigration.class);

    private static final String GROUP_ID = "_id";

    private static final String MAX_SEQ = "maxSeq";

    private final MongoTemplate mongoTemplate;

    public ProjectSequenceSeedMigration(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        seed();
    }

    /**
     * Inserts a counter set to the per-project archived maximum for every project that does not
     * already have one. Existing counters are left untouched.
     */
    public void seed() {
        var aggregation = Aggregation.newAggregation(
                Aggregation.group("projectId").max("timeStamp").as(MAX_SEQ));
        AggregationResults<Document> results =
                mongoTemplate.aggregate(aggregation, HighLevelBusinessEvent.class, Document.class);

        int seeded = 0;
        for (Document row : results) {
            String projectId = row.getString(GROUP_ID);
            if (projectId == null) {
                continue;
            }
            Number maxSeq = (Number) row.get(MAX_SEQ);
            int seedValue = maxSeq == null ? 0 : maxSeq.intValue();
            mongoTemplate.upsert(
                    new Query(Criteria.where("_id").is(projectId)),
                    new Update().setOnInsert(ProjectEventSequence.SEQ_FIELD, seedValue),
                    ProjectEventSequence.class);
            seeded++;
        }
        LOGGER.info("Project sequence seed migration processed {} project(s)", seeded);
    }
}
