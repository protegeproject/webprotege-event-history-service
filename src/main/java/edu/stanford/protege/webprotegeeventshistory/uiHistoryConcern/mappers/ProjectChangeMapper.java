package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.protege.webprotege.change.ProjectChange;
import org.bson.Document;
import org.springframework.stereotype.Component;

@Component
public class ProjectChangeMapper {

    private final ObjectMapper objectMapper;

    public ProjectChangeMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ProjectChange mapProjectChangeDocumentToProjectChange(Document projectChange) {
        return objectMapper.convertValue(projectChange, ProjectChange.class);
    }
}
