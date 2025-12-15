package pl.budziosz.votingsystem.controller.user;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.budziosz.votingsystem.model.domain.Election;
import pl.budziosz.votingsystem.kafka.VotePublisher;
import pl.budziosz.votingsystem.kafka.VoteRequestedEvent;
import pl.budziosz.votingsystem.service.UserVotingService;
import pl.budziosz.votingsystem.model.dto.ElectionDtos.ElectionResponse;
import pl.budziosz.votingsystem.model.dto.ElectionOptionDtos.ElectionOptionResponse;
import pl.budziosz.votingsystem.model.dto.VoteDtos.VoteAcceptedResponse;
import pl.budziosz.votingsystem.model.dto.VoteDtos.VoteRequest;

@RestController
@RequestMapping("/api/elections")
@RequiredArgsConstructor
public class UserVotingController {

    private final UserVotingService userVotingService;
    private final VotePublisher votePublisher;

    @GetMapping("/active")
    public List<ElectionResponse> listActive() {
        Instant now = Instant.now();
        return userVotingService.listActive(now).stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{electionId}/options")
    public List<ElectionOptionResponse> listOptions(@PathVariable Long electionId) {
        return userVotingService.listOptions(electionId).stream()
                .map(o -> new ElectionOptionResponse(o.getId(), o.getLabel()))
                .toList();
    }

    @PostMapping("/{electionId}/votes")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public VoteAcceptedResponse voteAsync(@PathVariable Long electionId, @Valid @RequestBody VoteRequest req) {
        Election e = userVotingService.getElection(electionId);
        String eventId = UUID.randomUUID().toString();
        VoteRequestedEvent event = new VoteRequestedEvent(
                eventId,
                e.getId(),
                req.voterId(),
                req.optionId(),
                Instant.now()
        );
        votePublisher.publish(event);
        return new VoteAcceptedResponse("accepted", eventId);
    }

    private ElectionResponse toResponse(Election e) {
        return new ElectionResponse(e.getId(), e.getName(), e.getDescription(), e.getStartsAt(), e.getEndsAt());
    }
}
