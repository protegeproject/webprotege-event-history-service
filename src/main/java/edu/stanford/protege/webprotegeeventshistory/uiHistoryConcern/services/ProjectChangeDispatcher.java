package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.services;

import edu.stanford.protege.webprotege.common.*;

public interface ProjectChangeDispatcher {
    void dispatchEvent(ProjectId projectId, ProjectEvent event);
}
