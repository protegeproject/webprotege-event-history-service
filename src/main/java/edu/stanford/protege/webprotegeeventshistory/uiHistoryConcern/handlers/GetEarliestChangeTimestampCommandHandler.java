package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.handlers;

import edu.stanford.protege.webprotege.ipc.*;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.services.NewRevisionsEventService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;


@Component
public class GetEarliestChangeTimestampCommandHandler implements CommandHandler<GetEntityEarliestChangeTimestampRequest, GetEntityEarliestChangeTimestampResponse> {

    private final NewRevisionsEventService service;

    public GetEarliestChangeTimestampCommandHandler(NewRevisionsEventService service) {
        this.service = service;
    }


    @Nonnull
    @Override
    public String getChannelName() {
        return GetEntityEarliestChangeTimestampRequest.CHANNEL;
    }

    @Override
    public Class<GetEntityEarliestChangeTimestampRequest> getRequestClass() {
        return GetEntityEarliestChangeTimestampRequest.class;
    }

    @Override
    public Mono<GetEntityEarliestChangeTimestampResponse> handleRequest(GetEntityEarliestChangeTimestampRequest request, ExecutionContext executionContext) {
        var earliestChangeTimestamp = service.getEntityEarliestChangeTimestamp(request.projectId(), request.entityIri().toString());
        return Mono.just(GetEntityEarliestChangeTimestampResponse.create(earliestChangeTimestamp));
    }
}
