package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.services;

import edu.stanford.protege.webprotege.change.ProjectChange;
import edu.stanford.protege.webprotege.common.Page;
import edu.stanford.protege.webprotege.common.*;
import edu.stanford.protege.webprotege.revision.RevisionNumber;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.dto.*;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.events.*;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.mappers.*;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.repositories.RevisionsEventRepository;
import org.bson.Document;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.semanticweb.owlapi.model.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.*;

import java.sql.Timestamp;
import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NewRevisionsEventServiceTest {

    @Mock
    private RevisionsEventRepository repository;

    @Mock
    private RevisionEventMapper revisionEventMapper;

    @Mock
    private ProjectChangeMapper projectChangeMapper;

    @InjectMocks
    private NewRevisionsEventServiceImpl service;


    private ProjectId projectId;
    private Timestamp timestamp;


    @BeforeEach
    public void setUp() {
        projectId = new ProjectId("testProjectId");
        timestamp = new Timestamp(System.currentTimeMillis());
    }

    @Test
    public void GIVEN_validNewLinearizationRevisionsEvent_WHEN_registerEventCalled_THEN_revisionsEventsSavedToRepository() {
        Set<ProjectChangeForEntity> changes = Set.of(mock(ProjectChangeForEntity.class));
        NewRevisionsEvent event = NewRevisionsEvent.create(EventId.generate(), projectId, changes, ChangeRequestId.generate());

        RevisionsEvent mockRevisionsEvent = RevisionsEvent.create(projectId, "whoficEntityIri", ChangeType.UPDATE_ENTITY, 12345L, new Document(), ChangeRequestId.generate());
        when(revisionEventMapper.mapNewRevisionsEventToRevisionsEvents(event))
                .thenReturn(List.of(mockRevisionsEvent));

        service.registerEvent(event);

        verify(repository).saveAll(any());
        verify(revisionEventMapper).mapNewRevisionsEventToRevisionsEvents(event);
    }

    @Test
    public void GIVEN_validProjectIdAndSubject_WHEN_fetchPaginatedProjectChangesCalled_THEN_returnPaginatedProjectChanges() {
        OWLEntity mockEntity = mock(OWLEntity.class);
        IRI mockIri = IRI.create("http://example.com/entity");
        when(mockEntity.getIRI()).thenReturn(mockIri);

        RevisionsEvent mockRevisionsEvent = RevisionsEvent.create(projectId, mockIri.toString(), ChangeType.UPDATE_ENTITY, 12345L, new Document(), ChangeRequestId.generate());
        PageRequest pageRequest = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "timestamp"));
        org.springframework.data.domain.Page<RevisionsEvent> mockPage = new PageImpl<>(List.of(mockRevisionsEvent), pageRequest, 1);

        when(repository.findAll(any(Example.class), eq(pageRequest))).thenReturn(mockPage);

        ProjectChange mockProjectChange = mock(ProjectChange.class);
        when(projectChangeMapper.mapProjectChangeDocumentToProjectChange(any())).thenReturn(mockProjectChange);

        Page<ProjectChange> result = service.fetchPaginatedProjectChanges(projectId, Optional.of(mockEntity), 1, 1);

        assertNotNull(result);
        assertEquals(1, result.getPageElements().size());
        assertEquals(mockProjectChange, result.getPageElements().get(0));

        verify(repository).findAll(any(Example.class), eq(pageRequest));
        verify(projectChangeMapper).mapProjectChangeDocumentToProjectChange(any());
    }

    @Test
    public void GIVEN_nullSubject_WHEN_fetchPaginatedProjectChangesCalled_THEN_returnPaginatedProjectChanges() {

        RevisionsEvent mockRevisionsEvent = RevisionsEvent.create(projectId, null, ChangeType.CREATE_ENTITY, 12345L, new Document(), ChangeRequestId.generate());
        PageRequest pageRequest = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "timestamp"));
        org.springframework.data.domain.Page<RevisionsEvent> mockPage = new PageImpl<>(List.of(mockRevisionsEvent), pageRequest, 1);

        when(repository.findAll(any(Example.class), eq(pageRequest))).thenReturn(mockPage);

        ProjectChange mockProjectChange = mock(ProjectChange.class);
        when(projectChangeMapper.mapProjectChangeDocumentToProjectChange(any())).thenReturn(mockProjectChange);

        Page<ProjectChange> result = service.fetchPaginatedProjectChanges(projectId, Optional.empty(), 1, 1);

        assertNotNull(result);
        assertEquals(1, result.getPageElements().size());
        assertEquals(mockProjectChange, result.getPageElements().get(0));

        verify(repository).findAll(any(Example.class), eq(pageRequest));
        verify(projectChangeMapper).mapProjectChangeDocumentToProjectChange(any());
    }

    @Test
    public void GIVEN_noResults_WHEN_fetchPaginatedProjectChangesCalled_THEN_returnEmptyPage() {

        PageRequest pageRequest = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "timestamp"));
        org.springframework.data.domain.Page<RevisionsEvent> mockPage = new PageImpl<>(List.of(), pageRequest, 0);

        when(repository.findAll(any(Example.class), eq(pageRequest))).thenReturn(mockPage);

        Page<ProjectChange> result = service.fetchPaginatedProjectChanges(projectId, Optional.empty(), 1, 1);

        assertNotNull(result);
        assertTrue(result.getPageElements().isEmpty());

        verify(repository).findAll(any(Example.class), eq(pageRequest));
        verifyNoInteractions(projectChangeMapper);
    }

    @Test
    public void GIVEN_noEntitiesChangedAfterTimestamp_WHEN_getChangedEntitiesAfterTimestampCalled_THEN_emptyChangedEntitiesReturned() {
        when(repository.findByProjectIdAndTimestampAfter(projectId.id(), timestamp.getTime())).thenReturn(List.of());

        ChangedEntities result = service.getChangedEntitiesAfterTimestamp(projectId, timestamp.getTime());

        assertEquals(0, result.createdEntities().size());
        assertEquals(0, result.updatedEntities().size());
        assertEquals(0, result.deletedEntities().size());

        verify(repository).findByProjectIdAndTimestampAfter(projectId.id(), timestamp.getTime());
    }

    @Test
    public void GIVEN_entitiesChangedAfterTimestamp_WHEN_getChangedEntitiesAfterTimestampCalled_THEN_returnGroupedChangedEntities() {
        RevisionsEvent createdEntity = RevisionsEvent.create(projectId, "entityIRI1", ChangeType.CREATE_ENTITY, timestamp.getTime(), new Document(), ChangeRequestId.generate());
        RevisionsEvent updatedEntity = RevisionsEvent.create(projectId, "entityIRI2", ChangeType.UPDATE_ENTITY, timestamp.getTime() + 1000, new Document(), ChangeRequestId.generate());
        RevisionsEvent deletedEntity = RevisionsEvent.create(projectId, "entityIRI3", ChangeType.DELETE_ENTITY, timestamp.getTime() + 2000, new Document(), ChangeRequestId.generate());

        when(repository.findByProjectIdAndTimestampAfter(projectId.id(), timestamp.getTime())).thenReturn(List.of(createdEntity, updatedEntity, deletedEntity));

        ChangedEntities result = service.getChangedEntitiesAfterTimestamp(projectId, timestamp.getTime());

        assertEquals(1, result.createdEntities().size());
        assertEquals("entityIRI1", result.createdEntities().get(0));

        assertEquals(1, result.updatedEntities().size());
        assertEquals("entityIRI2", result.updatedEntities().get(0));

        assertEquals(1, result.deletedEntities().size());
        assertEquals("entityIRI3", result.deletedEntities().get(0));

        verify(repository).findByProjectIdAndTimestampAfter(projectId.id(), timestamp.getTime());
    }

    @Test
    public void GIVEN_multipleEntitiesChangedAfterTimestamp_WHEN_getChangedEntitiesAfterTimestampCalled_THEN_returnDeduplicatedChangedEntities() {
        RevisionsEvent createdEntity1 = RevisionsEvent.create(projectId, "entityIRI1", ChangeType.CREATE_ENTITY, timestamp.getTime(), new Document(), ChangeRequestId.generate());
        RevisionsEvent createdEntity2 = RevisionsEvent.create(projectId, "entityIRI1", ChangeType.CREATE_ENTITY, timestamp.getTime() + 1000, new Document(), ChangeRequestId.generate());
        RevisionsEvent updatedEntity = RevisionsEvent.create(projectId, "entityIRI2", ChangeType.UPDATE_ENTITY, timestamp.getTime() + 2000, new Document(), ChangeRequestId.generate());

        when(repository.findByProjectIdAndTimestampAfter(projectId.id(), timestamp.getTime())).thenReturn(List.of(createdEntity1, createdEntity2, updatedEntity));

        ChangedEntities result = service.getChangedEntitiesAfterTimestamp(projectId, timestamp.getTime());

        assertEquals(1, result.createdEntities().size());
        assertEquals("entityIRI1", result.createdEntities().get(0));

        assertEquals(1, result.updatedEntities().size());
        assertEquals("entityIRI2", result.updatedEntities().get(0));

        assertEquals(0, result.deletedEntities().size());

        verify(repository).findByProjectIdAndTimestampAfter(projectId.id(), timestamp.getTime());
    }

    @Test
    public void GIVEN_noHistoryForEntity_WHEN_getEntityHistorySummaryCalled_THEN_returnEmptyHistorySummary() {
        String entityIri = "http://example.com/entity1";

        when(repository.findByProjectIdAndWhoficEntityIriOrderByTimestampDesc(projectId, entityIri))
                .thenReturn(Collections.emptyList());

        EntityHistorySummary result = service.getEntityHistorySummary(projectId, entityIri);

        assertNotNull(result);
        assertTrue(result.changes().isEmpty());

        verify(repository).findByProjectIdAndWhoficEntityIriOrderByTimestampDesc(projectId, entityIri);
    }

    @Test
    public void GIVEN_validHistoryForEntity_WHEN_getEntityHistorySummaryCalled_THEN_returnMappedHistorySummary() {
        String entityIri = "http://example.com/entity1";
        long timestamp1 = 1234567890123L;
        long timestamp2 = 1234567890124L;
        UserId userId1 = UserId.valueOf("user1");
        UserId userId2 = UserId.valueOf("user2");

        Document document1 = new Document("a", "b");
        Document document2 = new Document("c", "d");

        RevisionsEvent eventCreate = RevisionsEvent.create(projectId, entityIri, ChangeType.CREATE_ENTITY, timestamp2, document2, ChangeRequestId.generate());
        RevisionsEvent eventUpdate = RevisionsEvent.create(projectId, entityIri, ChangeType.UPDATE_ENTITY, timestamp1, document1, ChangeRequestId.generate());

        ProjectChange changeCreate = ProjectChange.get(
                RevisionNumber.getRevisionNumber(1),
                userId1,
                timestamp1,
                "Create",
                1,
                Page.emptyPage()
        );
        ProjectChange changeUpdate = ProjectChange.get(
                RevisionNumber.getRevisionNumber(2),
                userId2,
                timestamp2,
                "Update",
                1,
                Page.emptyPage()
        );

        when(repository.findByProjectIdAndWhoficEntityIriOrderByTimestampDesc(projectId, entityIri))
                .thenReturn(List.of(eventUpdate, eventCreate));
        when(projectChangeMapper.mapProjectChangeDocumentToProjectChange(eq(eventCreate.projectChange())))
                .thenReturn(changeCreate);
        when(projectChangeMapper.mapProjectChangeDocumentToProjectChange(eq(eventUpdate.projectChange())))
                .thenReturn(changeUpdate);

        EntityHistorySummary result = service.getEntityHistorySummary(projectId, entityIri);

        assertNotNull(result);
        assertEquals(2, result.changes().size());

        EntityChange entityUpdate = result.changes().get(0);
        assertEquals("Update", entityUpdate.changeSummary());
        assertEquals(userId2, entityUpdate.userId());
        assertEquals(LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp2), ZoneId.of("UTC")), entityUpdate.timestamp());

        EntityChange entityCreate = result.changes().get(1);
        assertEquals("Create", entityCreate.changeSummary());
        assertEquals(userId1, entityCreate.userId());
        assertEquals(LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp1), ZoneId.of("UTC")), entityCreate.timestamp());

        verify(repository).findByProjectIdAndWhoficEntityIriOrderByTimestampDesc(projectId, entityIri);
        verify(projectChangeMapper).mapProjectChangeDocumentToProjectChange(eventCreate.projectChange());
        verify(projectChangeMapper).mapProjectChangeDocumentToProjectChange(eventUpdate.projectChange());
    }

    @Test
    public void GIVEN_emptyHistory_WHEN_getEntityHistorySummaryCalled_THEN_returnEmptySummary() {
        String entityIri = "http://example.com/entity2";

        when(repository.findByProjectIdAndWhoficEntityIriOrderByTimestampDesc(projectId, entityIri))
                .thenReturn(Collections.emptyList());

        EntityHistorySummary result = service.getEntityHistorySummary(projectId, entityIri);

        assertNotNull(result);
        assertTrue(result.changes().isEmpty());

        verify(repository).findByProjectIdAndWhoficEntityIriOrderByTimestampDesc(projectId, entityIri);
    }
}
