package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.handlers;

import edu.stanford.protege.webprotege.ipc.EventHandler;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.events.NewLinearizationRevisionsEvent;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.services.NewRevisionsEventService;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
public class NewLinearizationRevisionsEventHandler implements EventHandler<NewLinearizationRevisionsEvent> {

    private final NewRevisionsEventService linRevisionsEventService;

    public NewLinearizationRevisionsEventHandler(NewRevisionsEventService linRevisionsEventService) {
        this.linRevisionsEventService = linRevisionsEventService;
    }

    @Nonnull
    @Override
    public String getChannelName() {
        return NewLinearizationRevisionsEvent.CHANNEL;
    }

    @Nonnull
    @Override
    public String getHandlerName() {
        return this.getClass().getName();
    }

    @Override
    public Class<NewLinearizationRevisionsEvent> getEventClass() {
        return NewLinearizationRevisionsEvent.class;
    }

    @Override
    public void handleEvent(NewLinearizationRevisionsEvent event) {
        linRevisionsEventService.registerEvent(event);
    }
}
