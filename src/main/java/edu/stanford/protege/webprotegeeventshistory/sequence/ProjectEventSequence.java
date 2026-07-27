package edu.stanford.protege.webprotegeeventshistory.sequence;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * The per-project sequence counter. One document per project, its {@code _id} being the
 * project id and {@code seq} the highest sequence ordinal assigned so far for that project.
 * The counter is advanced atomically server-side (single-document {@code $inc}) by
 * {@link ProjectSequenceService}.
 */
@Document(collection = ProjectEventSequence.COLLECTION)
public class ProjectEventSequence {

    public static final String COLLECTION = "ProjectEventSequence";

    public static final String SEQ_FIELD = "seq";

    @Id
    private String projectId;

    private int seq;

    public ProjectEventSequence() {
    }

    public ProjectEventSequence(String projectId, int seq) {
        this.projectId = projectId;
        this.seq = seq;
    }

    public String getProjectId() {
        return projectId;
    }

    public int getSeq() {
        return seq;
    }
}
