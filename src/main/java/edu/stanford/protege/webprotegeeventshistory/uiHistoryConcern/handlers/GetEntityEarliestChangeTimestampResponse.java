package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.handlers;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.common.Response;

import static edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.handlers.GetEntityEarliestChangeTimestampRequest.CHANNEL;


@JsonTypeName(CHANNEL)
public record GetEntityEarliestChangeTimestampResponse(
        @JsonProperty("earliestTimestamp") Long earliestTimestamp
) implements Response {

    @JsonCreator
    public static GetEntityEarliestChangeTimestampResponse create(@JsonProperty("earliestTimestamp") Long earliestTimestamp) {
        return new GetEntityEarliestChangeTimestampResponse(earliestTimestamp);
    }
}
