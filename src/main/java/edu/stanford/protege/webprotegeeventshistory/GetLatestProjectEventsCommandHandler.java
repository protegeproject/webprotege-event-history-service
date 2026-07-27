package edu.stanford.protege.webprotegeeventshistory;

import edu.stanford.protege.webprotege.authorization.BasicCapability;
import edu.stanford.protege.webprotege.authorization.Capability;
import edu.stanford.protege.webprotege.authorization.ProjectResource;
import edu.stanford.protege.webprotege.authorization.Resource;
import edu.stanford.protege.webprotege.ipc.AuthorizedCommandHandler;
import edu.stanford.protege.webprotege.ipc.CommandExecutionException;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotegeeventshistory.dto.ProjectEventsQueryRequest;
import edu.stanford.protege.webprotegeeventshistory.dto.ProjectEventsQueryResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;


@Component
public class GetLatestProjectEventsCommandHandler implements AuthorizedCommandHandler<ProjectEventsQueryRequest, ProjectEventsQueryResponse> {

    private static final Capability VIEW_PROJECT = new BasicCapability("ViewProject");

    private final HighLevelBusinessEventsService service;

    public GetLatestProjectEventsCommandHandler(HighLevelBusinessEventsService service) {
        this.service = service;
    }


    @Nonnull
    @Override
    public String getChannelName() {
        return ProjectEventsQueryRequest.CHANNEL;
    }

    @Override
    public Class<ProjectEventsQueryRequest> getRequestClass() {
        return ProjectEventsQueryRequest.class;
    }

    @Nonnull
    @Override
    public Resource getTargetResource(ProjectEventsQueryRequest request) {
        if (request.projectId == null) {
            throw CommandExecutionException.of(HttpStatus.BAD_REQUEST, "projectId must not be null");
        }
        return ProjectResource.forProject(request.projectId);
    }

    @Nonnull
    @Override
    public Collection<Capability> getRequiredCapabilities() {
        return List.of(VIEW_PROJECT);
    }

    @Override
    public Mono<ProjectEventsQueryResponse> handleRequest(ProjectEventsQueryRequest request, ExecutionContext executionContext) {
        var response = service.fetchEvents(request);
        return Mono.just(response);
    }
}
