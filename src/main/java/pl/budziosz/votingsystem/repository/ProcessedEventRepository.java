package pl.budziosz.votingsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.budziosz.votingsystem.model.domain.ProcessedEvent;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, String> {
}
