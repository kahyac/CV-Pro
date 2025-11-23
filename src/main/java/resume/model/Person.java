package resume.model;


import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.*;
import lombok.*;


import java.time.LocalDate;
import java.util.*;


@Entity
@Table(name = "persons", indexes = {
        @Index(name = "idx_person_email", columnList = "email", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Person {


    @Id
    @GeneratedValue
    private UUID id;


    @NotBlank
    @Size(max = 100)
    private String firstName;


    @NotBlank
    @Size(max = 100)
    private String lastName;


    @NotBlank
    @Email
    @Size(max = 255)
    @Column(nullable = false, unique = true)
    private String email;


    @Size(max = 255)
    private String website;


    @Past
    private LocalDate birthDate;


    @NotBlank
    @Size(min = 6, max = 100)
    @Column(nullable = false, length = 100)
    private String passwordHash;


    @Builder.Default
    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Activity> activities = new ArrayList<>();


    public void addActivity(Activity a) {
        a.setPerson(this);
        activities.add(a);
    }


    public void removeActivity(Activity a) {
        activities.remove(a);
        a.setPerson(null);
    }
}