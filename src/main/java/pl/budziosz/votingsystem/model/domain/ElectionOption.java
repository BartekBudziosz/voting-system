package pl.budziosz.votingsystem.model.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "election_options",
       uniqueConstraints = {
           @UniqueConstraint(name = "election_options_election_label_key", columnNames = {"election_id", "label"})
       },
       indexes = {
           @Index(name = "idx_option_election", columnList = "election_id")
       })
@Getter
@Setter
@NoArgsConstructor
public class ElectionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "election_id", nullable = false)
    private Election election;

    @Column(nullable = false)
    private String label;
}
