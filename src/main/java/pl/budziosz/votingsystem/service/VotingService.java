package pl.budziosz.votingsystem.service;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.budziosz.votingsystem.model.domain.Election;
import pl.budziosz.votingsystem.model.domain.ElectionOption;
import pl.budziosz.votingsystem.model.domain.ProcessedEvent;
import pl.budziosz.votingsystem.model.domain.Vote;
import pl.budziosz.votingsystem.model.domain.Voter;
import pl.budziosz.votingsystem.kafka.VoteRequestedEvent;
import pl.budziosz.votingsystem.repository.ElectionOptionRepository;
import pl.budziosz.votingsystem.repository.ElectionRepository;
import pl.budziosz.votingsystem.repository.ProcessedEventRepository;
import pl.budziosz.votingsystem.repository.VoteRepository;
import pl.budziosz.votingsystem.repository.VoterRepository;
import pl.budziosz.votingsystem.validators.VotingValidator;

@Service
@RequiredArgsConstructor
@Slf4j
public class VotingService {

    private final VoteRepository voteRepository;
    private final VoterRepository voterRepository;
    private final ElectionRepository electionRepository;
    private final ElectionOptionRepository electionOptionRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final VotingValidator votingValidator;

    @Transactional
    public void processEvent(VoteRequestedEvent e) {
        int inserted = insertProcessedEvent(e.eventId());
        if (inserted == 0) {
            log.debug("Event {} already processed, skipping", e.eventId());
            return;
        }

        Election election = electionRepository.findById(e.electionId())
                .orElseThrow(() -> new IllegalArgumentException("Election not found"));
        Instant t = e.requestedAt();
        votingValidator.ensureWithinWindow(election, t);

        Voter voter = voterRepository.findById(e.voterId())
                .orElseThrow(() -> new IllegalArgumentException("Voter not found"));
        votingValidator.requireActiveVoter(voter);

        ElectionOption option = electionOptionRepository.findById(e.optionId())
                .orElseThrow(() -> new IllegalArgumentException("Option not found"));
        votingValidator.ensureOptionBelongsToElection(option, e.electionId());

        Vote vote = new Vote();
        vote.setElection(election);
        vote.setVoter(voter);
        vote.setOption(option);

        try {
            voteRepository.save(vote);
            log.info("Vote stored for election={} voter={} option={} eventId={}", e.electionId(), e.voterId(), e.optionId(), e.eventId());
        } catch (DataIntegrityViolationException ex) {
            log.info("Duplicate vote ignored for election={} voter={} (eventId={})", e.electionId(), e.voterId(), e.eventId());
        }
    }

    private int insertProcessedEvent(String eventId) {
        try {
            processedEventRepository.save(ProcessedEvent.of(eventId));
            return 1;
        } catch (DataIntegrityViolationException ex) {
            return 0;
        }
    }
}
