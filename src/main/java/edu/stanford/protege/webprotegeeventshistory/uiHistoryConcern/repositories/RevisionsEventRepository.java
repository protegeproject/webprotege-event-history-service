package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.repositories;

import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.events.RevisionsEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RevisionsEventRepository extends MongoRepository<RevisionsEvent,String> {
}
