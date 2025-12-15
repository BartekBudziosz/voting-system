package pl.budziosz.votingsystem.validators;

import org.junit.jupiter.api.Test;
import pl.budziosz.votingsystem.model.domain.Election;
import pl.budziosz.votingsystem.model.domain.ElectionOption;
import pl.budziosz.votingsystem.model.domain.Voter;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class VotingValidatorTest {

    private final VotingValidator validator = new VotingValidator();

    @Test
    void ensureWithinWindow_allowsInsideWindow() {
        Election e = new Election();
        e.setStartsAt(Instant.parse("2025-12-01T00:00:00Z"));
        e.setEndsAt(Instant.parse("2025-12-31T23:59:59Z"));

        assertDoesNotThrow(() -> validator.ensureWithinWindow(e, Instant.parse("2025-12-15T12:00:00Z")));
    }

    @Test
    void ensureWithinWindow_throwsOutsideWindow() {
        Election e = new Election();
        e.setStartsAt(Instant.parse("2025-12-01T00:00:00Z"));
        e.setEndsAt(Instant.parse("2025-12-31T23:59:59Z"));

        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
                () -> validator.ensureWithinWindow(e, Instant.parse("2025-11-30T23:59:59Z")));
        assertTrue(ex1.getMessage().contains("window"));

        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
                () -> validator.ensureWithinWindow(e, Instant.parse("2026-01-01T00:00:00Z")));
        assertTrue(ex2.getMessage().contains("window"));
    }

    @Test
    void requireActiveVoter_throwsWhenBlocked() {
        Voter v = new Voter();
        v.setBlocked(true);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> validator.requireActiveVoter(v));
        assertTrue(ex.getMessage().toLowerCase().contains("blocked"));
    }

    @Test
    void requireActiveVoter_okWhenNotBlocked() {
        Voter v = new Voter();
        v.setBlocked(false);
        assertDoesNotThrow(() -> validator.requireActiveVoter(v));
    }

    @Test
    void ensureOptionBelongsToElection_checksRelation() {
        Election election = new Election();
        election.setId(10L);
        Election otherElection = new Election();
        otherElection.setId(11L);

        ElectionOption opt = new ElectionOption();
        opt.setElection(election);

        assertDoesNotThrow(() -> validator.ensureOptionBelongsToElection(opt, 10L));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> validator.ensureOptionBelongsToElection(opt, 11L));
        assertTrue(ex.getMessage().toLowerCase().contains("belong"));
    }
}
