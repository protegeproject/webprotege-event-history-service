package edu.stanford.protege.webprotegeeventshistory;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.protege.webprotege.common.ProjectEvent;
import edu.stanford.protege.webprotegeeventshistory.dto.*;
import edu.stanford.protege.webprotegeeventshistory.sequence.SequenceService;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Comparator;
import java.util.List;

@Service
public class HighLevelBusinessEventsService {

    private final static Logger LOGGER = LoggerFactory.getLogger(HighLevelBusinessEventsService.class);

    private final HighLevelBusinessEventsRepository repository;


    private final ObjectMapper objectMapper;

    private final SequenceService sequenceService;


    public HighLevelBusinessEventsService(HighLevelBusinessEventsRepository repository, ObjectMapper objectMapper, SequenceService sequenceService) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.sequenceService = sequenceService;
    }


    @Transactional
    void registerEvent(PackagedProjectChangeEvent projectEvent) {
        try {
            var nextDocument = objectMapper.convertValue(projectEvent, Document.class);

            HighLevelBusinessEvent event = new HighLevelBusinessEvent(projectEvent.eventId().id(), projectEvent.projectId().id(), getTagFromNow(), nextDocument);
            LOGGER.info("Logging event " + event);
            repository.save(event);
        } catch (Exception e) {
            LOGGER.error("An error occurred when trying to save events", e);
        }
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

    private int getTagFromNow() {
        return sequenceService.getNextHighLevelEventSequence();
    }
}
