package pl.budziosz.votingsystem.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VotePublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.votes:votes}")
    private String votesTopic;

    public void publish(VoteRequestedEvent event) {
        kafkaTemplate.send(votesTopic, String.valueOf(event.voterId()), event);
    }
}
