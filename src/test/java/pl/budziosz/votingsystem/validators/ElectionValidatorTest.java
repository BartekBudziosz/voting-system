package pl.budziosz.votingsystem.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import pl.budziosz.votingsystem.model.domain.Election;
import pl.budziosz.votingsystem.model.domain.ElectionOption;
import pl.budziosz.votingsystem.repository.ElectionOptionRepository;
import pl.budziosz.votingsystem.repository.ElectionRepository;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ElectionValidatorTest {

    private ElectionRepository electionRepository;
    private ElectionOptionRepository optionRepository;
    private ElectionValidator validator;

    @BeforeEach
    void setUp() {
        electionRepository = Mockito.mock(ElectionRepository.class);
        optionRepository = Mockito.mock(ElectionOptionRepository.class);
        validator = new ElectionValidator(electionRepository, optionRepository);
    }

    @Test
    void validateDates_allowsWhenStartBeforeEnd() {
        assertDoesNotThrow(() -> validator.validateDates(
                Instant.parse("2025-01-01T00:00:00Z"),
                Instant.parse("2025-01-02T00:00:00Z"))
        );
    }

    @Test
    void validateDates_throwsWhenStartNotBeforeEnd() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validator.validateDates(
                        Instant.parse("2025-01-02T00:00:00Z"),
                        Instant.parse("2025-01-02T00:00:00Z"))
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void ensureElectionExists_checksRepository() {
        when(electionRepository.existsById(1L)).thenReturn(true);
        assertDoesNotThrow(() -> validator.ensureElectionExists(1L));

        when(electionRepository.existsById(2L)).thenReturn(false);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validator.ensureElectionExists(2L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void ensureOptionBelongs_validatesRelation() {
        Election e = new Election();
        e.setId(10L);
        ElectionOption opt = new ElectionOption();
        opt.setElection(e);
        assertDoesNotThrow(() -> validator.ensureOptionBelongs(opt, 10L));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validator.ensureOptionBelongs(opt, 11L));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void optionLabelUniqueness_checksRepo() {
        when(optionRepository.existsByElectionIdAndLabel(5L, "A")).thenReturn(false);
        assertDoesNotThrow(() -> validator.ensureOptionLabelUniqueForCreate(5L, "A"));

        when(optionRepository.existsByElectionIdAndLabel(5L, "A")).thenReturn(true);
        ResponseStatusException ex1 = assertThrows(ResponseStatusException.class,
                () -> validator.ensureOptionLabelUniqueForCreate(5L, "A"));
        assertEquals(HttpStatus.CONFLICT, ex1.getStatusCode());

        assertDoesNotThrow(() -> validator.ensureOptionLabelUniqueForUpdate(5L, "A", "A"));

        when(optionRepository.existsByElectionIdAndLabel(5L, "B")).thenReturn(true);
        ResponseStatusException ex2 = assertThrows(ResponseStatusException.class,
                () -> validator.ensureOptionLabelUniqueForUpdate(5L, "B", "A"));
        assertEquals(HttpStatus.CONFLICT, ex2.getStatusCode());
    }
}
