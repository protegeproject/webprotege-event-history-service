package edu.stanford.protege.webprotegeeventshistory;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.protege.webprotege.common.ProjectEvent;
import edu.stanford.protege.webprotege.ipc.EventDispatcher;
import edu.stanford.protege.webprotegeeventshistory.dto.*;
import edu.stanford.protege.webprotegeeventshistory.sequence.ProjectSequenceService;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Comparator;
import java.util.List;

@Service
public class HighLevelBusinessEventsService {

    private final static Logger LOGGER = LoggerFactory.getLogger(HighLevelBusinessEventsService.class);

    private final HighLevelBusinessEventsRepository repository;


    private final ObjectMapper objectMapper;

    private final ProjectSequenceService projectSequenceService;

    private final EventDispatcher eventDispatcher;


    // EventDispatcher is injected lazily to break a bean-creation cycle: the ipc EventDispatcher
    // depends on RabbitMQEventsConfiguration, which eagerly wires every EventHandler, one of which
    // (RegisterHighLevelBusinessEventHandler) depends back on this service.
    public HighLevelBusinessEventsService(HighLevelBusinessEventsRepository repository,
                                          ObjectMapper objectMapper,
                                          ProjectSequenceService projectSequenceService,
                                          @Lazy EventDispatcher eventDispatcher) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.projectSequenceService = projectSequenceService;
        this.eventDispatcher = eventDispatcher;
    }


    @Transactional
    void registerEvent(PackagedProjectChangeEvent projectEvent) {
        int seq;
        try {
            var nextDocument = objectMapper.convertValue(projectEvent, Document.class);
            seq = projectSequenceService.next(projectEvent.projectId().id());
            HighLevelBusinessEvent event = new HighLevelBusinessEvent(projectEvent.eventId().id(), projectEvent.projectId().id(), seq, nextDocument);
            LOGGER.info("Logging event " + event);
            repository.save(event);
        } catch (Exception e) {
            LOGGER.error("An error occurred when trying to save events", e);
            return;
        }
        publishSequencedEvent(projectEvent, seq);
    }

    /**
     * Re-publishes the persisted change bundle enriched with its sequence ordinal so the gateway
     * can push it with truthful event tags. This runs after the archive write, so anything pushed
     * is already durably fetchable. Unlike the persistence step above, a failure here is not
     * swallowed: reliable delivery of this publish is tracked by #299, so it is allowed to surface
     * rather than pass silently.
     */
    private void publishSequencedEvent(PackagedProjectChangeEvent projectEvent, int seq) {
        var sequencedEvent = new SequencedPackagedProjectChangeEvent(
                projectEvent.projectId(),
                projectEvent.eventId(),
                seq,
                projectEvent.projectEvents());
        eventDispatcher.dispatchEvent(sequencedEvent);
    }

    ProjectEventsQueryResponse fetchEvents(ProjectEventsQueryRequest request) {

        ProjectEventsQueryResponse response = new ProjectEventsQueryResponse();

        EventTag first = request.sinceTag == null ? EventTag.getFirst() : request.sinceTag;

        List<HighLevelBusinessEvent> mongoResponse = repository.findByTimeStampGreaterThanAndProjectId(first.getOrdinal(), request.projectId.id());
        mongoResponse.sort(Comparator.comparing(HighLevelBusinessEvent::timeStamp));
        EventTag last;
        if(mongoResponse.size() > 0) {
            last = EventTag.get(mongoResponse.get(mongoResponse.size() -1).timeStamp());
        } else {
            last = first;
        }
        List<ProjectEvent> eventList = mongoResponse.stream()
                .map(HighLevelBusinessEvent::projectEvent)
                .map(projectEvent -> objectMapper.convertValue(projectEvent, PackagedProjectChangeEvent.class))
                .flatMap(packagedProjectChangeEvent -> packagedProjectChangeEvent.projectEvents().stream())
                .toList();


        response.events = new EventList<>(first, eventList, last);
        return response;
    }
}
