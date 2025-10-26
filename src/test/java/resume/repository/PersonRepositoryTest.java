package resume.repository;



import resume.model.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import org.springframework.dao.DataIntegrityViolationException;


import java.time.LocalDate;


import static org.assertj.core.api.Assertions.*;


@DataJpaTest
class PersonRepositoryTest {


    @Autowired
    PersonRepository personRepository;


    @Test
    void createReadUpdateDeletePerson() {
        var p = Person.builder()
                .firstName("Yacine")
                .lastName("Kartout")
                .email("yacine@mail.fr")
                .birthDate(LocalDate.of(2012, 12, 12))
                .passwordHash("$2a$10$12345678901234567890123456789012345678901234567890123")
                .build();



// CREATE
        var saved = personRepository.save(p);
        assertThat(saved.getId()).isNotNull();


// READ
        var fetched = personRepository.findByEmail("yacine@mail.fr");
        assertThat(fetched).isPresent();
        assertThat(fetched.get().getFirstName()).isEqualTo("Yacine");


// UPDATE
        fetched.get().setWebsite("https://yacine.com");
        var updated = personRepository.save(fetched.get());
        assertThat(updated.getWebsite()).isEqualTo("https://yacine.com");


// DELETE
        personRepository.delete(updated);
        assertThat(personRepository.findByEmail("yacine@mail.fr")).isEmpty();
    }


    @Test
    void uniqueEmailConstraint() {
        var p1 = Person.builder()
                .firstName("A")
                .lastName("A")
                .email("unique@mail.fr")
                .passwordHash("$2a$10$12345678901234567890123456789012345678901234567890123")
                .build();
        var p2 = Person.builder()
                .firstName("B")
                .lastName("B")
                .email("unique@mail.fr")
                .passwordHash("$2a$10$12345678901234567890123456789012345678901234567890123")
                .build();


        personRepository.saveAndFlush(p1);
        assertThatThrownBy(() -> personRepository.saveAndFlush(p2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}