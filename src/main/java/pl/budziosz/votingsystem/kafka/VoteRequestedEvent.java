package pl.budziosz.votingsystem.kafka;

import java.time.Instant;

public record VoteRequestedEvent(
        String eventId,
        Long electionId,
        Long voterId,
        Long optionId,
        Instant requestedAt
) {}
