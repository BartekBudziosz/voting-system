package pl.budziosz.votingsystem.repository;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.budziosz.votingsystem.model.domain.Election;

public interface ElectionRepository extends JpaRepository<Election, Long> {
    @Query("select e from Election e where e.startsAt <= :now and e.endsAt >= :now order by e.startsAt asc")
    List<Election> findActive(@Param("now") Instant now);
}