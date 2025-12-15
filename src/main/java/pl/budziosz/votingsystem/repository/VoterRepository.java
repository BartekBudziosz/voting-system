package pl.budziosz.votingsystem.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.budziosz.votingsystem.model.domain.Voter;

public interface VoterRepository extends JpaRepository<Voter, Long> {
    Optional<Voter> findByEmail(String email);
    boolean existsByPeselHash(String peselHash);
}