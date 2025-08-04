package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.events;

import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.EventId;
import edu.stanford.protege.webprotege.common.ProjectEvent;
import edu.stanford.protege.webprotege.common.ProjectId;
import org.semanticweb.owlapi.model.IRI;

@JsonTypeName(ParentsChangedEvent.CHANNEL)
public record ParentsChangedEvent(ProjectId projectId, EventId eventId, IRI entityIri) implements ProjectEvent {

    public final static String CHANNEL = "webprotege.events.projects.ui.ParentsChanged";


    @Override
    public ProjectId projectId() {
        return projectId;
    }


    @Override
    public EventId eventId() {
        return eventId;
    }

    @Override
    public String getChannel() {
        return CHANNEL;
    }
}