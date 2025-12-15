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
import pl.budziosz.votingsystem.model.domain.Voter;
import pl.budziosz.votingsystem.model.dto.VoterDtos;
import pl.budziosz.votingsystem.repository.VoterRepository;
import pl.budziosz.votingsystem.utils.PeselHasher;
import pl.budziosz.votingsystem.validators.VoterValidator;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminVoterServiceTest {

    private PeselHasher peselHasher;
    private VoterRepository voterRepository;
    private VoterValidator voterValidator;
    private AdminVoterService service;

    @BeforeEach
    void setUp() {
        peselHasher = Mockito.mock(PeselHasher.class);
        voterRepository = Mockito.mock(VoterRepository.class);
        voterValidator = Mockito.mock(VoterValidator.class);
        service = new AdminVoterService(peselHasher, voterRepository, voterValidator);
    }

    @Test
    void listAll_delegatesToRepository() {
        PageRequest pr = PageRequest.of(0, 10);
        when(voterRepository.findAll(pr)).thenReturn(new PageImpl<>(List.of()));
        Page<Voter> page = service.listAll(pr);
        assertNotNull(page);
        verify(voterRepository).findAll(pr);
    }

    @Test
    void get_notFound_throws404() {
        when(voterRepository.findById(1L)).thenReturn(Optional.empty());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.get(1L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void create_hashesPesel_validates_andSaves() {
        VoterDtos.VoterUpsertDto dto = new VoterDtos.VoterUpsertDto(
                "jan@gmail.com", "Jan Kowalski", "12345678901", false
        );
        when(peselHasher.hash("12345678901")).thenReturn("HASH");
        when(voterRepository.save(any())).thenAnswer(inv -> {
            Voter v = inv.getArgument(0);
            v.setId(100L); return v;
        });

        Voter created = service.create(dto);
        assertEquals(100L, created.getId());

        verify(voterValidator).ensureEmailUnique("jan@gmail.com", null);
        verify(voterValidator).ensureUniquePeselHashExceptCurrent("HASH", null);

        ArgumentCaptor<Voter> captor = ArgumentCaptor.forClass(Voter.class);
        verify(voterRepository).save(captor.capture());
        Voter saved = captor.getValue();
        assertEquals("HASH", saved.getPeselHash());
        assertEquals("jan@gmail.com", saved.getEmail());
        assertEquals("Jan Kowalski", saved.getFullName());
        assertFalse(saved.isBlocked());
    }

    @Test
    void block_and_unblock_toggleFlag() {
        Voter v = new Voter(); v.setId(5L); v.setBlocked(false);
        when(voterRepository.findById(5L)).thenReturn(Optional.of(v));
        when(voterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Voter blocked = service.block(5L);
        assertTrue(blocked.isBlocked());

        v.setBlocked(true);
        Voter unblocked = service.unblock(5L);
        assertFalse(unblocked.isBlocked());
    }
}
