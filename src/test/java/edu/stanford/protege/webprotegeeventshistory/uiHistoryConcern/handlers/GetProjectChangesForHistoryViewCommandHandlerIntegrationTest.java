package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.protege.webprotege.change.ProjectChange;
import edu.stanford.protege.webprotege.common.*;
import edu.stanford.protege.webprotege.revision.RevisionNumber;
import edu.stanford.protege.webprotegeeventshistory.*;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.events.RevisionsEvent;
import org.bson.Document;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.semanticweb.owlapi.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith({SpringExtension.class, RabbitTestExtension.class, MongoTestExtension.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@Import({WebprotegeEventsHistoryApplication.class})
public class GetProjectChangesForHistoryViewCommandHandlerIntegrationTest {

    @Autowired
    private GetProjectChangesForHistoryViewCommandHandler commandHandler;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        mongoTemplate.dropCollection(RevisionsEvent.class);
    }

    @Test
    public void GIVEN_validRequestWithSubject_WHEN_handleRequestCalled_THEN_returnCorrectProjectChanges() {
        // GIVEN
        ProjectId projectId = ProjectId.generate();
        OWLEntity subject = mockOWLEntity("http://example.com/Entity1");
        PageRequest pageRequest = PageRequest.requestPageWithSize(1, 10);
        ProjectChangesForHistoryViewRequest request = new ProjectChangesForHistoryViewRequest(projectId, Optional.of(subject), pageRequest);

        ProjectChange pc1 = insertMockRevisionsEvent(projectId, "http://example.com/Entity1", 12345L);
        ProjectChange pc2 = insertMockRevisionsEvent(projectId, "http://example.com/Entity1", 12346L);
        ProjectChange pc3 = insertMockRevisionsEvent(projectId, "http://example.com/Entity2", 12347L);

        Mono<ProjectChangesForHistoryViewResponse> responseMono = commandHandler.handleRequest(request, null);
        ProjectChangesForHistoryViewResponse response = responseMono.block();

        assertNotNull(response);
        Page<ProjectChange> projectChanges = response.changes();
        assertEquals(2, projectChanges.getTotalElements());

        // ProjectChanges should be sorted by timestamp in DESC order when fetched
        assertEquals(pc2.getTimestamp(), projectChanges.getPageElements().get(0).getTimestamp());
        assertEquals(pc1.getTimestamp(), projectChanges.getPageElements().get(1).getTimestamp());
    }

    @Test
    public void GIVEN_validRequestWithoutSubject_WHEN_handleRequestCalled_THEN_returnAllProjectChanges() {
        ProjectId projectId = ProjectId.generate();
        PageRequest pageRequest = PageRequest.requestPageWithSize(1, 10);
        ProjectChangesForHistoryViewRequest request = new ProjectChangesForHistoryViewRequest(projectId, Optional.empty(), pageRequest);

        ProjectChange pc1 = insertMockRevisionsEvent(projectId, "http://example.com/Entity1", 12345L);
        ProjectChange pc2 = insertMockRevisionsEvent(projectId, "http://example.com/Entity1", 12346L);
        ProjectChange pc3 = insertMockRevisionsEvent(projectId, "http://example.com/Entity2", 12347L);

        Mono<ProjectChangesForHistoryViewResponse> responseMono = commandHandler.handleRequest(request, null);
        ProjectChangesForHistoryViewResponse response = responseMono.block();

        assertNotNull(response);
        Page<ProjectChange> projectChanges = response.changes();
        assertEquals(3, projectChanges.getTotalElements());

        // ProjectChanges should be sorted by timestamp in DESC order when fetched
        assertEquals(pc3.getTimestamp(), projectChanges.getPageElements().get(0).getTimestamp());
        assertEquals(pc2.getTimestamp(), projectChanges.getPageElements().get(1).getTimestamp());
        assertEquals(pc1.getTimestamp(), projectChanges.getPageElements().get(2).getTimestamp());
    }

    @Test
    public void GIVEN_emptyProject_WHEN_handleRequestCalled_THEN_returnEmptyPage() {
        ProjectId projectId = ProjectId.generate();
        PageRequest pageRequest = PageRequest.requestPageWithSize(1, 10);
        ProjectChangesForHistoryViewRequest request = new ProjectChangesForHistoryViewRequest(projectId, Optional.empty(), pageRequest);


        Mono<ProjectChangesForHistoryViewResponse> responseMono = commandHandler.handleRequest(request, null);
        ProjectChangesForHistoryViewResponse response = responseMono.block();

        assertNotNull(response);
        Page<ProjectChange> projectChanges = response.changes();
        assertEquals(0, projectChanges.getTotalElements());
    }

    @Test
    public void GIVEN_requestWithPagination_WHEN_handleRequestCalled_THEN_returnPaginatedResults() {
        ProjectId projectId = ProjectId.generate();
        PageRequest pageRequest = PageRequest.requestPageWithSize(1, 1);
        ProjectChangesForHistoryViewRequest request = new ProjectChangesForHistoryViewRequest(projectId, Optional.empty(), pageRequest);

        insertMockRevisionsEvent(projectId, "http://example.com/Entity1", 12345L);
        insertMockRevisionsEvent(projectId, "http://example.com/Entity2", 12346L);
        insertMockRevisionsEvent(projectId, "http://example.com/Entity3", 12347L);

        Mono<ProjectChangesForHistoryViewResponse> responseMono = commandHandler.handleRequest(request, null);
        ProjectChangesForHistoryViewResponse response = responseMono.block();

        assertNotNull(response);
        Page<ProjectChange> projectChanges = response.changes();
        assertEquals(1, projectChanges.getPageElements().size());
        assertEquals(3, projectChanges.getTotalElements());
    }

    private OWLEntity mockOWLEntity(String iri) {
        OWLEntity entity = mock(OWLEntity.class);
        when(entity.getIRI()).thenReturn(IRI.create(iri));
        return entity;
    }

    private ProjectChange insertMockRevisionsEvent(ProjectId projectId, String whoficEntityIri, long timestamp) {
        ProjectChange projectChange = ProjectChange.get(RevisionNumber.getRevisionNumber(1), UserId.valueOf("user1"), timestamp, "Description1", 0, Page.emptyPage());
        org.bson.Document projectChangeDocument = objectMapper.convertValue(projectChange, Document.class);
        RevisionsEvent revisionsEvent = RevisionsEvent.create(projectId, whoficEntityIri, timestamp, projectChangeDocument);
        mongoTemplate.save(revisionsEvent);

        return projectChange;
    }
}
