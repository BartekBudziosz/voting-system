package pl.budziosz.votingsystem.controller.admin;

import jakarta.validation.Valid;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.budziosz.votingsystem.model.domain.Election;
import pl.budziosz.votingsystem.model.domain.ElectionOption;
import pl.budziosz.votingsystem.repository.VoteRepository;
import pl.budziosz.votingsystem.service.AdminElectionService;
import pl.budziosz.votingsystem.model.dto.ElectionDtos.ElectionResponse;
import pl.budziosz.votingsystem.model.dto.ElectionDtos.ElectionUpsertDto;
import pl.budziosz.votingsystem.model.dto.ElectionOptionDtos.ElectionOptionResponse;
import pl.budziosz.votingsystem.model.dto.ElectionOptionDtos.ElectionOptionUpsertDto;
import pl.budziosz.votingsystem.model.dto.ElectionResultsDtos.OptionResult;

@RestController
@RequestMapping("/api/admin/elections")
@RequiredArgsConstructor
public class AdminElectionController {

    private final AdminElectionService electionService;

    @GetMapping
    public Page<ElectionResponse> list(@ParameterObject Pageable pageable) {
        return electionService.listAll(pageable).map(this::toResponse);
    }

    @GetMapping("/{id}")
    public ElectionResponse get(@PathVariable Long id) {
        Election e = electionService.get(id);
        return toResponse(e);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ElectionResponse create(@Valid @RequestBody ElectionUpsertDto dto) {
        Election e = electionService.create(dto);
        return toResponse(e);
    }

    @PutMapping("/{id}")
    public ElectionResponse update(@PathVariable Long id, @Valid @RequestBody ElectionUpsertDto dto) {
        Election e = electionService.update(id, dto);
        return toResponse(e);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        electionService.delete(id);
    }


    @GetMapping("/{electionId}/options")
    public List<ElectionOptionResponse> listOptions(@PathVariable Long electionId) {
        return electionService.listOptions(electionId).stream().map(this::toResponse).toList();
    }

    @PostMapping("/{electionId}/options")
    @ResponseStatus(HttpStatus.CREATED)
    public ElectionOptionResponse addOption(@PathVariable Long electionId, @Valid @RequestBody ElectionOptionUpsertDto dto) {
        ElectionOption opt = electionService.addOption(electionId, dto);
        return toResponse(opt);
    }

    @PutMapping("/{electionId}/options/{optionId}")
    public ElectionOptionResponse updateOption(@PathVariable Long electionId, @PathVariable Long optionId, @Valid @RequestBody ElectionOptionUpsertDto dto) {
        ElectionOption opt = electionService.updateOption(electionId, optionId, dto);
        return toResponse(opt);
    }

    @DeleteMapping("/{electionId}/options/{optionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOption(@PathVariable Long electionId, @PathVariable Long optionId) {
        electionService.deleteOption(electionId, optionId);
    }

    @GetMapping("/{electionId}/results")
    public List<OptionResult> results(@PathVariable Long electionId) {
        List<VoteRepository.OptionCount> counts = electionService.getResults(electionId);
        return counts.stream()
                .map(c -> new OptionResult(c.getOptionId(), c.getLabel(), c.getVotes()))
                .toList();
    }

    private ElectionResponse toResponse(Election e) {
        return new ElectionResponse(e.getId(), e.getName(), e.getDescription(), e.getStartsAt(), e.getEndsAt());
    }

    private ElectionOptionResponse toResponse(ElectionOption option) {
        return new ElectionOptionResponse(option.getId(), option.getLabel());
    }
}
