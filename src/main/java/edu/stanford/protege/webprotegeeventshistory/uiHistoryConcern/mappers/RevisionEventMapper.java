package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.protege.webprotege.change.ProjectChange;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.dto.ProjectChangeForEntity;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.events.RevisionsEvent;
import org.bson.Document;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Stream;

@Component
public class RevisionEventMapper {

    private final ObjectMapper objectMapper;

    public RevisionEventMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<RevisionsEvent> mapNewLinearizationRevisionsEventToRevisionsEvents(ProjectId projectId, Set<ProjectChangeForEntity> changes) {

        List<RevisionsEvent> revisionsEvents = changes.stream()
                .flatMap(projectChangeForEntity -> {
                    String whoficIri = projectChangeForEntity.whoficEntityIri();
                    ProjectChange projectChange = projectChangeForEntity.projectChange();
                    long timestamp = projectChange.getTimestamp();
                    var projectChangeDocument = objectMapper.convertValue(projectChange, Document.class);

                    return Stream.of(RevisionsEvent.create(projectId,whoficIri,timestamp,projectChangeDocument));
                })
                .toList();

        return revisionsEvents;
    }
}
