package pl.budziosz.votingsystem.validators;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.budziosz.votingsystem.model.domain.Election;
import pl.budziosz.votingsystem.model.domain.ElectionOption;
import pl.budziosz.votingsystem.model.domain.Voter;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class VotingValidator {

    public void ensureWithinWindow(Election election, Instant at) {
        if (at.isBefore(election.getStartsAt()) || at.isAfter(election.getEndsAt())) {
            throw new IllegalArgumentException("Vote outside election window");
        }
    }

    public void requireActiveVoter(Voter voter) {
        if (voter.isBlocked()) {
            throw new IllegalArgumentException("Voter blocked");
        }
    }


    public void ensureOptionBelongsToElection(ElectionOption option, Long electionId) {
        if (!option.getElection().getId().equals(electionId)) {
            throw new IllegalArgumentException("Option does not belong to the election");
        }
    }
}
