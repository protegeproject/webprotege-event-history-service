package edu.stanford.protege.webprotegeeventshistory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.protege.webprotege.common.EventId;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.ipc.EventDispatcher;
import edu.stanford.protege.webprotege.tag.EntityTagsChangedEvent;
import edu.stanford.protege.webprotegeeventshistory.config.ObjectMapperConfiguration;
import edu.stanford.protege.webprotegeeventshistory.dto.*;
import edu.stanford.protege.webprotegeeventshistory.sequence.ProjectSequenceService;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.semanticweb.owlapi.model.IRI;
import org.springframework.data.domain.Pageable;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class HighLevelBusinessEventsServiceTest {


    @Mock
    private HighLevelBusinessEventsRepository repository;

    @Mock
    private ProjectSequenceService projectSequenceService;

    @Mock
    private EventDispatcher eventDispatcher;

    private HighLevelBusinessEventsService service;

    private PackagedProjectChangeEvent packagedProjectChangeEvent;

    private EntityTagsChangedEvent entityTagsChangedEvent;
    private ProjectId projectId;

    private EventId eventId;

    private ObjectMapper objectMapper;
    private final ArgumentCaptor<HighLevelBusinessEvent> captor = ArgumentCaptor.forClass(HighLevelBusinessEvent.class);


    @Before
    public void setUp() {
        objectMapper = new ObjectMapperConfiguration().objectMapper();
        service = new HighLevelBusinessEventsService(repository, objectMapper, projectSequenceService, eventDispatcher, 500);
        projectId = ProjectId.generate();
        eventId = EventId.generate();
        entityTagsChangedEvent = new EntityTagsChangedEvent(new EventId("eventId"),
                projectId,
                new OWLClassImpl(IRI.create("http://www.example.org/R9UuCy8Vzvft2f4fc67VwGs")),
                new ArrayList<>());
        packagedProjectChangeEvent = new PackagedProjectChangeEvent(projectId, eventId, List.of(entityTagsChangedEvent));
        when(projectSequenceService.next(projectId.id())).thenReturn(1);
    }

    @Test
    public void GIVEN_entityTagsChangedEvent_WHEN_registerEvent_THEN_correctTagIsUsed() throws JsonProcessingException {


        service.registerEvent(packagedProjectChangeEvent);


        verify(repository).save(captor.capture());
        var highLevelEvent = captor.getValue();

        assertEquals(1, highLevelEvent.timeStamp());
        assertEquals(eventId.id(), highLevelEvent.eventId());
        assertEquals(projectId.id(), highLevelEvent.projectId());
    }

    @Test
    public void GIVEN_entityTagsChangedEvent_WHEN_registerEvent_THEN_eventIsMapped() {
        service.registerEvent(packagedProjectChangeEvent);

        verify(repository).save(captor.capture());
        var eventDocument = captor.getValue().projectEvent();

        assertEquals("webprotege.events.projects.PackagedProjectChange", eventDocument.get("@type"));
        List<LinkedHashMap<String, LinkedHashMap<String, Document>>> events = (List<LinkedHashMap<String, LinkedHashMap<String, Document>>>) eventDocument.get("projectEvents");
        assertNotNull(events);
        assertEquals(1, events.size());
        LinkedHashMap<String,LinkedHashMap<String, Document>> eventsDocument =events.get(0);
        assertEquals("webprotege.events.tags.EntityTagsChanged", eventsDocument.get("@type"));
        LinkedHashMap<String,Document> entityDocument = eventsDocument.get("entity");

        assertEquals("http://www.example.org/R9UuCy8Vzvft2f4fc67VwGs", entityDocument.get("iri"));
    }

    @Test
    public void GIVEN_entityTagsChangedEvent_WHEN_registerEvent_THEN_sequencedEventIsPublished() {
        service.registerEvent(packagedProjectChangeEvent);

        ArgumentCaptor<SequencedPackagedProjectChangeEvent> publishCaptor =
                ArgumentCaptor.forClass(SequencedPackagedProjectChangeEvent.class);
        verify(eventDispatcher).dispatchEvent(publishCaptor.capture());
        var published = publishCaptor.getValue();

        assertEquals(SequencedPackagedProjectChangeEvent.CHANNEL, published.getChannel());
        assertEquals(1, published.sequenceNumber());
        assertEquals(projectId, published.projectId());
        assertEquals(eventId, published.eventId());
        assertEquals(1, published.projectEvents().size());
        assertEquals(entityTagsChangedEvent, published.projectEvents().get(0));
    }

    @Test
    public void GIVEN_saveFails_WHEN_registerEvent_THEN_noSequencedEventIsPublished() {
        when(repository.save(any(HighLevelBusinessEvent.class))).thenThrow(new RuntimeException("boom"));

        service.registerEvent(packagedProjectChangeEvent);

        org.mockito.Mockito.verifyNoInteractions(eventDispatcher);
    }

    @Test
    public void GIVEN_fetchTag_WHEN_fetchEvents_THEN_eventsAreMapped() {
        HighLevelBusinessEvent businessEvent = new HighLevelBusinessEvent("eventId", projectId.id(), 5, objectMapper.convertValue(packagedProjectChangeEvent, Document.class));
        when(repository.findByProjectIdAndTimeStampGreaterThan(eq(projectId.id()), eq(2), any(Pageable.class))).thenReturn(Arrays.asList(businessEvent));
        ProjectEventsQueryRequest request = new ProjectEventsQueryRequest();
        request.sinceTag = EventTag.get(2);
        request.projectId = projectId;
        ProjectEventsQueryResponse response = service.fetchEvents(request);

        assertNotNull(response);
        assertNotNull(response.events);
        assertEquals(1, response.events.events().size());
        assertEquals(entityTagsChangedEvent, response.events.events().get(0));
        assertEquals(2, response.events.startTag().getOrdinal());
        assertEquals(5, response.events.endTag().getOrdinal());
    }

    @Test
    public void GIVEN_emptyListOnRepository_WHEN_fetchEvents_THEN_startTagIsSameAsEndTag(){
        when(repository.findByProjectIdAndTimeStampGreaterThan(eq(projectId.id()), eq(4), any(Pageable.class))).thenReturn(new ArrayList<>());
        ProjectEventsQueryRequest request = new ProjectEventsQueryRequest();
        request.sinceTag = EventTag.get(4);
        request.projectId = projectId;

        ProjectEventsQueryResponse response = service.fetchEvents(request);

        assertEquals(4, response.events.endTag().getOrdinal());
        assertEquals(4, response.events.startTag().getOrdinal());

    }

    @Test
    public void GIVEN_latestOnly_WHEN_fetchEvents_THEN_emptyWindowAtHeadAndNoRowsRead() {
        when(projectSequenceService.getCurrentSequence(projectId.id())).thenReturn(7);
        ProjectEventsQueryRequest request = new ProjectEventsQueryRequest();
        request.latestOnly = true;
        request.projectId = projectId;

        ProjectEventsQueryResponse response = service.fetchEvents(request);

        assertNotNull(response.events);
        assertEquals(0, response.events.events().size());
        // Anchor at the current head: first == last == head, and no event rows are touched.
        assertEquals(7, response.events.startTag().getOrdinal());
        assertEquals(7, response.events.endTag().getOrdinal());
        verifyNoInteractions(repository);
    }

    @Test
    public void GIVEN_multipleEvents_WHEN_fetchEvents_THEN_windowIsCappedAndEndTagIsWindowMax() {
        HighLevelBusinessEvent third = new HighLevelBusinessEvent("e3", projectId.id(), 3, objectMapper.convertValue(packagedProjectChangeEvent, Document.class));
        HighLevelBusinessEvent fourth = new HighLevelBusinessEvent("e4", projectId.id(), 4, objectMapper.convertValue(packagedProjectChangeEvent, Document.class));
        when(repository.findByProjectIdAndTimeStampGreaterThan(eq(projectId.id()), eq(2), any(Pageable.class)))
                .thenReturn(Arrays.asList(third, fourth));
        ProjectEventsQueryRequest request = new ProjectEventsQueryRequest();
        request.sinceTag = EventTag.get(2);
        request.projectId = projectId;

        ProjectEventsQueryResponse response = service.fetchEvents(request);

        // endTag is the last ordinal in the returned window, so a far-behind client pages forward.
        assertEquals(2, response.events.startTag().getOrdinal());
        assertEquals(4, response.events.endTag().getOrdinal());
        assertEquals(2, response.events.events().size());

        // The query is bounded: a window-sized Pageable is passed to the repository.
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findByProjectIdAndTimeStampGreaterThan(eq(projectId.id()), eq(2), pageableCaptor.capture());
        assertEquals(500, pageableCaptor.getValue().getPageSize());
    }

}
