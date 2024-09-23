package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.handlers;

import edu.stanford.protege.webprotege.ipc.*;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.services.NewRevisionsEventService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;


@Component
public class GetProjectChangesForHistoryViewCommandHandler implements CommandHandler<ProjectChangesForHistoryViewRequest, ProjectChangesForHistoryViewResponse> {

    private final NewRevisionsEventService service;

    public GetProjectChangesForHistoryViewCommandHandler(NewRevisionsEventService service) {
        this.service = service;
    }


    @Nonnull
    @Override
    public String getChannelName() {
        return ProjectChangesForHistoryViewRequest.CHANNEL;
    }

    @Override
    public Class<ProjectChangesForHistoryViewRequest> getRequestClass() {
        return ProjectChangesForHistoryViewRequest.class;
    }

    @Override
    public Mono<ProjectChangesForHistoryViewResponse> handleRequest(ProjectChangesForHistoryViewRequest request, ExecutionContext executionContext) {
        int pageNumber = request.pageRequest().getPageNumber()-1;
        int pageSize = request.pageRequest().getPageSize();
        var changes = service.fetchPaginatedProjectChanges(request.projectId(), request.subject(), pageNumber, pageSize);
        return Mono.just(ProjectChangesForHistoryViewResponse.create(changes));
    }
}
