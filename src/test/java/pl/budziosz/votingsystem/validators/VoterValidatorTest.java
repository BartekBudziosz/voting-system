package pl.budziosz.votingsystem.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import pl.budziosz.votingsystem.model.domain.Voter;
import pl.budziosz.votingsystem.repository.VoterRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VoterValidatorTest {

    private VoterRepository voterRepository;
    private VoterValidator validator;

    @BeforeEach
    void setUp() {
        voterRepository = Mockito.mock(VoterRepository.class);
        validator = new VoterValidator(voterRepository);
    }

    @Test
    void ensureEmailUnique_allowsWhenFreeOrSameAsCurrent() {
        when(voterRepository.findByEmail("nowy@gmail.com")).thenReturn(Optional.empty());
        assertDoesNotThrow(() -> validator.ensureEmailUnique("nowy@gmail.com", null));

        Voter existing = new Voter();
        existing.setId(5L);
        when(voterRepository.findByEmail("stary@gmail.com")).thenReturn(Optional.of(existing));
        assertDoesNotThrow(() -> validator.ensureEmailUnique("stary@gmail.com", 5L));
    }

    @Test
    void ensureEmailUnique_throwsOnConflict() {
        Voter existing = new Voter();
        existing.setId(7L);
        when(voterRepository.findByEmail("duplicate@gmail.com")).thenReturn(Optional.of(existing));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validator.ensureEmailUnique("duplicate@gmail.com", 8L));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertNotNull(ex.getReason());
        assertTrue(ex.getReason().toLowerCase().contains("email"));
    }

    @Test
    void ensureUniquePeselHashExceptCurrent_allowsWhenUniqueOrSameAsCurrent() {
        when(voterRepository.existsByPeselHash("hashA")).thenReturn(false);
        assertDoesNotThrow(() -> validator.ensureUniquePeselHashExceptCurrent("hashA", null));

        when(voterRepository.existsByPeselHash("hashB")).thenReturn(true);
        assertDoesNotThrow(() -> validator.ensureUniquePeselHashExceptCurrent("hashB", "hashB"));
    }

    @Test
    void ensureUniquePeselHashExceptCurrent_throwsOnDuplicateDifferentFromCurrent() {
        when(voterRepository.existsByPeselHash("duplicateHash")).thenReturn(true);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> validator.ensureUniquePeselHashExceptCurrent("duplicateHash", "otherHash"));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertNotNull(ex.getReason());
        assertTrue(ex.getReason().toLowerCase().contains("pesel"));
    }
}
