package edu.stanford.protege.webprotegeeventshistory.sequence;

import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

/**
 * Assigns per-project, monotonically increasing sequence ordinals.
 * <p>
 * Each project has its own counter document in the {@code ProjectEventSequence} collection
 * (see {@link ProjectEventSequence}). {@link #next(String)} advances it with a single-document
 * {@code findAndModify}/{@code $inc}, which MongoDB executes atomically on the server, so the
 * numbers stay dense and gapless even when several service instances increment the same
 * project's counter concurrently. The first ordinal handed out for a project is {@code 1}.
 */
@Service
public class ProjectSequenceService {

    private final MongoTemplate mongoTemplate;

    public ProjectSequenceService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Atomically increments and returns the next sequence ordinal for the given project.
     * If the project has no counter yet one is created and {@code 1} is returned.
     */
    public int next(String projectId) {
        var query = new Query(Criteria.where("_id").is(projectId));
        var update = new Update().inc(ProjectEventSequence.SEQ_FIELD, 1);
        var options = new FindAndModifyOptions().upsert(true).returnNew(true);
        ProjectEventSequence result = mongoTemplate.findAndModify(query, update, options, ProjectEventSequence.class);
        return result.getSeq();
    }

    /**
     * Reads the current head ordinal for the given project without advancing it.
     * Returns {@code 0} when the project has no counter yet (consumed by #301).
     */
    public int getCurrentSequence(String projectId) {
        var query = new Query(Criteria.where("_id").is(projectId));
        ProjectEventSequence result = mongoTemplate.findOne(query, ProjectEventSequence.class);
        return result == null ? 0 : result.getSeq();
    }
}
