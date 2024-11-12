package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.services;

import edu.stanford.protege.webprotege.common.*;
import edu.stanford.protege.webprotegeeventshistory.config.events.UpdateUiHistoryEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ProjectChangeServiceImpl implements ProjectChangeService {

    private final ProjectChangeDispatcher changeDispatcher;

    public ProjectChangeServiceImpl(ProjectChangeDispatcher changeDispatcher) {
        this.changeDispatcher = changeDispatcher;
    }

    @Async
    @EventListener
    public void handleUiHistoryEvent(UpdateUiHistoryEvent event) {
        // Emit the project change when the event is handled
        emitProjectChange(event.projectId(), UpdateUiHistoryEvent.create(EventId.generate(), event.projectId(), event.affectedEntityIris()));
    }

    @Override
    public void emitProjectChange(ProjectId projectId, ProjectEvent event) {
        this.changeDispatcher.dispatchEvent(projectId, event);
    }
}
