package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.services;

import edu.stanford.protege.webprotege.common.*;
import edu.stanford.protege.webprotege.ipc.EventDispatcher;
import edu.stanford.protege.webprotegeeventshistory.dto.PackagedProjectChangeEvent;
import org.springframework.stereotype.*;

import java.util.List;

@Component
public class ProjectChangeDispatcherImpl implements ProjectChangeDispatcher {

    private final EventDispatcher eventDispatcher;

    public ProjectChangeDispatcherImpl(EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    @Override
    public void dispatchEvent(ProjectId projectId, ProjectEvent event) {
        var packagedProjectChange = new PackagedProjectChangeEvent(projectId, EventId.generate(), List.of(event));
        eventDispatcher.dispatchEvent(packagedProjectChange);
    }
}
