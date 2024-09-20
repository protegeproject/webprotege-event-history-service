package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.services;

import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.events.NewLinearizationRevisionsEvent;

public interface NewRevisionsEventService {

    void registerEvent(NewLinearizationRevisionsEvent newLinRevEvent);
}
