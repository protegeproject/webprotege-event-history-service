package edu.stanford.protege.webprotegeeventshistory;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface HighLevelBusinessEventsRepository  extends MongoRepository<HighLevelBusinessEvent, String> {

    List<HighLevelBusinessEvent> findByTimeStampGreaterThanAndProjectId(int timestamp, String projectId);

}
