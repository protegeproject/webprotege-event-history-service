package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.handlers;

import com.fasterxml.jackson.annotation.*;
import edu.stanford.protege.webprotege.common.*;
import org.semanticweb.owlapi.model.OWLEntity;

import java.util.Optional;

import static edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.handlers.ProjectChangesForHistoryViewRequest.CHANNEL;


@JsonTypeName(CHANNEL)
public record ProjectChangesForHistoryViewRequest(
        @JsonProperty("projectId") ProjectId projectId,
        @JsonProperty("subject") Optional<OWLEntity> subject,
        @JsonProperty("pageRequest") PageRequest pageRequest
) implements ProjectRequest<ProjectChangesForHistoryViewResponse> {

    public final static String CHANNEL = "webprotege.events.projects.history.ProjectChangesForHistory";


    @Override
    public String getChannel() {
        return CHANNEL;
    }
}