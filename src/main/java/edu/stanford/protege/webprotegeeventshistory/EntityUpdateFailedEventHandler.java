package edu.stanford.protege.webprotegeeventshistory;


import edu.stanford.protege.webprotege.ipc.EventHandler;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.repositories.RevisionsEventRepository;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

@Component
public class EntityUpdateFailedEventHandler implements EventHandler<EntityUpdateFailedEvent> {

    private final RevisionsEventRepository repository;

    public EntityUpdateFailedEventHandler(RevisionsEventRepository repository) {
        this.repository = repository;
    }

    @Nonnull
    @Override
    public String getChannelName() {
        return EntityUpdateFailedEvent.CHANNEL;
    }

    @Nonnull
    @Override
    public String getHandlerName() {
        return EntityUpdateFailedEventHandler.class.getName();
    }

    @Override
    public Class<EntityUpdateFailedEvent> getEventClass() {
        return EntityUpdateFailedEvent.class;
    }
    @Override
    public void handleEvent(EntityUpdateFailedEvent event) {
        repository.deleteByChangeRequestIdAndWhoficEntityIri(event.changeRequestId().id(), event.entityIri());
    }
}
