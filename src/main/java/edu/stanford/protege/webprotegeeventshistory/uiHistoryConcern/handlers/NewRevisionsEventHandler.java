package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.handlers;

import edu.stanford.protege.webprotege.ipc.EventHandler;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.events.NewRevisionsEvent;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.services.NewRevisionsEventService;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
public class NewRevisionsEventHandler implements EventHandler<NewRevisionsEvent> {

    private final NewRevisionsEventService linRevisionsEventService;

    public NewRevisionsEventHandler(NewRevisionsEventService linRevisionsEventService) {
        this.linRevisionsEventService = linRevisionsEventService;
    }

    @Nonnull
    @Override
    public String getChannelName() {
        return NewRevisionsEvent.CHANNEL;
    }

    @Nonnull
    @Override
    public String getHandlerName() {
        return this.getClass().getName();
    }

    @Override
    public Class<NewRevisionsEvent> getEventClass() {
        return NewRevisionsEvent.class;
    }

    @Override
    public void handleEvent(NewRevisionsEvent event) {
        linRevisionsEventService.registerEvent(event);
    }
}
