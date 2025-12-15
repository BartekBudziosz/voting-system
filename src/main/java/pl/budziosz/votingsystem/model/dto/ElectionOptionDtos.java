package pl.budziosz.votingsystem.model.dto;

import jakarta.validation.constraints.NotBlank;

public class ElectionOptionDtos {
    public record ElectionOptionUpsertDto(
            @NotBlank String label
    ) {
    }

    public record ElectionOptionResponse(
            Long id,
            String label
    ) {
    }
}
