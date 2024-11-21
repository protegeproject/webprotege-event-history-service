package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.dto;

import java.util.List;

public record EntityHistorySummary(List<EntityChange> changes) {
    public static EntityHistorySummary create(List<EntityChange> changes) {
        return new EntityHistorySummary(changes);
    }
}
