package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.handlers;

import edu.stanford.protege.webprotege.change.ProjectChange;
import edu.stanford.protege.webprotege.common.*;
import edu.stanford.protege.webprotege.diff.DiffElement;
import edu.stanford.protege.webprotege.revision.RevisionNumber;
import edu.stanford.protege.webprotegeeventshistory.*;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.dto.ProjectChangeForEntity;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.events.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import({WebprotegeEventsHistoryApplication.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@ExtendWith({SpringExtension.class, RabbitTestExtension.class, MongoTestExtension.class})
@ActiveProfiles("test")
public class NewLinearizationRevisionsEventHandlerIntegrationTest {

    @Autowired
    private NewLinearizationRevisionsEventHandler handler;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        mongoTemplate.dropCollection(RevisionsEvent.class);
    }

    @Test
    public void GIVEN_validNewLinearizationRevisionsEvent_WHEN_handleEventCalled_THEN_eventIsRegisteredInDatabase() {
        ProjectId projectId = ProjectId.generate();
        Set<ProjectChangeForEntity> changes = new LinkedHashSet<>();
        Page<DiffElement<String, String>> emptyPage = Page.emptyPage();
        ProjectChange projectChange1 = ProjectChange.get(RevisionNumber.getRevisionNumber(1), UserId.valueOf("user1"), 12345L, "Description1", 0, emptyPage);
        ProjectChange projectChange2 = ProjectChange.get(RevisionNumber.getRevisionNumber(2), UserId.valueOf("user2"), 12346L, "Description2", 0, emptyPage);

        changes.add(ProjectChangeForEntity.create("whoficEntityIri1", projectChange1));
        changes.add(ProjectChangeForEntity.create("whoficEntityIri2", projectChange2));

        NewLinearizationRevisionsEvent newLinRevEvent = NewLinearizationRevisionsEvent.create(EventId.generate(), projectId, changes);

        handler.handleEvent(newLinRevEvent);

        List<RevisionsEvent> savedEvents = mongoTemplate.findAll(RevisionsEvent.class);
        assertEquals(2, savedEvents.size());

        RevisionsEvent firstEvent = savedEvents.get(0);
        assertEquals("whoficEntityIri1", firstEvent.whoficEntityIri());
        assertEquals(projectId, firstEvent.projectId());
        assertEquals(12345L, firstEvent.timestamp());

        RevisionsEvent secondEvent = savedEvents.get(1);
        assertEquals("whoficEntityIri2", secondEvent.whoficEntityIri());
        assertEquals(projectId, secondEvent.projectId());
        assertEquals(12346L, secondEvent.timestamp());
    }

    @Test
    public void GIVEN_emptyChanges_WHEN_handleEventCalled_THEN_noEventsAreSavedToDatabase() {
        ProjectId projectId = ProjectId.generate();
        Set<ProjectChangeForEntity> emptyChanges = new LinkedHashSet<>();

        NewLinearizationRevisionsEvent emptyEvent = NewLinearizationRevisionsEvent.create(EventId.generate(), projectId, emptyChanges);

        handler.handleEvent(emptyEvent);

        List<RevisionsEvent> savedEvents = mongoTemplate.findAll(RevisionsEvent.class);
        assertTrue(savedEvents.isEmpty());
    }

    @Test
    public void GIVEN_nullEvent_WHEN_handleEventCalled_THEN_throwException() {
        NewLinearizationRevisionsEvent nullEvent = null;

        assertThrows(NullPointerException.class, () -> handler.handleEvent(nullEvent));
    }

    @Test
    public void GIVEN_multipleValidEvents_WHEN_handleEventCalled_THEN_allEventsAreRegisteredInDatabase() {
        ProjectId projectId1 = ProjectId.generate();
        ProjectId projectId2 = ProjectId.generate();
        Page<DiffElement<String, String>> emptyPage = Page.emptyPage();

        Set<ProjectChangeForEntity> changesForFirstEvent = new LinkedHashSet<>();
        Set<ProjectChangeForEntity> changesForSecondEvent = new LinkedHashSet<>();

        ProjectChange projectChange1 = ProjectChange.get(RevisionNumber.getRevisionNumber(1), UserId.valueOf("user1"), 12345L, "Description1", 0, emptyPage);
        ProjectChange projectChange2 = ProjectChange.get(RevisionNumber.getRevisionNumber(2), UserId.valueOf("user2"), 12346L, "Description2", 0, emptyPage);
        ProjectChange projectChange3 = ProjectChange.get(RevisionNumber.getRevisionNumber(3), UserId.valueOf("user3"), 12347L, "Description3", 0, emptyPage);

        changesForFirstEvent.add(ProjectChangeForEntity.create("whoficEntityIri1", projectChange1));
        changesForFirstEvent.add(ProjectChangeForEntity.create("whoficEntityIri2", projectChange2));

        changesForSecondEvent.add(ProjectChangeForEntity.create("whoficEntityIri3", projectChange3));

        NewLinearizationRevisionsEvent firstEvent = NewLinearizationRevisionsEvent.create(EventId.generate(), projectId1, changesForFirstEvent);
        NewLinearizationRevisionsEvent secondEvent = NewLinearizationRevisionsEvent.create(EventId.generate(), projectId2, changesForSecondEvent);

        handler.handleEvent(firstEvent);
        handler.handleEvent(secondEvent);

        List<RevisionsEvent> savedEvents = mongoTemplate.findAll(RevisionsEvent.class);
        assertEquals(3, savedEvents.size());

        RevisionsEvent firstSavedEvent = savedEvents.get(0);
        assertEquals("whoficEntityIri1", firstSavedEvent.whoficEntityIri());
        assertEquals(projectId1, firstSavedEvent.projectId());

        RevisionsEvent thirdSavedEvent = savedEvents.get(2);
        assertEquals("whoficEntityIri3", thirdSavedEvent.whoficEntityIri());
        assertEquals(projectId2, thirdSavedEvent.projectId());
    }
}
