package resume.model;

import jakarta.persistence.*;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;


@Entity
@Table(name = "activities", indexes = {
        @Index(name = "idx_activity_person_year", columnList = "person_id, year DESC")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Activity {


    @Id
    @GeneratedValue
    private UUID id;


    @Min(1900)
    @Max(3000)
    @Column(nullable = false)
    private int year;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ActivityType type;


    @NotBlank
    @Size(max = 200)
    @Column(nullable = false)
    private String title;


    @Lob
    private String description;


    @Size(max = 255)
    private String url;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;
}
