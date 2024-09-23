package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.handlers;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.change.ProjectChange;
import edu.stanford.protege.webprotege.common.*;

import java.util.Objects;

import static edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.handlers.ProjectChangesForHistoryViewRequest.CHANNEL;


@JsonTypeName(CHANNEL)
public record ProjectChangesForHistoryViewResponse(Page<ProjectChange> changes) implements Response {
    public ProjectChangesForHistoryViewResponse(@JsonProperty("projectChanges") Page<ProjectChange> changes) {
        Objects.requireNonNull(changes);
        this.changes = changes;
    }

    public static ProjectChangesForHistoryViewResponse create(Page<ProjectChange> changes){
        return new ProjectChangesForHistoryViewResponse(changes);
    }

    @JsonProperty("projectChanges")
    public Page<ProjectChange> changes() {
        return this.changes;
    }
}
