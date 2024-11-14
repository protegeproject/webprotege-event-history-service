package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.handlers;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.common.Response;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.dto.ChangedEntities;

import static edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.handlers.GetChangedEntitiesRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record GetChangedEntitiesResponse(
        @JsonProperty("changedEntities") ChangedEntities changedEntities) implements Response {
    public static GetChangedEntitiesResponse create(ChangedEntities changedEntities) {
        return new GetChangedEntitiesResponse(changedEntities);
    }
}
