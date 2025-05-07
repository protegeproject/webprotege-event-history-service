package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.repositories;

import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.events.RevisionsEvent;
import org.semanticweb.owlapi.model.EntityType;
import org.springframework.data.mongodb.repository.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RevisionsEventRepository extends MongoRepository<RevisionsEvent, String> {
    @Query("{ 'projectId': ?0, 'timestamp': { $gt: ?1 } }")
    List<RevisionsEvent> findByProjectIdAndTimestampAfter(String projectId, long timestamp);

    List<RevisionsEvent> findByProjectIdAndWhoficEntityIriOrderByTimestampDesc(ProjectId projectId, String whoficEntityIri);
    List<RevisionsEvent> findByProjectIdAndEntityTypeAndTimestampAfter(ProjectId projectId, EntityType entityType, long timestamp);
    @Transactional
    void deleteByChangeRequestIdAndWhoficEntityIri(String changeRequestId, String whoficEntityIri);
}
