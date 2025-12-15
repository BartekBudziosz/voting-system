package pl.budziosz.votingsystem.model.dto;

import jakarta.validation.constraints.NotNull;

public class VoteDtos {

    public record VoteRequest(
            @NotNull Long voterId,
            @NotNull Long optionId
    ) {
    }

    public record VoteAcceptedResponse(
            String status,
            String eventId
    ) {
    }
}
