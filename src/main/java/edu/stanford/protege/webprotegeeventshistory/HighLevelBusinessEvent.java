package edu.stanford.protege.webprotegeeventshistory;



import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "HighLevelEvents")
@CompoundIndex(name = "projectId_timeStamp_idx", def = "{'projectId': 1, 'timeStamp': 1}")
public record HighLevelBusinessEvent(@Id String eventId,String projectId, int timeStamp, org.bson.Document projectEvent) {
}
