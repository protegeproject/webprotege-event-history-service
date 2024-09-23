package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.events;

import com.google.common.base.Objects;
import edu.stanford.protege.webprotege.common.ProjectId;
import org.springframework.data.mongodb.core.index.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "RevisionsEvents")
public record RevisionsEvent(
        ProjectId projectId,
        String whoficEntityIri,
        @Indexed(name = "timestamp", direction = IndexDirection.DESCENDING) long timestamp,
        org.bson.Document projectChange
) {

    public static final String WHOFIC_ENTITY_IRI = "whoficEntityIri";
    public static final String PROJECT_ID = "projectId";
    public static final String TIMESTAMP = "timestamp";
    public static final String PROJECT_CHANGE = "projectChange";

    public static RevisionsEvent create(ProjectId projectId,
                                        String whoficEntityIri,
                                        long timestamp,
                                        org.bson.Document projectChange) {
        return new RevisionsEvent(projectId, whoficEntityIri, timestamp, projectChange);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RevisionsEvent that = (RevisionsEvent) o;
        return timestamp == that.timestamp &&
                Objects.equal(projectId, that.projectId) &&
                Objects.equal(whoficEntityIri, that.whoficEntityIri) &&
                Objects.equal(projectChange, that.projectChange);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(projectId, whoficEntityIri, timestamp, projectChange);
    }
}
