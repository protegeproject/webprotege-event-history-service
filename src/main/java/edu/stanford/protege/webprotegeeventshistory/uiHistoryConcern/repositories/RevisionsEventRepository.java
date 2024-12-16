package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.repositories;

import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.events.RevisionsEvent;
import org.springframework.data.mongodb.repository.*;

import java.util.List;

public interface RevisionsEventRepository extends MongoRepository<RevisionsEvent, String> {
    @Query("{ 'projectId': ?0, 'timestamp': { $gt: ?1 } }")
    List<RevisionsEvent> findByProjectIdAndTimestampAfter(String projectId, long timestamp);
}
