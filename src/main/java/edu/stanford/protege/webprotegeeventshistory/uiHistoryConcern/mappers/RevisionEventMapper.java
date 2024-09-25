package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.protege.webprotege.change.ProjectChange;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.events.*;
import org.bson.Document;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

@Component
public class RevisionEventMapper {

    private final ObjectMapper objectMapper;

    public RevisionEventMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<RevisionsEvent> mapNewRevisionsEventToRevisionsEvents(NewRevisionsEvent newRevisionsEvent) {

        List<RevisionsEvent> revisionsEvents = newRevisionsEvent.changes().stream()
                .flatMap(projectChangeForEntity -> {
                    String whoficIri = projectChangeForEntity.whoficEntityIri();
                    ProjectChange projectChange = projectChangeForEntity.projectChange();
                    long timestamp = projectChange.getTimestamp();
                    var projectChangeDocument = objectMapper.convertValue(projectChange, Document.class);

                    return Stream.of(RevisionsEvent.create(newRevisionsEvent.projectId(), whoficIri, timestamp, projectChangeDocument));
                })
                .toList();

        return revisionsEvents;
    }
}
