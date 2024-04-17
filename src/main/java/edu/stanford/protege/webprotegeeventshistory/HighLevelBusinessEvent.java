package edu.stanford.protege.webprotegeeventshistory;



import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "HighLevelEvents")
public record HighLevelBusinessEvent(@Id String eventId,String projectId, int timeStamp, org.bson.Document projectEvent) {
}
