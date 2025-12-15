package pl.budziosz.votingsystem.service;

import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pl.budziosz.votingsystem.model.domain.Election;
import pl.budziosz.votingsystem.model.domain.ElectionOption;
import pl.budziosz.votingsystem.repository.ElectionOptionRepository;
import pl.budziosz.votingsystem.repository.ElectionRepository;
import pl.budziosz.votingsystem.validators.ElectionValidator;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserVotingService {

    private final ElectionRepository electionRepository;
    private final ElectionOptionRepository optionRepository;
    private final ElectionValidator electionValidator;

    public List<Election> listActive(Instant now) {
        return electionRepository.findActive(now);
    }

    public List<ElectionOption> listOptions(Long electionId) {
        electionValidator.ensureElectionExists(electionId);
        return optionRepository.findByElectionId(electionId);
    }

    public Election getElection(Long electionId) {
        return electionRepository.findById(electionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Election not found"));
    }
}
