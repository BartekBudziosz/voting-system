package pl.budziosz.votingsystem.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.budziosz.votingsystem.model.domain.Vote;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    @Query("select o.id as optionId, o.label as label, count(v.id) as votes " +
           "from ElectionOption o left join Vote v on v.option = o " +
           "where o.election.id = :electionId " +
           "group by o.id, o.label order by o.id")
    List<OptionCount> countResultsByElection(@Param("electionId") Long electionId);

    interface OptionCount {
        Long getOptionId();
        String getLabel();
        long getVotes();
    }
}
