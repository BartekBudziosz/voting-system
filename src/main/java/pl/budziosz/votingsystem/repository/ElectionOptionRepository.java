package pl.budziosz.votingsystem.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.budziosz.votingsystem.model.domain.ElectionOption;

public interface ElectionOptionRepository extends JpaRepository<ElectionOption, Long> {
    List<ElectionOption> findByElectionId(Long electionId);
    boolean existsByElectionIdAndLabel(Long electionId, String label);
}