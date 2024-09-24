package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.protege.webprotege.change.ProjectChange;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.dto.ProjectChangeForEntity;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.events.RevisionsEvent;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RevisionEventMapperTest {

    @Mock
    private ObjectMapper objectMapper;
    @InjectMocks
    private RevisionEventMapper revisionEventMapper;

    @Test
    public void GIVEN_validProjectIdAndChanges_WHEN_mapNewLinearizationRevisionsEventToRevisionsEvents_THEN_returnListOfRevisionsEvents() {
        ProjectId projectId = new ProjectId("testProjectId");
        ProjectChange mockProjectChange = mock(ProjectChange.class);
        ProjectChangeForEntity change1 = ProjectChangeForEntity.create("whoficEntityIri1", mockProjectChange);
        ProjectChangeForEntity change2 = ProjectChangeForEntity.create("whoficEntityIri2", mockProjectChange);
        Set<ProjectChangeForEntity> changes = new LinkedHashSet<>();
        changes.add(change1);
        changes.add(change2);

        when(mockProjectChange.getTimestamp()).thenReturn(12345L);

        Document mockDocument = new Document();
        when(objectMapper.convertValue(mockProjectChange, Document.class)).thenReturn(mockDocument);

        List<RevisionsEvent> result = revisionEventMapper.mapNewLinearizationRevisionsEventToRevisionsEvents(projectId, changes);

        assertNotNull(result);
        assertEquals(2, result.size());

        RevisionsEvent firstEvent = result.get(0);
        RevisionsEvent secondEvent = result.get(1);

        assertEquals("whoficEntityIri1", firstEvent.whoficEntityIri());
        assertEquals(12345L, firstEvent.timestamp());
        assertEquals(mockDocument, firstEvent.projectChange());

        assertEquals("whoficEntityIri2", secondEvent.whoficEntityIri());
        assertEquals(12345L, secondEvent.timestamp());
        assertEquals(mockDocument, secondEvent.projectChange());

        verify(objectMapper, times(2)).convertValue(mockProjectChange, Document.class);
    }

    @Test
    public void GIVEN_emptyChangesSet_WHEN_mapNewLinearizationRevisionsEventToRevisionsEvents_THEN_returnEmptyList() {
        ProjectId projectId = new ProjectId("testProjectId");
        Set<ProjectChangeForEntity> emptyChanges = Set.of();

        List<RevisionsEvent> result = revisionEventMapper.mapNewLinearizationRevisionsEventToRevisionsEvents(projectId, emptyChanges);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verifyNoInteractions(objectMapper);
    }

    @Test
    public void GIVEN_nullChangesSet_WHEN_mapNewLinearizationRevisionsEventToRevisionsEvents_THEN_returnEmptyList() {
        ProjectId projectId = new ProjectId("testProjectId");
        Set<ProjectChangeForEntity> nullChanges = null;

        assertThrows(NullPointerException.class, () -> revisionEventMapper.mapNewLinearizationRevisionsEventToRevisionsEvents(projectId, nullChanges));

        verifyNoInteractions(objectMapper);
    }
}
