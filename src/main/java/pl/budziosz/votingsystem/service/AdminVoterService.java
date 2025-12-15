package pl.budziosz.votingsystem.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pl.budziosz.votingsystem.model.domain.Voter;
import pl.budziosz.votingsystem.repository.VoterRepository;
import pl.budziosz.votingsystem.model.dto.VoterDtos.VoterUpsertDto;
import pl.budziosz.votingsystem.utils.PeselHasher;
import pl.budziosz.votingsystem.validators.VoterValidator;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminVoterService {

    private final PeselHasher peselHasher;
    private final VoterRepository voterRepository;
    private final VoterValidator voterValidator;

    public Page<Voter> listAll(@ParameterObject Pageable pageable) {
        return voterRepository.findAll(pageable);
    }

    public Voter get(Long id) {
        return voterRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Voter not found"));
    }

    @Transactional
    public Voter create(@Valid VoterUpsertDto dto) {
        voterValidator.ensureEmailUnique(dto.email(), null);
        String peselHash = peselHasher.hash(dto.pesel());
        voterValidator.ensureUniquePeselHashExceptCurrent(peselHash, null);
        Voter v = new Voter();
        apply(v, dto, peselHash);
        return voterRepository.save(v);
    }

    @Transactional
    public Voter update(Long id, @Valid VoterUpsertDto dto) {
        Voter v = voterRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Voter not found"));
        voterValidator.ensureEmailUnique(dto.email(), id);
        String peselHash = peselHasher.hash(dto.pesel());
        voterValidator.ensureUniquePeselHashExceptCurrent(dto.pesel(), v.getPeselHash());
        apply(v, dto, peselHash);
        return voterRepository.save(v);
    }

    @Transactional
    public void delete(Long id) {
        if (!voterRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Voter not found");
        }
        voterRepository.deleteById(id);
    }

    @Transactional
    public Voter block(Long id) {
        Voter v = voterRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Voter not found"));
        v.setBlocked(true);
        return voterRepository.save(v);
    }

    @Transactional
    public Voter unblock(Long id) {
        Voter v = voterRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Voter not found"));
        v.setBlocked(false);
        return voterRepository.save(v);
    }

    private void apply(Voter v, VoterUpsertDto dto, String peselHash) {
        v.setEmail(dto.email());
        v.setFullName(dto.fullName());
        v.setPeselHash(peselHash);
        if (dto.blocked() != null) {
            v.setBlocked(dto.blocked());
        }
    }
}
