package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.dto;

import edu.stanford.protege.webprotege.change.ProjectChange;

public record ProjectChangeForEntity(String whoficEntityIri,
                                     ProjectChange projectChange) {

    public static ProjectChangeForEntity create(String whoficEntityIri,
                                                ProjectChange projectChange) {
        return new ProjectChangeForEntity(whoficEntityIri, projectChange);
    }
}
