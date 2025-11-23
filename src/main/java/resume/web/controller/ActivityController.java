package resume.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import resume.service.ActivityService;
import resume.web.dto.activity.ActivityCreateRequestDto;
import resume.web.dto.activity.ActivityResponseDto;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;


    @PostMapping("/api/persons/{personId}/activities")
    public ActivityResponseDto add(
            @PathVariable UUID personId,
            @RequestAttribute("authPersonId") UUID authId,
            @Valid @RequestBody ActivityCreateRequestDto dto
    ) {
        var a = activityService.addActivity(personId, authId, dto);

        return new ActivityResponseDto(
                a.getId(),
                a.getYear(),
                a.getType(),
                a.getTitle(),
                a.getDescription(),
                a.getUrl()
        );
    }


    @GetMapping("/api/persons/{personId}/activities")
    public Page<ActivityResponseDto> list(
            @PathVariable UUID personId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return activityService.listActivities(personId, page, size)
                .map(a -> new ActivityResponseDto(
                        a.getId(),
                        a.getYear(),
                        a.getType(),
                        a.getTitle(),
                        a.getDescription(),
                        a.getUrl()
                ));
    }


    @PutMapping("/api/activities/{activityId}")
    public ActivityResponseDto update(
            @PathVariable UUID activityId,
            @RequestAttribute("authPersonId") UUID authId,
            @Valid @RequestBody ActivityCreateRequestDto dto
    ) {
        var a = activityService.updateActivity(activityId, authId, dto);

        return new ActivityResponseDto(
                a.getId(),
                a.getYear(),
                a.getType(),
                a.getTitle(),
                a.getDescription(),
                a.getUrl()
        );
    }


    @DeleteMapping("/api/activities/{activityId}")
    public void delete(
            @PathVariable UUID activityId,
            @RequestAttribute("authPersonId") UUID authId
    ) {
        System.out.println("🗑️ Controller DELETE activityId = " + activityId);
        System.out.println("🗑️ Controller authPersonId = " + authId);

        activityService.deleteActivity(activityId, authId);
    }

    @GetMapping("/api/activities/search")
    public Page<ActivityResponseDto> search(
            @RequestParam String q,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="10") int size
    ) {
        return activityService.searchByTitle(q, page, size)
                .map(a -> new ActivityResponseDto(
                        a.getId(), a.getYear(), a.getType(),
                        a.getTitle(), a.getDescription(), a.getUrl()
                ));
    }
}
