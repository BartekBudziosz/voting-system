package pl.budziosz.votingsystem.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class ElectionDtos {
    public record ElectionUpsertDto(
            @NotBlank String name,
            String description,
            @NotNull Instant startsAt,
            @NotNull Instant endsAt
    ) {
    }

    public record ElectionResponse(
            Long id,
            String name,
            String description,
            Instant startsAt,
            Instant endsAt
    ) {
    }
}
