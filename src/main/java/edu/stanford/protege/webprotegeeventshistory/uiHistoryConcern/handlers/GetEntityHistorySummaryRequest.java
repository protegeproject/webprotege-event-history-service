package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.handlers;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.common.Request;

@JsonTypeName(GetEntityHistorySummaryRequest.CHANNEL)
public record GetEntityHistorySummaryRequest(
        @JsonProperty("projectId") String projectId,
        @JsonProperty("entityIri") String entityIri
) implements Request<GetEntityHistorySummaryResponse> {

    public static final String CHANNEL = "webprotege.history.GetEntityHistorySummary";

    public static GetEntityHistorySummaryRequest create(String projectId,
                                                        String entityIri) {
        return new GetEntityHistorySummaryRequest(projectId, entityIri);
    }

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
