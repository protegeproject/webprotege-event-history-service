package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.handlers;

import edu.stanford.protege.webprotege.ipc.*;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.services.NewRevisionsEventService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;


@Component
public class GetChangedEntitiesCommandHandler implements CommandHandler<GetChangedEntitiesRequest, GetChangedEntitiesResponse> {

    private final NewRevisionsEventService service;

    public GetChangedEntitiesCommandHandler(NewRevisionsEventService service) {
        this.service = service;
    }


    @Nonnull
    @Override
    public String getChannelName() {
        return GetChangedEntitiesRequest.CHANNEL;
    }

    @Override
    public Class<GetChangedEntitiesRequest> getRequestClass() {
        return GetChangedEntitiesRequest.class;
    }

    @Override
    public Mono<GetChangedEntitiesResponse> handleRequest(GetChangedEntitiesRequest request, ExecutionContext executionContext) {
        var entityChanges = service.getChangedEntitiesAfterTimestamp(request.projectId(), request.timestamp());
        return Mono.just(GetChangedEntitiesResponse.create(entityChanges));
    }
}
