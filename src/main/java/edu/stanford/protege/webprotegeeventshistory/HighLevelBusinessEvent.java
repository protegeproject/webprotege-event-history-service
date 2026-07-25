package edu.stanford.protege.webprotegeeventshistory;



import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "HighLevelEvents")
@CompoundIndex(name = "projectId_timeStamp_idx", def = "{'projectId': 1, 'timeStamp': 1}")
// NB: timeStamp is the per-project sequence ordinal (an EventTag value), not a wall clock.
// eventId is the Mongo @Id, so re-saving the same event upserts rather than duplicates.
public record HighLevelBusinessEvent(@Id String eventId,String projectId, int timeStamp, org.bson.Document projectEvent) {
}
