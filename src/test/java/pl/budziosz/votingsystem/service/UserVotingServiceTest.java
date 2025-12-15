package pl.budziosz.votingsystem.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import pl.budziosz.votingsystem.model.domain.Election;
import pl.budziosz.votingsystem.model.domain.ElectionOption;
import pl.budziosz.votingsystem.repository.ElectionOptionRepository;
import pl.budziosz.votingsystem.repository.ElectionRepository;
import pl.budziosz.votingsystem.validators.ElectionValidator;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserVotingServiceTest {

    private ElectionRepository electionRepository;
    private ElectionOptionRepository optionRepository;
    private ElectionValidator electionValidator;
    private UserVotingService service;

    @BeforeEach
    void setUp() {
        electionRepository = Mockito.mock(ElectionRepository.class);
        optionRepository = Mockito.mock(ElectionOptionRepository.class);
        electionValidator = Mockito.mock(ElectionValidator.class);
        service = new UserVotingService(electionRepository, optionRepository, electionValidator);
    }

    @Test
    void listActive_delegatesToRepository() {
        Instant now = Instant.parse("2025-12-15T12:00:00Z");
        when(electionRepository.findActive(now)).thenReturn(List.of());
        List<Election> res = service.listActive(now);
        assertNotNull(res);
        verify(electionRepository).findActive(now);
    }

    @Test
    void listOptions_validatesElectionExists_thenFetchesOptions() {
        when(optionRepository.findByElectionId(5L)).thenReturn(List.of(new ElectionOption()));
        List<ElectionOption> opts = service.listOptions(5L);
        assertEquals(1, opts.size());
        verify(electionValidator).ensureElectionExists(5L);
        verify(optionRepository).findByElectionId(5L);
    }

    @Test
    void getElection_returnsOr404() {
        Election e = new Election(); e.setId(7L);
        when(electionRepository.findById(7L)).thenReturn(Optional.of(e));
        assertEquals(7L, service.getElection(7L).getId());

        when(electionRepository.findById(8L)).thenReturn(Optional.empty());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.getElection(8L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}
