package edu.stanford.protege.webprotegeeventshistory.dto;


import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.common.Request;
import jakarta.validation.constraints.NotNull;

public class ProjectEventsQueryRequest implements Request<ProjectEventsQueryResponse> {

    public final static String CHANNEL = "webprotege.hierarchies.GetProjectEvents";

    public EventTag sinceTag;


    @NotNull
    public ProjectId projectId;

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}
