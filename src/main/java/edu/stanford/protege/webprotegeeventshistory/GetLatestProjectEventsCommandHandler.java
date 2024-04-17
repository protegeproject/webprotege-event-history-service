package edu.stanford.protege.webprotegeeventshistory;

import edu.stanford.protege.webprotege.ipc.CommandHandler;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotegeeventshistory.dto.ProjectEventsQueryRequest;
import edu.stanford.protege.webprotegeeventshistory.dto.ProjectEventsQueryResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;


@Component
public class GetLatestProjectEventsCommandHandler implements CommandHandler<ProjectEventsQueryRequest, ProjectEventsQueryResponse> {

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

    @Override
    public Mono<ProjectEventsQueryResponse> handleRequest(ProjectEventsQueryRequest request, ExecutionContext executionContext) {
        var response = service.fetchEvents(request);
        return Mono.just(response);
    }
}
