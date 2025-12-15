package pl.budziosz.votingsystem.model.dto;

import java.util.List;

public class ElectionResultsDtos {
    public record OptionResult(
            Long optionId,
            String label,
            long votes
    ) {
    }

    public record ElectionResults(
            Long electionId,
            List<OptionResult> options
    ) {
    }
}
