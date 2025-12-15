package pl.budziosz.votingsystem.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.budziosz.votingsystem.service.VotingService;

@Component
@RequiredArgsConstructor
@Slf4j
public class VoteConsumer {

    private final VotingService votingService;

    @KafkaListener(topics = "${app.kafka.topics.votes:votes}", groupId = "vote-consumers")
    @Transactional
    public void handle(VoteRequestedEvent e) {
        try {
            votingService.processEvent(e);
        } catch (Exception ex) {
            log.warn("Vote event {} rejected: {}", e.eventId(), ex.getMessage());
        }
    }
}
