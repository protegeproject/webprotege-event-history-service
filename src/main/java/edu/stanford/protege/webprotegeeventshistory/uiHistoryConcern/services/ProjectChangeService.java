package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.services;

import edu.stanford.protege.webprotege.common.*;

public interface ProjectChangeService {
    void emitProjectChange(ProjectId projectId, ProjectEvent event);
}
