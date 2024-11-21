package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.handlers;

import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.ipc.*;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.dto.EntityHistorySummary;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.services.NewRevisionsEventService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

@Component
public class GetEntityHistorySummaryCommandHandler implements CommandHandler<GetEntityHistorySummaryRequest, GetEntityHistorySummaryResponse> {

    private final NewRevisionsEventService service;

    public GetEntityHistorySummaryCommandHandler(NewRevisionsEventService service) {
        this.service = service;
    }


    @Nonnull
    @Override
    public String getChannelName() {
        return GetEntityHistorySummaryRequest.CHANNEL;
    }

    @Override
    public Class<GetEntityHistorySummaryRequest> getRequestClass() {
        return GetEntityHistorySummaryRequest.class;
    }

    @Override
    public Mono<GetEntityHistorySummaryResponse> handleRequest(GetEntityHistorySummaryRequest request, ExecutionContext executionContext) {
        EntityHistorySummary entityHistorySummary = service.getEntityHistorySummary(ProjectId.valueOf(request.projectId()), request.entityIri());
        return Mono.just(GetEntityHistorySummaryResponse.create(entityHistorySummary));
    }
}
