package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.handlers;

import edu.stanford.protege.webprotege.common.EventId;
import edu.stanford.protege.webprotege.ipc.EventHandler;
import edu.stanford.protege.webprotegeeventshistory.config.events.UpdateUiHistoryEvent;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.events.NewRevisionsEvent;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.services.NewRevisionsEventService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.*;

@Component
public class NewRevisionsEventHandler implements EventHandler<NewRevisionsEvent> {

    private final NewRevisionsEventService newRevisionsEventService;
    private final ApplicationEventPublisher eventPublisher;

    public NewRevisionsEventHandler(NewRevisionsEventService newRevisionsEventService,
                                    ApplicationEventPublisher eventPublisher) {
        this.newRevisionsEventService = newRevisionsEventService;
        this.eventPublisher = eventPublisher;
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
        newRevisionsEventService.registerEvent(event);
        Set<String> entitySubjects = new HashSet<>();
        event.changes().forEach(change -> {
            entitySubjects.add(change.whoficEntityIri());
        });

        eventPublisher.publishEvent(UpdateUiHistoryEvent.create(EventId.generate(), event.projectId(), entitySubjects));
    }
}
