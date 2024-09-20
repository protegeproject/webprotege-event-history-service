package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.mappers;

import edu.stanford.protege.webprotege.change.ProjectChange;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.dto.ProjectChangeForEntity;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.events.RevisionsEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

@Component
public class RevisionEventMapper {
    public List<RevisionsEvent> mapNewLinearizationRevisionsEventToRevisionsEvents(ProjectId projectId, List<ProjectChangeForEntity> changeList) {

        List<RevisionsEvent> revisionsEvents = changeList.stream()
                .flatMap(projectChangeForEntity -> {
                    String whoficIri = projectChangeForEntity.whoficEntityIri();
                    ProjectChange projectChange = projectChangeForEntity.projectChange();
                    long timestamp = projectChange.getTimestamp();
                    return Stream.of(RevisionsEvent.create(projectId,whoficIri,timestamp,projectChange));
                })
                .toList();

        return revisionsEvents;
    }
}
