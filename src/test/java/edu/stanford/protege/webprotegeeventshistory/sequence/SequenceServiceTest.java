package edu.stanford.protege.webprotegeeventshistory.sequence;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class SequenceServiceTest {


    @Mock
    private HighLevelSequenceRepository repository;

    ArgumentCaptor<HighLevelEventsSequence> captor = ArgumentCaptor.forClass(HighLevelEventsSequence.class);

    private SequenceService service;


    @Before
    public void setUp(){
        this.service = new SequenceService(repository);
    }

    @Test
    public void GIVEN_newSequence_WHEN_getNextId_THEN_newSequenceIsSaved(){
        when(repository.findAll()).thenReturn(new ArrayList<>());

        service.getNextHighLevelEventSequence();

        verify(repository).save(captor.capture());
        var parameter = captor.getValue();
        assertEquals(1, parameter.getSeq());
    }

    @Test
    public void GIVEN_existingSequence_WHEN_getNextId_THEN_sequenceIsUpdated(){
        when(repository.findAll()).thenReturn(List.of(new HighLevelEventsSequence(5)));

        service.getNextHighLevelEventSequence();

        verify(repository).save(captor.capture());
        var parameter = captor.getValue();
        assertEquals(6, parameter.getSeq());
    }
}
