package edu.stanford.protege.webprotegeeventshistory.dto;


import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.common.Request;
import jakarta.validation.constraints.NotNull;

public class ProjectEventsQueryRequest implements Request<ProjectEventsQueryResponse> {

    public final static String CHANNEL = "webprotege.hierarchies.GetProjectEvents";

    public EventTag sinceTag;

    /**
     * When {@code true}, the client only wants the current head position so it can start listening
     * for live updates without downloading any history (#301). The service skips the archive and
     * returns an empty window anchored at the current sequence head. Absent in the JSON of older
     * clients, where Jackson leaves the primitive at {@code false}, preserving today's behaviour.
     */
    public boolean latestOnly;


    @NotNull
    public ProjectId projectId;

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
