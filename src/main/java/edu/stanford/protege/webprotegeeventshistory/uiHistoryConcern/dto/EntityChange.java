package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.stanford.protege.webprotege.common.UserId;

import java.time.LocalDateTime;

public record EntityChange(String changeSummary,
                           UserId userId,
                           @JsonProperty("timestamp") LocalDateTime timestamp) {

    public static EntityChange create(String changeSummary,
                                      UserId userId,
                                      LocalDateTime timestamp){
        return new EntityChange(changeSummary,userId,timestamp);
    }
}
