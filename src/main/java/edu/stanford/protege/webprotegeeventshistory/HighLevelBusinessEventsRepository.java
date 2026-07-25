package edu.stanford.protege.webprotegeeventshistory;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface HighLevelBusinessEventsRepository  extends MongoRepository<HighLevelBusinessEvent, String> {

    /**
     * Returns the events for a project whose ordinal ({@code timeStamp}) is greater than
     * {@code timeStamp}. The {@link Pageable} bounds the result so a single response can never
     * replay the whole archive; pass an ascending sort on {@code timeStamp} so the last element is
     * the end of the returned window. Served by the {@code (projectId, timeStamp)} compound index.
     */
    List<HighLevelBusinessEvent> findByProjectIdAndTimeStampGreaterThan(String projectId, int timeStamp, Pageable pageable);

}
