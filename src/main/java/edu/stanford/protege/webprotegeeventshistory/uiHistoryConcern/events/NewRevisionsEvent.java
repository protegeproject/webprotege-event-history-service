package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.events;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.common.*;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.dto.ProjectChangeForEntity;

import java.util.Set;

import static edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.events.NewRevisionsEvent.CHANNEL;


@JsonTypeName(CHANNEL)
public record NewRevisionsEvent(
        EventId eventId,
        ProjectId projectId,
        Set<ProjectChangeForEntity> changes
) implements ProjectEvent {
    public final static String CHANNEL = "webprotege.events.projects.uiHistory.NewRevisionsEvent";

    @JsonCreator
    public static NewRevisionsEvent create(@JsonProperty("eventId") EventId eventId,
                                           @JsonProperty("projectId") ProjectId projectId,
                                           @JsonProperty("changes") Set<ProjectChangeForEntity> changes) {
        return new NewRevisionsEvent(eventId, projectId, changes);
    }

    @Override
    @JsonProperty("eventId")
    public ProjectId projectId() {
        return projectId;
    }

    @Override
    @JsonProperty("projectId")
    public EventId eventId() {
        return eventId;
    }

    @JsonProperty("changes")
    public Set<ProjectChangeForEntity> changes() {
        return changes;
    }

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}

