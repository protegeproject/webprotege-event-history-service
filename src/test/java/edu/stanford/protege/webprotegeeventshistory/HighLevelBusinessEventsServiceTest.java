package edu.stanford.protege.webprotegeeventshistory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.protege.webprotege.common.EventId;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.tag.EntityTagsChangedEvent;
import edu.stanford.protege.webprotegeeventshistory.config.ObjectMapperConfiguration;
import edu.stanford.protege.webprotegeeventshistory.dto.*;
import edu.stanford.protege.webprotegeeventshistory.sequence.SequenceService;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.semanticweb.owlapi.model.IRI;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.GenericMessage;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class HighLevelBusinessEventsServiceTest {


    @Mock
    private HighLevelBusinessEventsRepository repository;

    @Mock
    private SequenceService sequenceService;

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    private HighLevelBusinessEventsService service;

    private PackagedProjectChangeEvent packagedProjectChangeEvent;

    private EntityTagsChangedEvent entityTagsChangedEvent;
    private ProjectId projectId;

    private EventId eventId;

    private ObjectMapper objectMapper;
    private final ArgumentCaptor<HighLevelBusinessEvent> captor = ArgumentCaptor.forClass(HighLevelBusinessEvent.class);

    private final ArgumentCaptor<GenericMessage> websocketCaptor = ArgumentCaptor.forClass(GenericMessage.class);

    @Before
    public void setUp() {
        objectMapper = new ObjectMapperConfiguration().objectMapper();
        service = new HighLevelBusinessEventsService(repository, objectMapper, sequenceService, simpMessagingTemplate);
        projectId = ProjectId.generate();
        eventId = EventId.generate();
        entityTagsChangedEvent = new EntityTagsChangedEvent(new EventId("eventId"),
                projectId,
                new OWLClassImpl(IRI.create("http://www.example.org/R9UuCy8Vzvft2f4fc67VwGs")),
                new ArrayList<>());
        packagedProjectChangeEvent = new PackagedProjectChangeEvent(projectId, eventId, List.of(entityTagsChangedEvent));
        when(sequenceService.getNextHighLevelEventSequence()).thenReturn(1);
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
    public void GIVEN_entityTagsChangedEvent_WHEN_registerEvent_THEN_eventIsPushedToWebsocket() throws JsonProcessingException {
        service.registerEvent(packagedProjectChangeEvent);


        verify(simpMessagingTemplate).send(eq("/topic/project-events/" + projectId.id()), websocketCaptor.capture());

        var capturedMessage = websocketCaptor.getValue();
        ProjectEventsQueryResponse response = new ProjectEventsQueryResponse();
        response.events = new EventList(EventTag.getFirst(), packagedProjectChangeEvent.projectEvents(), EventTag.get(1));

        String expectedEvent = objectMapper.writeValueAsString(response);


        assertEquals(objectMapper.readTree(expectedEvent), objectMapper.readTree(new String( (byte[]) capturedMessage.getPayload())));

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
    public void GIVEN_fetchTag_WHEN_fetchEvents_THEN_eventsAreMapped() {
        HighLevelBusinessEvent businessEvent = new HighLevelBusinessEvent("eventId", projectId.id(), 5, objectMapper.convertValue(packagedProjectChangeEvent, Document.class));
        when(repository.findByTimeStampGreaterThanAndProjectId(eq(2), eq(projectId.id()))).thenReturn(Arrays.asList(businessEvent));
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
        when(repository.findByTimeStampGreaterThanAndProjectId(eq(2),eq(projectId.id()))).thenReturn(new ArrayList<>());
        ProjectEventsQueryRequest request = new ProjectEventsQueryRequest();
        request.sinceTag = EventTag.get(4);
        request.projectId = projectId;

        ProjectEventsQueryResponse response = service.fetchEvents(request);

        assertEquals(4, response.events.endTag().getOrdinal());
        assertEquals(4, response.events.startTag().getOrdinal());

    }

}
