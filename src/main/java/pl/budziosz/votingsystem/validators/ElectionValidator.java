package pl.budziosz.votingsystem.validators;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import pl.budziosz.votingsystem.model.domain.ElectionOption;
import pl.budziosz.votingsystem.repository.ElectionOptionRepository;
import pl.budziosz.votingsystem.repository.ElectionRepository;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class ElectionValidator {

    private final ElectionRepository electionRepository;
    private final ElectionOptionRepository optionRepository;

    public void validateDates(Instant startsAt, Instant endsAt) {
        if (startsAt != null && endsAt != null && !startsAt.isBefore(endsAt)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startsAt must be before endsAt");
        }
    }

    public void ensureElectionExists(Long id) {
        if (!electionRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Election not found");
        }
    }

    public void ensureOptionBelongs(ElectionOption option, Long electionId) {
        if (!option.getElection().getId().equals(electionId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Option does not belong to the given election");
        }
    }

    public void ensureOptionLabelUniqueForCreate(Long electionId, String label) {
        if (optionRepository.existsByElectionIdAndLabel(electionId, label)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Option label already exists in this election");
        }
    }

    public void ensureOptionLabelUniqueForUpdate(Long electionId, String newLabel, String currentLabel) {
        if (currentLabel != null && currentLabel.equals(newLabel)) {
            return;
        }
        if (optionRepository.existsByElectionIdAndLabel(electionId, newLabel)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Option label already exists in this election");
        }
    }
}
