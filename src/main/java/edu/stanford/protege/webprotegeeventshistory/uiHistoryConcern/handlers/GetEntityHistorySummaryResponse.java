package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.handlers;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.common.Response;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.dto.EntityHistorySummary;

import static edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.handlers.GetEntityHistorySummaryRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record GetEntityHistorySummaryResponse(
        @JsonProperty("entityHistorySummary") EntityHistorySummary entityHistorySummary
) implements Response {
    public static GetEntityHistorySummaryResponse create(EntityHistorySummary entityHistorySummary) {
        return new GetEntityHistorySummaryResponse(entityHistorySummary);
    }
}
