package pl.budziosz.votingsystem.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import pl.budziosz.votingsystem.model.domain.Election;
import pl.budziosz.votingsystem.model.domain.ElectionOption;
import pl.budziosz.votingsystem.model.dto.ElectionDtos;
import pl.budziosz.votingsystem.model.dto.ElectionOptionDtos;
import pl.budziosz.votingsystem.repository.ElectionOptionRepository;
import pl.budziosz.votingsystem.repository.ElectionRepository;
import pl.budziosz.votingsystem.repository.VoteRepository;
import pl.budziosz.votingsystem.validators.ElectionValidator;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminElectionServiceTest {

    private ElectionRepository electionRepository;
    private ElectionOptionRepository optionRepository;
    private VoteRepository voteRepository;
    private ElectionValidator electionValidator;
    private AdminElectionService service;

    @BeforeEach
    void setUp() {
        electionRepository = Mockito.mock(ElectionRepository.class);
        optionRepository = Mockito.mock(ElectionOptionRepository.class);
        voteRepository = Mockito.mock(VoteRepository.class);
        electionValidator = Mockito.mock(ElectionValidator.class);
        service = new AdminElectionService(electionRepository, optionRepository, voteRepository, electionValidator);
    }

    @Test
    void listAll_delegatesToRepo() {
        PageRequest pr = PageRequest.of(0, 10);
        when(electionRepository.findAll(pr)).thenReturn(new PageImpl<>(List.of()));
        Page<Election> page = service.listAll(pr);
        assertNotNull(page);
        verify(electionRepository).findAll(pr);
    }

    @Test
    void create_validatesDates_andSaves() {
        ElectionDtos.ElectionUpsertDto dto = new ElectionDtos.ElectionUpsertDto(
                "Wybory", "Opis", Instant.parse("2025-01-01T00:00:00Z"), Instant.parse("2025-01-02T00:00:00Z")
        );
        when(electionRepository.save(any())).thenAnswer(inv -> {
            Election e = inv.getArgument(0);
            e.setId(1L); return e;
        });
        Election saved = service.create(dto);
        assertEquals(1L, saved.getId());
        verify(electionValidator).validateDates(dto.startsAt(), dto.endsAt());
        ArgumentCaptor<Election> captor = ArgumentCaptor.forClass(Election.class);
        verify(electionRepository).save(captor.capture());
        Election toSave = captor.getValue();
        assertEquals("Wybory", toSave.getName());
        assertEquals("Opis", toSave.getDescription());
    }

    @Test
    void get_notFound_throws404() {
        when(electionRepository.findById(9L)).thenReturn(Optional.empty());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.get(9L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void addOption_validatesAndSaves() {
        Election election = new Election(); election.setId(3L);
        when(electionRepository.findById(3L)).thenReturn(Optional.of(election));
        ElectionOptionDtos.ElectionOptionUpsertDto dto = new ElectionOptionDtos.ElectionOptionUpsertDto("Andrzej");
        when(optionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ElectionOption opt = service.addOption(3L, dto);
        assertEquals("Andrzej", opt.getLabel());
        assertEquals(3L, opt.getElection().getId());
        verify(electionValidator).ensureOptionLabelUniqueForCreate(3L, "Andrzej");
    }

    @Test
    void updateOption_checksBelongs_andLabelUniqueness() {
        Election e = new Election(); e.setId(4L);
        ElectionOption opt = new ElectionOption(); opt.setId(7L); opt.setElection(e); opt.setLabel("Poprzednia opcja");
        when(optionRepository.findById(7L)).thenReturn(Optional.of(opt));
        when(optionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ElectionOptionDtos.ElectionOptionUpsertDto dto = new ElectionOptionDtos.ElectionOptionUpsertDto("Nowa opcja");
        ElectionOption updated = service.updateOption(4L, 7L, dto);
        assertEquals("Nowa opcja", updated.getLabel());
        verify(electionValidator).ensureOptionBelongs(opt, 4L);
        verify(electionValidator).ensureOptionLabelUniqueForUpdate(4L, "Nowa opcja", "Poprzednia opcja");
    }

    @Test
    void delete_nonExistingElection_throws404() {
        when(electionRepository.existsById(100L)).thenReturn(false);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.delete(100L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}
