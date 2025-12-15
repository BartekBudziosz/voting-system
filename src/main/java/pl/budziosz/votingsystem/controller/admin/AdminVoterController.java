package pl.budziosz.votingsystem.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.budziosz.votingsystem.model.domain.Voter;
import pl.budziosz.votingsystem.service.AdminVoterService;
import pl.budziosz.votingsystem.model.dto.VoterDtos.VoterResponse;
import pl.budziosz.votingsystem.model.dto.VoterDtos.VoterUpsertDto;

@RestController
@RequestMapping("/api/admin/voters")
@RequiredArgsConstructor
public class AdminVoterController {

    private final AdminVoterService voterService;

    @GetMapping
    public Page<VoterResponse> list(@ParameterObject Pageable pageable) {
        return voterService.listAll(pageable).map(this::toResponse);
    }

    @GetMapping("/{id}")
    public VoterResponse get(@PathVariable Long id) {
        Voter v = voterService.get(id);
        return toResponse(v);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VoterResponse create(@Valid @RequestBody VoterUpsertDto dto) {
        Voter v = voterService.create(dto);
        return toResponse(v);
    }

    @PutMapping("/{id}")
    public VoterResponse update(@PathVariable Long id, @Valid @RequestBody VoterUpsertDto dto) {
        Voter v = voterService.update(id, dto);
        return toResponse(v);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        voterService.delete(id);
    }

    @PatchMapping("/{id}/block")
    public VoterResponse block(@PathVariable Long id) {
        Voter v = voterService.block(id);
        return toResponse(v);
    }

    @PatchMapping("/{id}/unblock")
    public VoterResponse unblock(@PathVariable Long id) {
        Voter v = voterService.unblock(id);
        return toResponse(v);
    }

    private VoterResponse toResponse(Voter v) {
        return new VoterResponse(v.getId(), v.getEmail(), v.getFullName(), v.isBlocked());
    }
}
