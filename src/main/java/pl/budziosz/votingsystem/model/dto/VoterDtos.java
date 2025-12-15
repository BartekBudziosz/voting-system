package pl.budziosz.votingsystem.model.dto;

import jakarta.validation.constraints.*;

public class VoterDtos {
    public record VoterUpsertDto(
            @NotBlank @Email String email,
            @NotBlank String fullName,
            @NotBlank @Pattern(regexp = "\\d{11}", message = "PESEL musi mieć 11 cyfr") @Size(min = 11, max = 11) String pesel,
            Boolean blocked
    ) {
    }

    public record VoterResponse(
            Long id,
            String email,
            String fullName,
            boolean blocked
    ) {
    }
}
