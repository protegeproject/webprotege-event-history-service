package edu.stanford.protege.webprotegeeventshistory.config.events;

import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.*;

@JsonTypeName(ProjectLinearizationChangedEvent.CHANNEL)
public record ProjectLinearizationChangedEvent(EventId eventId,
                                               ProjectId projectId) implements ProjectEvent {
    public static final String CHANNEL = "webprotege.linearization.ProjectLinearizationChangedEvent";

    public String getChannel() {
        return CHANNEL;
    }

    public EventId eventId() {
        return this.eventId;
    }

    public ProjectId projectId() {
        return this.projectId;
    }

}
