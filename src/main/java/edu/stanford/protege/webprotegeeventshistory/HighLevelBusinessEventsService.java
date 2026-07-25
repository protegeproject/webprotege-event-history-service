package edu.stanford.protege.webprotegeeventshistory;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.protege.webprotege.common.ProjectEvent;
import edu.stanford.protege.webprotege.ipc.EventDispatcher;
import edu.stanford.protege.webprotegeeventshistory.dto.*;
import edu.stanford.protege.webprotegeeventshistory.sequence.ProjectSequenceService;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Service
public class HighLevelBusinessEventsService {

    private final static Logger LOGGER = LoggerFactory.getLogger(HighLevelBusinessEventsService.class);

    private final HighLevelBusinessEventsRepository repository;


    private final ObjectMapper objectMapper;

    private final ProjectSequenceService projectSequenceService;

    private final EventDispatcher eventDispatcher;

    // Caps how many archived events a single catch-up response may carry, so a project open never
    // replays the whole (unbounded) history in one shot. A far-behind client pages forward over
    // successive polls. Configurable via webprotege.events.query.window-size (default 500).
    private final int windowSize;


    // EventDispatcher is injected lazily to break a bean-creation cycle: the ipc EventDispatcher
    // depends on RabbitMQEventsConfiguration, which eagerly wires every EventHandler, one of which
    // (RegisterHighLevelBusinessEventHandler) depends back on this service.
    public HighLevelBusinessEventsService(HighLevelBusinessEventsRepository repository,
                                          ObjectMapper objectMapper,
                                          ProjectSequenceService projectSequenceService,
                                          @Lazy EventDispatcher eventDispatcher,
                                          @Value("${webprotege.events.query.window-size:500}") int windowSize) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.projectSequenceService = projectSequenceService;
        this.eventDispatcher = eventDispatcher;
        this.windowSize = windowSize;
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

        // Anchor mode (#301): the client only wants the current head so it can begin listening for
        // live updates without downloading history. Skip Mongo entirely and return an empty window
        // whose start and end are both the current sequence head.
        if (request.latestOnly) {
            EventTag head = EventTag.get(projectSequenceService.getCurrentSequence(request.projectId.id()));
            response.events = new EventList<ProjectEvent>(head, List.of(), head);
            return response;
        }

        EventTag first = request.sinceTag == null ? EventTag.getFirst() : request.sinceTag;

        // Bounded, ordered page: never replay the whole archive in a single response. The query is
        // capped at windowSize and sorted ascending by ordinal, so the last row is the end of this
        // window; endTag reflects that, letting a far-behind client page forward on later polls.
        List<HighLevelBusinessEvent> mongoResponse = repository.findByProjectIdAndTimeStampGreaterThan(
                request.projectId.id(),
                first.getOrdinal(),
                PageRequest.of(0, windowSize, Sort.by(Sort.Direction.ASC, "timeStamp")));
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
