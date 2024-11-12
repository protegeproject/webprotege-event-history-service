package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.services;

import edu.stanford.protege.webprotege.change.ProjectChange;
import edu.stanford.protege.webprotege.common.*;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.events.*;
import org.semanticweb.owlapi.model.OWLEntity;

import java.util.Optional;

public interface NewRevisionsEventService {

    void registerEvent(NewRevisionsEvent newLinRevEvent);

    Page<ProjectChange> fetchPaginatedProjectChanges(ProjectId projectId, Optional<OWLEntity> subject, int pageNumber, int pageSize);
}
