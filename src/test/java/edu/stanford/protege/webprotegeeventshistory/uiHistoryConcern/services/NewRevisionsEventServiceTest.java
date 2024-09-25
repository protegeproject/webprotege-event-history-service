package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.services;

import edu.stanford.protege.webprotege.change.ProjectChange;
import edu.stanford.protege.webprotege.common.Page;
import edu.stanford.protege.webprotege.common.*;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.dto.ProjectChangeForEntity;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.events.*;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.mappers.*;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.repositories.RevisionsEventRepository;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.semanticweb.owlapi.model.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.*;

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

    @Test
    public void GIVEN_validNewLinearizationRevisionsEvent_WHEN_registerEventCalled_THEN_revisionsEventsSavedToRepository() {
        ProjectId projectId = new ProjectId("testProjectId");
        Set<ProjectChangeForEntity> changes = Set.of(mock(ProjectChangeForEntity.class));
        NewRevisionsEvent event = NewRevisionsEvent.create(EventId.generate(), projectId, changes);

        RevisionsEvent mockRevisionsEvent = RevisionsEvent.create(projectId, "whoficEntityIri", 12345L, new Document());
        when(revisionEventMapper.mapNewRevisionsEventToRevisionsEvents(event))
                .thenReturn(List.of(mockRevisionsEvent));

        service.registerEvent(event);

        verify(repository).saveAll(any());
        verify(revisionEventMapper).mapNewRevisionsEventToRevisionsEvents(event);
    }

    @Test
    public void GIVEN_validProjectIdAndSubject_WHEN_fetchPaginatedProjectChangesCalled_THEN_returnPaginatedProjectChanges() {
        ProjectId projectId = new ProjectId("testProjectId");
        OWLEntity mockEntity = mock(OWLEntity.class);
        IRI mockIri = IRI.create("http://example.com/entity");
        when(mockEntity.getIRI()).thenReturn(mockIri);

        RevisionsEvent mockRevisionsEvent = RevisionsEvent.create(projectId, mockIri.toString(), 12345L, new Document());
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
        ProjectId projectId = new ProjectId("testProjectId");

        RevisionsEvent mockRevisionsEvent = RevisionsEvent.create(projectId, null, 12345L, new Document());
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
        ProjectId projectId = new ProjectId("testProjectId");

        PageRequest pageRequest = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "timestamp"));
        org.springframework.data.domain.Page<RevisionsEvent> mockPage = new PageImpl<>(List.of(), pageRequest, 0);

        when(repository.findAll(any(Example.class), eq(pageRequest))).thenReturn(mockPage);

        Page<ProjectChange> result = service.fetchPaginatedProjectChanges(projectId, Optional.empty(), 1, 1);

        assertNotNull(result);
        assertTrue(result.getPageElements().isEmpty());

        verify(repository).findAll(any(Example.class), eq(pageRequest));
        verifyNoInteractions(projectChangeMapper);
    }
}
