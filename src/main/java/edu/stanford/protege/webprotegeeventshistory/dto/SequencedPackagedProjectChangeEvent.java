package edu.stanford.protege.webprotegeeventshistory.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.EventId;
import edu.stanford.protege.webprotege.common.ProjectEvent;
import edu.stanford.protege.webprotege.common.ProjectId;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * A {@link PackagedProjectChangeEvent} enriched with the per-project sequence ordinal assigned to
 * the bundle when it was durably archived. Published post-persistence on {@link #CHANNEL} so the
 * gateway can push it with truthful event tags ({@code startTag = sequenceNumber - 1},
 * {@code endTag = sequenceNumber}); because it is emitted only after the archive write, anything
 * pushed is already fetchable via the pull path.
 * <p>
 * The field names are the wire contract consumed by the gateway's mirror DTO. {@code projectId},
 * {@code eventId} and {@code projectEvents} serialize exactly as in {@link PackagedProjectChangeEvent}.
 */
@JsonTypeName(SequencedPackagedProjectChangeEvent.CHANNEL)
public record SequencedPackagedProjectChangeEvent(ProjectId projectId,
                                                  EventId eventId,
                                                  int sequenceNumber,
                                                  List<ProjectEvent> projectEvents) implements ProjectEvent {

    public static final String CHANNEL = "webprotege.events.projects.SequencedPackagedProjectChange";

    @Nonnull
    @Override
    public ProjectId projectId() {
        return projectId;
    }

    @Nonnull
    @Override
    public EventId eventId() {
        return eventId;
    }

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
