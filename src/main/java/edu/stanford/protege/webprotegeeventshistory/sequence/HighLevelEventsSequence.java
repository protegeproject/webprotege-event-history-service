package edu.stanford.protege.webprotegeeventshistory.sequence;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "HighLevelEventSequence")
public class HighLevelEventsSequence {
    @Id
    private String id;
    private int seq;

    public HighLevelEventsSequence(int seq) {
        this.seq = seq;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }
}
