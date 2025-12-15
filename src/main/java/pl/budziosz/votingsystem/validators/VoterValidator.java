package pl.budziosz.votingsystem.validators;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import pl.budziosz.votingsystem.repository.VoterRepository;

@Component
@RequiredArgsConstructor
public class VoterValidator {

    private final VoterRepository voterRepository;

    public void ensureEmailUnique(String email, Long excludeVoterId) {
        voterRepository.findByEmail(email).ifPresent(existing -> {
            if (!existing.getId().equals(excludeVoterId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
            }
        });
    }

    public void ensureUniquePeselHashExceptCurrent(String peselHash, String currentPeselHash) {
        boolean duplicate = voterRepository.existsByPeselHash(peselHash);
        boolean isSameAsCurrent = peselHash.equals(currentPeselHash);
        if (duplicate && !isSameAsCurrent) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "PESEL already exists");
        }
    }
}
