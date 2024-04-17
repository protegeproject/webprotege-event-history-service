package edu.stanford.protege.webprotegeeventshistory.sequence;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class SequenceService {

    private final HighLevelSequenceRepository sequenceRepository;

    public SequenceService(HighLevelSequenceRepository sequenceRepository) {
        this.sequenceRepository = sequenceRepository;
    }

    @Transactional
    public int getNextHighLevelEventSequence() {
        synchronized (this) {
            List<HighLevelEventsSequence> sequences = sequenceRepository.findAll();
            HighLevelEventsSequence sequence;
            if (sequences.isEmpty()) {
                sequence = new HighLevelEventsSequence((0));
            } else {
                sequence = sequences.get(0);
            }
            sequence.setSeq(sequence.getSeq() + 1);
            sequenceRepository.save(sequence);
            return sequence.getSeq();
        }
    }
}
