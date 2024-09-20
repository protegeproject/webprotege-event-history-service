package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.services;

import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.events.*;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.mappers.RevisionEventMapper;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.repositories.RevisionsEventRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NewRevisionsEventServiceImpl implements NewRevisionsEventService {

    private final RevisionsEventRepository repository;
    private final RevisionEventMapper revisionEventMapper;

    public NewRevisionsEventServiceImpl(RevisionsEventRepository repository,
                                        RevisionEventMapper revisionEventMapper) {
        this.repository = repository;
        this.revisionEventMapper = revisionEventMapper;
    }

    @Override
    public void registerEvent(NewLinearizationRevisionsEvent newLinRevEvent) {
        List<RevisionsEvent> revisionsEvents = revisionEventMapper.mapNewLinearizationRevisionsEventToRevisionsEvents(newLinRevEvent.projectId(), newLinRevEvent.changeList());
        repository.saveAll(revisionsEvents);
    }
}
