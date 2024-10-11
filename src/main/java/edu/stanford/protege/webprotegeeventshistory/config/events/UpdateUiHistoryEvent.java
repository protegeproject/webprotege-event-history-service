package edu.stanford.protege.webprotegeeventshistory.config.events;

import com.fasterxml.jackson.annotation.*;
import com.google.common.base.Objects;
import edu.stanford.protege.webprotege.common.*;

import java.util.Set;

@JsonTypeName(UpdateUiHistoryEvent.CHANNEL)
public record UpdateUiHistoryEvent(EventId eventId,
                                   ProjectId projectId,
                                   Set<String> affectedEntityIris) implements ProjectEvent {

    @JsonCreator
    public static UpdateUiHistoryEvent create(EventId eventId,
                                              ProjectId projectId,
                                              Set<String> afectedEntityIris
    ) {
        return new UpdateUiHistoryEvent(eventId, projectId, afectedEntityIris);
    }

    public static final String CHANNEL = "webprotege.events.projects.uiHistory.UpdateUiHistoryEvent";

    public ProjectId projectId() {
        return this.projectId;
    }

    @Override
    public String getChannel() {
        return CHANNEL;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdateUiHistoryEvent that = (UpdateUiHistoryEvent) o;
        return Objects.equal(eventId, that.eventId) && Objects.equal(projectId, that.projectId) && Objects.equal(affectedEntityIris, that.affectedEntityIris);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(eventId, projectId, affectedEntityIris);
    }

    @Override
    public String toString() {
        return "UpdateUiHistoryEvent{" +
                "eventId=" + eventId +
                ", projectId=" + projectId +
                ", subjects=" + affectedEntityIris +
                '}';
    }

}
