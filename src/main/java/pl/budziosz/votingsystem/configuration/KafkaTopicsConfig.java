package pl.budziosz.votingsystem.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfig {

    @Value("${app.kafka.topics.votes:votes}")
    private String votesTopicName;

    @Value("${app.kafka.topics.votes.partitions:1}")
    private int votesTopicPartitions;

    @Value("${app.kafka.topics.votes.replicas:1}")
    private short votesTopicReplicas;

    @Bean
    public NewTopic votesTopic() {
        return TopicBuilder.name(votesTopicName)
                .partitions(votesTopicPartitions)
                .replicas(votesTopicReplicas)
                .build();
    }
}
