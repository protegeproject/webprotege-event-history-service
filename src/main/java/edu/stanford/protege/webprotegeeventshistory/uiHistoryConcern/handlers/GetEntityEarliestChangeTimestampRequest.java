package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.handlers;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.common.*;
import org.semanticweb.owlapi.model.IRI;

import static edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.handlers.GetEntityEarliestChangeTimestampRequest.CHANNEL;

@JsonTypeName(CHANNEL)
public record GetEntityEarliestChangeTimestampRequest(
        @JsonProperty("projectId") ProjectId projectId,
        @JsonProperty("entityIri") IRI entityIri
) implements Request<GetEntityEarliestChangeTimestampResponse> {

    public static final String CHANNEL = "webprotege.history.GetEntityEarliestChangeTimestamp";

    @JsonCreator
    public static GetEntityEarliestChangeTimestampRequest create(
            @JsonProperty("projectId") ProjectId projectId,
            @JsonProperty("entityIri") IRI entityIri) {
        return new GetEntityEarliestChangeTimestampRequest(projectId, entityIri);
    }

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
