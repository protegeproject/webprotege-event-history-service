package edu.stanford.protege.webprotegeeventshistory.dto;

import edu.stanford.protege.webprotege.common.Event;

import java.util.List;

public record EventList <E extends Event> (EventTag startTag, List<E> events, EventTag endTag){
}
