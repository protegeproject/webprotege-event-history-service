package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.handlers;

import edu.stanford.protege.webprotege.common.ChangeRequestId;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotegeeventshistory.*;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.dto.*;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.events.RevisionsEvent;
import org.bson.Document;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.semanticweb.owlapi.model.EntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith({SpringExtension.class, RabbitTestExtension.class, MongoTestExtension.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@Import({WebprotegeEventsHistoryApplication.class})
@ActiveProfiles("test")
public class GetChangedEntitiesCommandHandlerIT {

    @Autowired
    private GetChangedEntitiesCommandHandler commandHandler;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    public void setUp() {
        mongoTemplate.dropCollection(RevisionsEvent.class);
    }

    @Test
    public void GIVEN_eventsAfterTimestamp_WHEN_handleRequestCalled_THEN_returnChangedEntities() {
        ProjectId projectId = ProjectId.generate();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis() - 10000);

        insertMockRevisionsEvent(projectId, "entity1", timestamp.getTime() - 5000, ChangeType.CREATE_ENTITY, EntityType.CLASS);
        insertMockRevisionsEvent(projectId, "entity2", timestamp.getTime() + 5000, ChangeType.UPDATE_ENTITY, EntityType.CLASS);
        insertMockRevisionsEvent(projectId, "entity3", timestamp.getTime() + 6000, ChangeType.DELETE_ENTITY, EntityType.CLASS);
        insertMockRevisionsEvent(projectId, "entity4", timestamp.getTime() + 6000, ChangeType.CREATE_ENTITY, EntityType.ANNOTATION_PROPERTY);
        insertMockRevisionsEvent(projectId, "entity5", timestamp.getTime() + 6000, ChangeType.DELETE_ENTITY, EntityType.DATATYPE);

        GetChangedEntitiesRequest request = GetChangedEntitiesRequest.create(projectId, timestamp.getTime());

        Mono<GetChangedEntitiesResponse> responseMono = commandHandler.handleRequest(request, null);
        GetChangedEntitiesResponse response = responseMono.block();

        assertNotNull(response);
        ChangedEntities changedEntities = response.changedEntities();
        assertEquals(0, changedEntities.createdEntities().size());
        assertEquals(1, changedEntities.updatedEntities().size());
        assertEquals(1, changedEntities.deletedEntities().size());

        assertEquals("entity2", changedEntities.updatedEntities().get(0));
        assertEquals("entity3", changedEntities.deletedEntities().get(0));
    }

    private void insertMockRevisionsEvent(ProjectId projectId, String entityIri, long timestamp, ChangeType changeType, EntityType entityType) {
        RevisionsEvent revisionsEvent = RevisionsEvent.create(
                projectId,
                entityIri,
                changeType,
                entityType,
                timestamp,
                new Document(),
                ChangeRequestId.generate());
        mongoTemplate.save(revisionsEvent);
    }
}
