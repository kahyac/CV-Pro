package resume.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import resume.model.Person;
import resume.repository.PersonRepository;
import resume.web.security.PasswordHasher;
import resume.web.dto.person.PersonCreateRequestDto;
import resume.service.exceptions.*;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

    @Mock
    PersonRepository personRepository;

    @Mock
    PasswordHasher passwordHasher;

    @InjectMocks
    PersonService personService;

    // ---------- CREATE PERSON ----------

    @Test
    void createPerson_ok_whenEmailFree_andCreatorValid() {
        var req = new PersonCreateRequestDto(
                "a", "b", "a@b.com",
                null, LocalDate.of(1990, 1, 1), "pass"
        );

        UUID creatorId = UUID.randomUUID();

        when(personRepository.findById(creatorId)).thenReturn(Optional.of(mock(Person.class)));
        when(personRepository.existsByEmail("a@b.com")).thenReturn(false);
        when(passwordHasher.hash("pass")).thenReturn("HASH");

        when(personRepository.save(any(Person.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var saved = personService.createPerson(req, creatorId);

        assertThat(saved.getFirstName()).isEqualTo("a");
        assertThat(saved.getEmail()).isEqualTo("a@b.com");
        assertThat(saved.getPasswordHash()).isEqualTo("HASH");

        verify(personRepository).findById(creatorId);
        verify(personRepository).existsByEmail("a@b.com");
        verify(passwordHasher).hash("pass");
        verify(personRepository).save(any(Person.class));
        verifyNoMoreInteractions(personRepository, passwordHasher);
    }

    @Test
    void createPerson_ok_whenCreatorAuthenticated() {
        var creatorId = UUID.randomUUID();
        var req = new PersonCreateRequestDto(
                "x", "x", "x@x.com",
                null, null, "x"
        );

        when(personRepository.findById(creatorId))
                .thenReturn(Optional.of(Person.builder().id(creatorId).build()));
        when(personRepository.existsByEmail("x@x.com")).thenReturn(false);
        when(passwordHasher.hash("x")).thenReturn("HASH2");
        when(personRepository.save(any(Person.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var saved = personService.createPerson(req, creatorId);

        assertThat(saved.getEmail()).isEqualTo("x@x.com");
        assertThat(saved.getPasswordHash()).isEqualTo("HASH2");

        verify(personRepository).findById(creatorId);
        verify(personRepository).existsByEmail("x@x.com");
        verify(passwordHasher).hash("x");
        verify(personRepository).save(any(Person.class));
        verifyNoMoreInteractions(personRepository, passwordHasher);
    }

    @Test
    void createPerson_forbidden_whenCreatorNotFound() {
        var creatorId = UUID.randomUUID();
        var req = new PersonCreateRequestDto(
                "X", "Y", "x@y.com", null, null, "passss"
        );

        when(personRepository.findById(creatorId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> personService.createPerson(req, creatorId))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("authenticated");

        verify(personRepository).findById(creatorId);
        verifyNoMoreInteractions(personRepository, passwordHasher);
    }

    @Test
    void createPerson_conflict_whenEmailExists() {
        var req = new PersonCreateRequestDto(
                "a", "b", "a@b.com",
                null, null, "passss"
        );

        UUID creatorId = UUID.randomUUID();

        // creatorId doit être valide sinon on tombe avant sur Forbidden
        when(personRepository.findById(creatorId)).thenReturn(Optional.of(mock(Person.class)));
        when(personRepository.existsByEmail("a@b.com")).thenReturn(true);

        assertThatThrownBy(() -> personService.createPerson(req, creatorId))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email");

        verify(personRepository).findById(creatorId);
        verify(personRepository).existsByEmail("a@b.com");
        verifyNoMoreInteractions(personRepository, passwordHasher);
    }

    // ---------- READ PERSON ----------

    @Test
    void getPersonWithActivities_ok() {
        var id = UUID.randomUUID();
        var p = Person.builder().id(id).email("a@b.com").build();

        when(personRepository.findWithActivitiesById(id))
                .thenReturn(Optional.of(p));

        var fetched = personService.getPersonWithActivities(id);

        assertThat(fetched).isSameAs(p);

        verify(personRepository).findWithActivitiesById(id);
        verifyNoMoreInteractions(personRepository);
    }

    @Test
    void getPersonWithActivities_notFound() {
        var id = UUID.randomUUID();
        when(personRepository.findWithActivitiesById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> personService.getPersonWithActivities(id))
                .isInstanceOf(NotFoundException.class);

        verify(personRepository).findWithActivitiesById(id);
        verifyNoMoreInteractions(personRepository);
    }

    // ---------- DELETE PERSON ----------

    @Test
    void deletePerson_ok_forOwner() {
        var id = UUID.randomUUID();
        var p = Person.builder().id(id).build();

        when(personRepository.findById(id)).thenReturn(Optional.of(p));

        personService.deletePerson(id, id);

        verify(personRepository).findById(id);
        verify(personRepository).delete(p);
        verifyNoMoreInteractions(personRepository);
    }

    @Test
    void deletePerson_forbidden_ifNotOwner() {
        var id = UUID.randomUUID();
        var other = UUID.randomUUID();

        assertThatThrownBy(() -> personService.deletePerson(id, other))
                .isInstanceOf(ForbiddenException.class);

        verifyNoInteractions(personRepository);
    }

    @Test
    void deletePerson_notFound_whenMissing() {
        var id = UUID.randomUUID();
        when(personRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> personService.deletePerson(id, id))
                .isInstanceOf(NotFoundException.class);

        verify(personRepository).findById(id);
        verifyNoMoreInteractions(personRepository);
    }
}
