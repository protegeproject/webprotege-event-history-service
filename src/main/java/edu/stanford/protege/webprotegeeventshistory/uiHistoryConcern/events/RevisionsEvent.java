package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.events;

import com.google.common.base.Objects;
import edu.stanford.protege.webprotege.common.ChangeRequestId;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.dto.ChangeType;
import org.springframework.data.mongodb.core.index.*;
import org.springframework.data.mongodb.core.mapping.*;

@Document(collection = "RevisionsEvents")
public record RevisionsEvent(
        @Indexed(name = "revisionEventProjectId")
        @Field("projectId") String projectId,
        @Indexed(name = "revisionEventEntityIri")
        String whoficEntityIri,
        ChangeType changeType,
        @Indexed(name = "timestamp", direction = IndexDirection.DESCENDING) long timestamp,

        String changeRequestId,
        org.bson.Document projectChange
) {

    public static final String WHOFIC_ENTITY_IRI = "whoficEntityIri";
    public static final String PROJECT_ID = "projectId";
    public static final String TIMESTAMP = "timestamp";
    public static final String CHANGE_TYPE = "changeType";
    public static final String PROJECT_CHANGE = "projectChange";

    public static RevisionsEvent create(ProjectId projectId,
                                        String whoficEntityIri,
                                        ChangeType changeType,
                                        long timestamp,
                                        org.bson.Document projectChange,
                                        ChangeRequestId changeRequestId) {
        return new RevisionsEvent(projectId.id(), whoficEntityIri, changeType, timestamp, changeRequestId != null ? changeRequestId.id() : null, projectChange);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RevisionsEvent that = (RevisionsEvent) o;
        return timestamp == that.timestamp &&
                Objects.equal(projectId, that.projectId) &&
                Objects.equal(whoficEntityIri, that.whoficEntityIri) &&
                Objects.equal(changeType, that.changeType) &&
                Objects.equal(projectChange, that.projectChange);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(projectId, whoficEntityIri, changeType, timestamp, projectChange);
    }
}
