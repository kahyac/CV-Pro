package resume.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import resume.model.Person;
import resume.service.PersonService;

import resume.web.dto.activity.ActivityResponseDto;
import resume.web.dto.person.PersonCreateRequestDto;
import resume.web.dto.person.PersonResponseDto;
import resume.web.dto.person.PersonUpdateRequestDto;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/persons")
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;

    @PostMapping
    public PersonResponseDto create(
            @Valid @RequestBody PersonCreateRequestDto dto,
            @RequestAttribute(name = "authPersonId") UUID authId
    ) {
        Person p = personService.createPerson(dto, authId);
        return toDto(p, true);
    }


    @GetMapping("/{id}")
    public PersonResponseDto get(@PathVariable UUID id) {
        var p = personService.getPersonWithActivities(id);
        return toDto(p, true);
    }


    @PutMapping("/{id}")
    public PersonResponseDto updatePerson(
            @PathVariable UUID id,
            @RequestAttribute("authPersonId") UUID authId,
            @Valid @RequestBody PersonUpdateRequestDto dto
    ) {
        var updated = personService.updatePerson(id, authId, dto);
        return toDto(updated, true);
    }


    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable UUID id,
            @RequestAttribute("authPersonId") UUID authId
    ) {
        personService.deletePerson(id, authId);
    }

    @GetMapping
    public Page<PersonResponseDto> list(
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="10") int size
    ) {
        return personService.listPersons(page, size)
                .map(p -> toDto(p, false));
    }

    @GetMapping("/search")
    public Page<PersonResponseDto> search(
            @RequestParam String q,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="10") int size
    ) {
        return personService.searchPersons(q, page, size)
                .map(p -> toDto(p, false));
    }

    private PersonResponseDto toDto(Person p, boolean withActivities) {
        List<ActivityResponseDto> acts = withActivities
                ? p.getActivities().stream()
                .map(a -> new ActivityResponseDto(
                        a.getId(),
                        a.getYear(),
                        a.getType(),
                        a.getTitle(),
                        a.getDescription(),
                        a.getUrl()
                )).toList()
                : List.of();

        return new PersonResponseDto(
                p.getId(),
                p.getFirstName(),
                p.getLastName(),
                p.getEmail(),
                p.getWebsite(),
                p.getBirthDate(),
                acts
        );
    }
}
