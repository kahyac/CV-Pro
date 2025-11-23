package resume.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resume.model.Person;
import resume.repository.PersonRepository;
import resume.web.security.PasswordHasher;
import resume.web.dto.person.PersonCreateRequestDto;
import resume.service.exceptions.*;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PersonService {

    private final PersonRepository personRepository;
    private final PasswordHasher passwordHasher;

    @Transactional
    public Person createPerson(PersonCreateRequestDto req, UUID creatorId) {
        if (creatorId == null) {
            throw new ForbiddenException("You must be authenticated to create a person (cooptation)");
        }

        if (personRepository.findById(creatorId).isEmpty()) {
            throw new ForbiddenException("Creator must be authenticated");
        }

        if (personRepository.existsByEmail(req.email())) {
            throw new ConflictException("Email already used");
        }

        var p = Person.builder()
                .firstName(req.firstName())
                .lastName(req.lastName())
                .email(req.email())
                .website(req.website())
                .birthDate(req.birthDate())
                .passwordHash(passwordHasher.hash(req.rawPassword()))
                .build();

        return personRepository.save(p);
    }

    @Transactional(readOnly = true)
    public Person getPersonWithActivities(UUID id) {
        return personRepository.findWithActivitiesById(id)
                .orElseThrow(() -> new NotFoundException("Person not found"));
    }

    @Transactional
    public Person updateWebsite(UUID personId, UUID authPersonId, String website) {
        if (!personId.equals(authPersonId)) {
            throw new ForbiddenException("You can update only your profile");
        }

        var p = personRepository.findById(personId)
                .orElseThrow(() -> new NotFoundException("Person not found"));

        p.setWebsite(website);
        return personRepository.save(p);
    }

    @Transactional
    public void deletePerson(UUID personId, UUID authPersonId) {
        if (!personId.equals(authPersonId)) {
            throw new ForbiddenException("You can delete only your profile");
        }
        var p = personRepository.findById(personId)
                .orElseThrow(() -> new NotFoundException("Person not found"));

        personRepository.delete(p);
    }

    @Transactional(readOnly = true)
    public Page<Person> listPersons(int page, int size) {
        return personRepository.findAll(PageRequest.of(page, size, Sort.by("lastName").ascending()));
    }

    @Transactional(readOnly = true)
    public Page<Person> searchPersons(String q, int page, int size) {
        return personRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                        q, q,
                        PageRequest.of(page, size, Sort.by("lastName").ascending())
                );
    }
}
