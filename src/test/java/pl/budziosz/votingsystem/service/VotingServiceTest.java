package pl.budziosz.votingsystem.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import pl.budziosz.votingsystem.kafka.VoteRequestedEvent;
import pl.budziosz.votingsystem.model.domain.Election;
import pl.budziosz.votingsystem.model.domain.ElectionOption;
import pl.budziosz.votingsystem.model.domain.ProcessedEvent;
import pl.budziosz.votingsystem.model.domain.Vote;
import pl.budziosz.votingsystem.model.domain.Voter;
import pl.budziosz.votingsystem.repository.*;
import pl.budziosz.votingsystem.validators.VotingValidator;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class VotingServiceTest {

    private VoteRepository voteRepository;
    private VoterRepository voterRepository;
    private ElectionRepository electionRepository;
    private ElectionOptionRepository electionOptionRepository;
    private ProcessedEventRepository processedEventRepository;
    private VotingValidator votingValidator;

    private VotingService service;

    @BeforeEach
    void setUp() {
        voteRepository = Mockito.mock(VoteRepository.class);
        voterRepository = Mockito.mock(VoterRepository.class);
        electionRepository = Mockito.mock(ElectionRepository.class);
        electionOptionRepository = Mockito.mock(ElectionOptionRepository.class);
        processedEventRepository = Mockito.mock(ProcessedEventRepository.class);
        votingValidator = Mockito.mock(VotingValidator.class);
        service = new VotingService(voteRepository, voterRepository, electionRepository,
                electionOptionRepository, processedEventRepository, votingValidator);
    }

    private VoteRequestedEvent sampleEvent() {
        return new VoteRequestedEvent(
                "123",
                1L,
                2L,
                3L,
                Instant.parse("2025-12-15T12:00:00Z")
        );
    }

    private void stubHappyPath() {
        Election election = new Election(); election.setId(1L); election.setStartsAt(Instant.parse("2025-12-01T00:00:00Z")); election.setEndsAt(Instant.parse("2025-12-31T23:59:59Z"));
        Voter voter = new Voter(); voter.setId(2L); voter.setBlocked(false);
        ElectionOption option = new ElectionOption(); option.setId(3L); option.setElection(election);

        when(electionRepository.findById(1L)).thenReturn(Optional.of(election));
        when(voterRepository.findById(2L)).thenReturn(Optional.of(voter));
        when(electionOptionRepository.findById(3L)).thenReturn(Optional.of(option));
        when(processedEventRepository.save(any())).thenReturn(ProcessedEvent.of("123"));
        when(voteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void processEvent_happyPath_savesVote() {
        stubHappyPath();
        VoteRequestedEvent e = sampleEvent();

        service.processEvent(e);

        InOrder inOrder = inOrder(processedEventRepository, electionRepository, votingValidator, voterRepository, electionOptionRepository, voteRepository);
        inOrder.verify(processedEventRepository).save(any(ProcessedEvent.class));
        inOrder.verify(electionRepository).findById(1L);
        inOrder.verify(votingValidator).ensureWithinWindow(any(Election.class), eq(e.requestedAt()));
        inOrder.verify(voterRepository).findById(2L);
        inOrder.verify(votingValidator).requireActiveVoter(any(Voter.class));
        inOrder.verify(electionOptionRepository).findById(3L);
        inOrder.verify(votingValidator).ensureOptionBelongsToElection(any(ElectionOption.class), eq(1L));
        inOrder.verify(voteRepository).save(any(Vote.class));
    }

    @Test
    void processEvent_duplicateEvent_isIgnoredEarly() {

        doThrow(new DataIntegrityViolationException("duplicate")).when(processedEventRepository).save(any());
        service.processEvent(sampleEvent());
        verifyNoInteractions(electionRepository, voterRepository, electionOptionRepository, voteRepository, votingValidator);
    }

    @Test
    void processEvent_duplicateVote_isCaughtButNotThrown() {
        stubHappyPath();
        when(voteRepository.save(any())).thenThrow(new DataIntegrityViolationException("violation"));

        assertDoesNotThrow(() -> service.processEvent(sampleEvent()));
        verify(voteRepository).save(any(Vote.class));
    }

    @Test
    void processEvent_outsideWindow_throwsAndDoesNotSaveVote() {
        stubHappyPath();
        doThrow(new IllegalArgumentException("Vote outside election window"))
                .when(votingValidator).ensureWithinWindow(any(Election.class), any());

        assertThrows(IllegalArgumentException.class, () -> service.processEvent(sampleEvent()));
        verify(voteRepository, never()).save(any());
        verify(processedEventRepository).save(any());
    }
}
