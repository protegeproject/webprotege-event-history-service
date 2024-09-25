package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.services;

import edu.stanford.protege.webprotege.change.ProjectChange;
import edu.stanford.protege.webprotege.common.Page;
import edu.stanford.protege.webprotege.common.*;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.events.*;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.mappers.*;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.repositories.RevisionsEventRepository;
import org.semanticweb.owlapi.model.OWLEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.events.RevisionsEvent.*;

@Service
@Transactional
public class NewRevisionsEventServiceImpl implements NewRevisionsEventService {

    private final RevisionsEventRepository repository;
    private final RevisionEventMapper revisionEventMapper;
    private final ProjectChangeMapper projectChangeMapper;

    public NewRevisionsEventServiceImpl(RevisionsEventRepository repository,
                                        RevisionEventMapper revisionEventMapper, ProjectChangeMapper projectChangeMapper) {
        this.repository = repository;
        this.revisionEventMapper = revisionEventMapper;
        this.projectChangeMapper = projectChangeMapper;
    }

    @Override
    public void registerEvent(NewRevisionsEvent newLinRevEvent) {
        List<RevisionsEvent> revisionsEvents = revisionEventMapper.mapNewRevisionsEventToRevisionsEvents(newLinRevEvent);

        repository.saveAll(revisionsEvents);
    }

    @Override
    public Page<ProjectChange> fetchPaginatedProjectChanges(ProjectId projectId, Optional<OWLEntity> subject, int pageNumber, int pageSize) {
        String entityIriSubject = subject.map(sub -> sub.getIRI().toString()).orElse(null);
        RevisionsEvent probe = new RevisionsEvent(
                projectId,
                entityIriSubject,
                0,
                null
        );
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher(PROJECT_ID, ExampleMatcher.GenericPropertyMatchers.exact())
                .withIgnorePaths(TIMESTAMP)
                .withMatcher(WHOFIC_ENTITY_IRI, ExampleMatcher.GenericPropertyMatchers.exact())
                .withIgnoreNullValues();

        Example<RevisionsEvent> example = Example.of(probe, matcher);

        //Page number from ui is starting from 1
        //PageRequest from spring-data is starting from 0
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, Sort.by(Sort.Direction.DESC, TIMESTAMP));

        org.springframework.data.domain.Page<RevisionsEvent> revisionsEventPage = repository.findAll(example, pageable);

        List<ProjectChange> changes = revisionsEventPage.get()
                .map(revisionsEvent -> projectChangeMapper.mapProjectChangeDocumentToProjectChange(revisionsEvent.projectChange()))
                .toList();

        int pageCount = revisionsEventPage.getTotalPages() == 0 ? 1 : revisionsEventPage.getTotalPages();

        //Page number from ui is starting from 1
        //PageRequest from spring-data is starting from 0
        return Page.create(pageNumber, pageCount, changes, revisionsEventPage.getTotalElements());
    }
}