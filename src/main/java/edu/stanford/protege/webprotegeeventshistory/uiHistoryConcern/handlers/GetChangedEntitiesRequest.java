package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.handlers;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.common.*;

@JsonTypeName(GetChangedEntitiesRequest.CHANNEL)
public record GetChangedEntitiesRequest(
        @JsonProperty("projectId") ProjectId projectId,
        @JsonProperty("timestamp") long timestamp
) implements Request<GetChangedEntitiesResponse> {

    public static final String CHANNEL = "webprotege.history.GetChangedEntities";

    @Override
    public String getChannel() {
        return CHANNEL;
    }

    public static GetChangedEntitiesRequest create(ProjectId projectId,
                                                   long timestamp) {
        return new GetChangedEntitiesRequest(projectId, timestamp);
    }
}
