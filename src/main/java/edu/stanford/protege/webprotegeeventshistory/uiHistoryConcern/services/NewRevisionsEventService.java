package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.services;

import edu.stanford.protege.webprotege.change.ProjectChange;
import edu.stanford.protege.webprotege.common.*;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.dto.ChangedEntities;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.events.NewRevisionsEvent;
import org.semanticweb.owlapi.model.OWLEntity;

import java.sql.Timestamp;
import java.util.Optional;

public interface NewRevisionsEventService {

    void registerEvent(NewRevisionsEvent newLinRevEvent);

    Page<ProjectChange> fetchPaginatedProjectChanges(ProjectId projectId, Optional<OWLEntity> subject, int pageNumber, int pageSize);

    ChangedEntities getChangedEntitiesAfterTimestamp(ProjectId projectId, Timestamp timestamp);
}
