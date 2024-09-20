package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.events;

import edu.stanford.protege.webprotege.change.ProjectChange;
import edu.stanford.protege.webprotege.common.ProjectId;
import org.springframework.data.mongodb.core.index.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "RevisionsEvents")
public record RevisionsEvent(
        ProjectId projectId,
        String whoficEntityIri,
        @Indexed(name = "timestamp", direction = IndexDirection.DESCENDING) long timestamp,
        ProjectChange projectChange
) {

    public static RevisionsEvent create(ProjectId projectId,
                                 String whoficEntityIri,
                                 long timestamp,
                                 ProjectChange projectChange) {
        return new RevisionsEvent(projectId, whoficEntityIri, timestamp, projectChange);
    }
}
