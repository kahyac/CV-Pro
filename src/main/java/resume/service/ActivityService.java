package resume.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import resume.model.Activity;
import resume.repository.ActivityRepository;
import resume.repository.PersonRepository;
import resume.web.dto.activity.ActivityCreateRequestDto;
import resume.service.exceptions.*;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final PersonRepository personRepository;

    @Transactional
    public Activity addActivity(UUID personId, UUID authPersonId, ActivityCreateRequestDto req) {
        if (!personId.equals(authPersonId)) {
            throw new ForbiddenException("You can modify only your CV");
        }

        var person = personRepository.findById(personId)
                .orElseThrow(() -> new NotFoundException("Person not found"));

        var a = Activity.builder()
                .year(req.year())
                .type(req.type())
                .title(req.title())
                .description(req.description())
                .url(req.url())
                .person(person)
                .build();

        return activityRepository.save(a);
    }

    @Transactional(readOnly = true)
    public Page<Activity> listActivities(UUID personId, int page, int size) {
        return activityRepository.findByPersonId(
                personId,
                PageRequest.of(page, size, Sort.by(Sort.Order.desc("year")))
        );
    }

    @Transactional
    public Activity updateActivity(UUID activityId, UUID authPersonId, ActivityCreateRequestDto req) {
        var a = activityRepository.findById(activityId)
                .orElseThrow(() -> new NotFoundException("Activity not found"));

        if (!a.getPerson().getId().equals(authPersonId)) {
            throw new ForbiddenException("You can modify only your CV");
        }

        a.setYear(req.year());
        a.setType(req.type());
        a.setTitle(req.title());
        a.setDescription(req.description());
        a.setUrl(req.url());

        return activityRepository.save(a);
    }

    @Transactional
    public void deleteActivity(UUID activityId, UUID authPersonId) {
        var a = activityRepository.findById(activityId)
                .orElseThrow(() -> new NotFoundException("Activity not found"));

        if (!a.getPerson().getId().equals(authPersonId)) {
            throw new ForbiddenException("You can modify only your CV");
        }

        activityRepository.delete(a);
    }

    @Transactional(readOnly = true)
    public Page<Activity> searchByTitle(String q, int page, int size) {
        return activityRepository.findByTitleContainingIgnoreCase(
                q,
                PageRequest.of(page, size, Sort.by(Sort.Order.desc("year")))
        );
    }
}
