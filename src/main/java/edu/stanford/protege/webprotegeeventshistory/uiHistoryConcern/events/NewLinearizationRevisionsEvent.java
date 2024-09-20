package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.events;

import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.*;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.dto.ProjectChangeForEntity;
import jakarta.validation.constraints.NotNull;

import java.util.List;

import static edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.events.NewLinearizationRevisionsEvent.CHANNEL;


@JsonTypeName(CHANNEL)
public record NewLinearizationRevisionsEvent(
        EventId eventId,
        ProjectId projectId,
        List<ProjectChangeForEntity> changeList
) implements ProjectEvent {
    public final static String CHANNEL = "webprotege.events.projects.linearizations.NewLinearizationRevisionsEvent";

    public static NewLinearizationRevisionsEvent create(EventId eventId,
                                                        ProjectId projectId,
                                                        List<ProjectChangeForEntity> changeList) {
        return new NewLinearizationRevisionsEvent(eventId, projectId, changeList);
    }

    @NotNull
    @Override
    public ProjectId projectId() {
        return projectId;
    }

    @NotNull
    @Override
    public EventId eventId() {
        return eventId;
    }

    public List<ProjectChangeForEntity> getProjectChanges() {
        return changeList;
    }

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}

