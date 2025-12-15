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
import pl.budziosz.votingsystem.model.domain.Election;
import pl.budziosz.votingsystem.model.domain.ElectionOption;
import pl.budziosz.votingsystem.model.dto.ElectionDtos.ElectionUpsertDto;
import pl.budziosz.votingsystem.model.dto.ElectionOptionDtos.ElectionOptionUpsertDto;
import pl.budziosz.votingsystem.repository.ElectionOptionRepository;
import pl.budziosz.votingsystem.repository.ElectionRepository;
import pl.budziosz.votingsystem.repository.VoteRepository;
import pl.budziosz.votingsystem.validators.ElectionValidator;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminElectionService {

    private final ElectionRepository electionRepository;
    private final ElectionOptionRepository optionRepository;
    private final VoteRepository voteRepository;
    private final ElectionValidator electionValidator;

    public Page<Election> listAll(@ParameterObject Pageable pageable) {
        return electionRepository.findAll(pageable);
    }

    public Election get(Long id) {
        return electionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Election not found"));
    }

    @Transactional
    public Election create(@Valid ElectionUpsertDto dto) {
        electionValidator.validateDates(dto.startsAt(), dto.endsAt());
        Election e = new Election();
        apply(e, dto);
        return electionRepository.save(e);
    }

    @Transactional
    public Election update(Long id, @Valid ElectionUpsertDto dto) {
        electionValidator.validateDates(dto.startsAt(), dto.endsAt());
        Election e = electionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Election not found"));
        apply(e, dto);
        return electionRepository.save(e);
    }

    @Transactional
    public void delete(Long id) {
        if (!electionRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Election not found");
        }
        electionRepository.deleteById(id);
    }

    public List<ElectionOption> listOptions(Long electionId) {
        electionValidator.ensureElectionExists(electionId);
        return optionRepository.findByElectionId(electionId);
    }

    @Transactional
    public ElectionOption addOption(Long electionId, @Valid ElectionOptionUpsertDto dto) {
        Election election = electionRepository.findById(electionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Election not found"));
        electionValidator.ensureOptionLabelUniqueForCreate(electionId, dto.label());
        ElectionOption opt = new ElectionOption();
        opt.setElection(election);
        opt.setLabel(dto.label());
        return optionRepository.save(opt);
    }

    @Transactional
    public ElectionOption updateOption(Long electionId, Long optionId, @Valid ElectionOptionUpsertDto dto) {
        ElectionOption opt = optionRepository.findById(optionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Option not found"));
        electionValidator.ensureOptionBelongs(opt, electionId);
        electionValidator.ensureOptionLabelUniqueForUpdate(electionId, dto.label(), opt.getLabel());
        opt.setLabel(dto.label());
        return optionRepository.save(opt);
    }

    @Transactional
    public void deleteOption(Long electionId, Long optionId) {
        ElectionOption opt = optionRepository.findById(optionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Option not found"));
        electionValidator.ensureOptionBelongs(opt, electionId);
        optionRepository.deleteById(optionId);
    }

    public java.util.List<VoteRepository.OptionCount> getResults(Long electionId) {
        electionValidator.ensureElectionExists(electionId);
        return voteRepository.countResultsByElection(electionId);
    }

    private void apply(Election e, ElectionUpsertDto dto) {
        e.setName(dto.name());
        e.setDescription(dto.description());
        e.setStartsAt(dto.startsAt());
        e.setEndsAt(dto.endsAt());
    }
}
