package edu.stanford.protege.webprotegeeventshistory.sequence;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface HighLevelSequenceRepository extends MongoRepository<HighLevelEventsSequence, String> {
}
