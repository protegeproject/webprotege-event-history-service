package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record EntityHistorySummary(@JsonProperty("entityUri") String entityUri,
                                   @JsonProperty("projectId") String projectId,
                                   @JsonProperty("changes") List<EntityChange> changes) {
    public static EntityHistorySummary create(String entityUri,
                                              String projectId,
                                              List<EntityChange> changes) {
        return new EntityHistorySummary(entityUri, projectId, changes);
    }
}
