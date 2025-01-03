package edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.services;

import edu.stanford.protege.webprotege.change.ProjectChange;
import edu.stanford.protege.webprotege.common.Page;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.dto.ChangeType;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.dto.ChangedEntities;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.dto.EntityChange;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.dto.EntityHistorySummary;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.events.NewRevisionsEvent;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.events.RevisionsEvent;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.mappers.ProjectChangeMapper;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.mappers.RevisionEventMapper;
import edu.stanford.protege.webprotegeeventshistory.uiHistoryConcern.repositories.RevisionsEventRepository;
import org.semanticweb.owlapi.model.OWLEntity;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public void registerEvent(NewRevisionsEvent newRevEvent) {
        List<RevisionsEvent> revisionsEvents = revisionEventMapper.mapNewRevisionsEventToRevisionsEvents(newRevEvent);

        repository.saveAll(revisionsEvents);
    }

    @Override
    public Page<ProjectChange> fetchPaginatedProjectChanges(ProjectId projectId, Optional<OWLEntity> subject, int pageNumber, int pageSize) {
        String entityIriSubject = subject.map(sub -> sub.getIRI().toString()).orElse(null);
        RevisionsEvent probe = RevisionsEvent.create(
                projectId,
                entityIriSubject,
                null,
                0,
                null,
                null
        );
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher(PROJECT_ID, ExampleMatcher.GenericPropertyMatchers.exact())
                .withIgnorePaths(TIMESTAMP)
                .withMatcher(WHOFIC_ENTITY_IRI, ExampleMatcher.GenericPropertyMatchers.exact())
                .withIgnorePaths(CHANGE_TYPE)
                .withIgnoreNullValues();

        Example<RevisionsEvent> example = Example.of(probe, matcher);

        //Page number from ui is starting from 1
        //PageRequest from spring-data is starting from 0
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, Sort.by(Sort.Direction.DESC, TIMESTAMP));

        org.springframework.data.domain.Page<RevisionsEvent> revisionsEventPage = repository.findAll(example, pageable);
        if (revisionsEventPage.getTotalElements() == 0) {
            return Page.emptyPage();
        }

        List<ProjectChange> changes = revisionsEventPage.get()
                .map(revisionsEvent -> projectChangeMapper.mapProjectChangeDocumentToProjectChange(revisionsEvent.projectChange()))
                .toList();

        //Page number from ui is starting from 1
        //PageRequest from spring-data is starting from 0
        return Page.create(pageNumber, revisionsEventPage.getTotalPages(), changes, revisionsEventPage.getTotalElements());
    }

    @Override
    public ChangedEntities getChangedEntitiesAfterTimestamp(ProjectId projectId, long timestamp) {
        List<RevisionsEvent> revisionsEvents = repository.findByProjectIdAndTimestampAfter(projectId.id(), timestamp);

        List<String> createdEntities = groupByChangeType(revisionsEvents, ChangeType.CREATE_ENTITY);

        List<String> updatedEntities = groupByChangeType(revisionsEvents, ChangeType.UPDATE_ENTITY);

        List<String> deletedEntities = groupByChangeType(revisionsEvents, ChangeType.DELETE_ENTITY);

        return new ChangedEntities(createdEntities, updatedEntities, deletedEntities);
    }

    private static List<String> groupByChangeType(List<RevisionsEvent> revisionsEvents, ChangeType changeType) {
        return revisionsEvents.stream()
                .filter(event -> event.changeType() == changeType)
                .map(RevisionsEvent::whoficEntityIri)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public EntityHistorySummary getEntityHistorySummary(ProjectId projectId, String entityIri) {
        List<RevisionsEvent> revisionsForEntity = repository.findByProjectIdAndWhoficEntityIriOrderByTimestampDesc(projectId, entityIri);
        List<EntityChange> entityChanges = revisionsForEntity.stream()
                .map(revisionEvent -> {
                            ProjectChange projectChange = projectChangeMapper.mapProjectChangeDocumentToProjectChange(revisionEvent.projectChange());
                            Instant instant = Instant.ofEpochMilli(projectChange.getTimestamp());
                            LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.of("UTC"));
                            return EntityChange.create(projectChange.getSummary(), projectChange.getAuthor(), localDateTime);
                        }
                ).toList();


        return EntityHistorySummary.create(entityIri, projectId.value(), entityChanges);
    }
}
