package edu.stanford.protege.webprotegeeventshistory;


import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.protege.webprotege.common.EventId;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.ipc.impl.RabbitMQEventDispatcher;
import edu.stanford.protege.webprotege.tag.EntityTagsChangedEvent;
import edu.stanford.protege.webprotegeeventshistory.dto.EventTag;
import edu.stanford.protege.webprotegeeventshistory.dto.PackagedProjectChangeEvent;
import edu.stanford.protege.webprotegeeventshistory.dto.ProjectEventsQueryRequest;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class HighLevelBusinessEventsIntegrationTest extends IntegrationTest {

    @Autowired
    private HighLevelBusinessEventsRepository eventsRepository;

    @Autowired
    private GetLatestProjectEventsCommandHandler commandHandler;

    @MockBean
    private SimpMessagingTemplate simpleMessagingTemplate;

    @Autowired
    @Qualifier("eventRabbitTemplate")
    private RabbitTemplate eventRabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private ProjectId projectId;

    @Before
    public void setUp(){
        projectId = ProjectId.generate();
    }

    @Test
    public void GIVEN_newEvent_WHEN_register_THEN_eventIsSavedInDb() throws InterruptedException {

        var entityTagsChangedEvent = new EntityTagsChangedEvent(new EventId("eventId"),
                projectId,
                new OWLClassImpl(IRI.create("http://www.example.org/R9UuCy8Vzvft2f4fc67VwGs")),
                new ArrayList<>());
        var packagedProjectChangeEvent = new PackagedProjectChangeEvent(projectId, EventId.generate(), Arrays.asList(entityTagsChangedEvent));
        RabbitMQEventDispatcher dispatcher = new RabbitMQEventDispatcher(objectMapper, eventRabbitTemplate);
        dispatcher.dispatchEvent(packagedProjectChangeEvent);

        Thread.sleep(5000);

        List<HighLevelBusinessEvent> eventList = eventsRepository.findByTimeStampGreaterThanAndProjectId(0, projectId.id());

        assertNotNull(eventList);
        assertEquals(1, eventList.size());
        HighLevelBusinessEvent event = eventList.get(0);
        assertEquals(packagedProjectChangeEvent.eventId().id(), event.eventId());
    }


    @Test
    public void GIVEN_multipleEvents_WHEN_fetchEvents_THEN_onlyNewerEventsAreReturned(){
        saveEvents(3, 6);
        ProjectEventsQueryRequest request = new ProjectEventsQueryRequest();
        request.sinceTag = EventTag.get(5);
        request.projectId = projectId;
        var response = commandHandler.handleRequest(request, new ExecutionContext());

        response.subscribe(resp -> {
            assertEquals(2, resp.events.events().size());
            assertEquals(6, resp.events.endTag().getOrdinal());
        });

    }

    private void saveEvents(int eventStartTag, int nrOfEvents) {

        for(int i = eventStartTag; i<nrOfEvents; i ++) {
            var projectEvent = new EntityTagsChangedEvent(new EventId("eventId"),
                    projectId,
                    new OWLClassImpl(IRI.create("http://www.example.org/R9UuCy8Vzvft2"+ i)),
                    new ArrayList<>());

            var highLevelEvent = new HighLevelBusinessEvent("eventId", projectId.id(), i, objectMapper.convertValue(projectEvent, Document.class));
            eventsRepository.save(highLevelEvent);
        }
        var projectEvent = new EntityTagsChangedEvent(new EventId("eventId"),
                ProjectId.generate(),
                new OWLClassImpl(IRI.create("http://www.example.org/R9UuCy8Vzvft2asdasd")),
                new ArrayList<>());

        var highLevelEvent = new HighLevelBusinessEvent("eventId", projectId.id(), eventStartTag+1, objectMapper.convertValue(projectEvent, Document.class));
        eventsRepository.save(highLevelEvent);
    }

}
