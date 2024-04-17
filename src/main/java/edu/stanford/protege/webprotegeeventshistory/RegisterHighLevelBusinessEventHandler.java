package edu.stanford.protege.webprotegeeventshistory;

import edu.stanford.protege.webprotege.ipc.EventHandler;
import edu.stanford.protege.webprotegeeventshistory.dto.PackagedProjectChangeEvent;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;


@Component
public class RegisterHighLevelBusinessEventHandler implements EventHandler<PackagedProjectChangeEvent> {


    private final HighLevelBusinessEventsService service;



    public RegisterHighLevelBusinessEventHandler(HighLevelBusinessEventsService service) {
        this.service = service;
    }


    @Nonnull
    @Override
    public String getChannelName() {
        return "webprotege.events.projects.PackagedProjectChange";
    }

    @Nonnull
    @Override
    public String getHandlerName() {
        return this.getClass().getName();
    }

    @Override
    public Class<PackagedProjectChangeEvent> getEventClass() {
        return PackagedProjectChangeEvent.class;
    }

    @Override
    public void handleEvent(PackagedProjectChangeEvent event) {
        service.registerEvent(event);
    }
}
