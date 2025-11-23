package resume.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import resume.model.*;
import resume.repository.*;
import resume.web.dto.activity.ActivityCreateRequestDto;
import resume.service.exceptions.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock ActivityRepository activityRepository;
    @Mock PersonRepository personRepository;

    @InjectMocks ActivityService activityService;

    // ---------- ADD ACTIVITY ----------

    @Test
    void addActivity_ok_forOwner() {
        var personId = UUID.randomUUID();
        var p = Person.builder().id(personId).email("x@x.com").passwordHash("passss").build();

        when(personRepository.findById(personId)).thenReturn(Optional.of(p));
        when(activityRepository.save(any(Activity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var req = new ActivityCreateRequestDto(
                2025, ActivityType.PROJECT, "E-jury", null, null
        );

        var a = activityService.addActivity(personId, personId, req);

        assertThat(a.getPerson()).isEqualTo(p);
        assertThat(a.getYear()).isEqualTo(2025);
        assertThat(a.getType()).isEqualTo(ActivityType.PROJECT);
        assertThat(a.getTitle()).isEqualTo("E-jury");

        verify(personRepository).findById(personId);
        verify(activityRepository).save(any(Activity.class));
        verifyNoMoreInteractions(personRepository, activityRepository);
    }

    @Test
    void addActivity_forbidden_ifNotOwner() {
        var personId = UUID.randomUUID();
        var otherId = UUID.randomUUID();
        var req = new ActivityCreateRequestDto(2024, ActivityType.PROJECT, "X", null, null);

        assertThatThrownBy(() ->
                activityService.addActivity(personId, otherId, req)
        ).isInstanceOf(ForbiddenException.class);

        verifyNoInteractions(personRepository, activityRepository);
    }

    @Test
    void addActivity_notFound_ifPersonMissing() {
        var personId = UUID.randomUUID();
        var req = new ActivityCreateRequestDto(2024, ActivityType.PROJECT, "X", null, null);

        when(personRepository.findById(personId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                activityService.addActivity(personId, personId, req)
        ).isInstanceOf(NotFoundException.class);

        verify(personRepository).findById(personId);
        verifyNoMoreInteractions(personRepository);
        verifyNoInteractions(activityRepository);
    }

    // ---------- LIST ACTIVITIES ----------

    @Test
    void listActivities_ok_returnsPageSorted() {
        var personId = UUID.randomUUID();
        var page = new PageImpl<Activity>(List.of(
                Activity.builder().year(2024).build(),
                Activity.builder().year(2023).build()
        ));

        when(activityRepository.findByPersonId(eq(personId), any(Pageable.class)))
                .thenReturn(page);

        var result = activityService.listActivities(personId, 0, 10);

        assertThat(result.getTotalElements()).isEqualTo(2);

        verify(activityRepository).findByPersonId(eq(personId), any(Pageable.class));
        verifyNoMoreInteractions(activityRepository);
    }

    // ---------- UPDATE ACTIVITY ----------

    @Test
    void updateActivity_ok_forOwner() {
        var ownerId = UUID.randomUUID();
        var p = Person.builder().id(ownerId).build();
        var actId = UUID.randomUUID();

        var a = Activity.builder()
                .id(actId)
                .person(p)
                .year(2020)
                .type(ActivityType.PROJECT)
                .title("Old")
                .build();

        when(activityRepository.findById(actId)).thenReturn(Optional.of(a));
        when(activityRepository.save(any(Activity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var req = new ActivityCreateRequestDto(
                2021, ActivityType.EDUCATION, "New", "desc", "http://new"
        );

        var updated = activityService.updateActivity(actId, ownerId, req);

        assertThat(updated.getYear()).isEqualTo(2021);
        assertThat(updated.getType()).isEqualTo(ActivityType.EDUCATION);
        assertThat(updated.getTitle()).isEqualTo("New");
        assertThat(updated.getDescription()).isEqualTo("desc");
        assertThat(updated.getUrl()).isEqualTo("http://new");

        verify(activityRepository).findById(actId);
        verify(activityRepository).save(a);
        verifyNoMoreInteractions(activityRepository);
    }

    @Test
    void updateActivity_forbidden_ifNotOwner() {
        var ownerId = UUID.randomUUID();
        var otherId = UUID.randomUUID();
        var p = Person.builder().id(ownerId).build();
        var actId = UUID.randomUUID();

        var a = Activity.builder().id(actId).person(p).build();

        when(activityRepository.findById(actId)).thenReturn(Optional.of(a));

        var req = new ActivityCreateRequestDto(
                2021, ActivityType.EDUCATION, "New", null, null
        );

        assertThatThrownBy(() ->
                activityService.updateActivity(actId, otherId, req)
        ).isInstanceOf(ForbiddenException.class);

        verify(activityRepository).findById(actId);
        verifyNoMoreInteractions(activityRepository);
    }

    @Test
    void updateActivity_notFound_ifMissing() {
        var actId = UUID.randomUUID();
        when(activityRepository.findById(actId)).thenReturn(Optional.empty());

        var req = new ActivityCreateRequestDto(
                2021, ActivityType.EDUCATION, "New", null, null
        );

        assertThatThrownBy(() ->
                activityService.updateActivity(actId, UUID.randomUUID(), req)
        ).isInstanceOf(NotFoundException.class);

        verify(activityRepository).findById(actId);
        verifyNoMoreInteractions(activityRepository);
    }

    // ---------- DELETE ACTIVITY ----------

    @Test
    void deleteActivity_ok_forOwner() {
        var ownerId = UUID.randomUUID();
        var p = Person.builder().id(ownerId).build();
        var actId = UUID.randomUUID();
        var a = Activity.builder().id(actId).person(p).build();

        when(activityRepository.findById(actId)).thenReturn(Optional.of(a));

        activityService.deleteActivity(actId, ownerId);

        verify(activityRepository).findById(actId);
        verify(activityRepository).delete(a);
        verifyNoMoreInteractions(activityRepository);
    }

    @Test
    void deleteActivity_forbidden_ifNotOwner() {
        var ownerId = UUID.randomUUID();
        var otherId = UUID.randomUUID();
        var p = Person.builder().id(ownerId).build();
        var actId = UUID.randomUUID();
        var a = Activity.builder().id(actId).person(p).build();

        when(activityRepository.findById(actId)).thenReturn(Optional.of(a));

        assertThatThrownBy(() ->
                activityService.deleteActivity(actId, otherId)
        ).isInstanceOf(ForbiddenException.class);

        verify(activityRepository).findById(actId);
        verifyNoMoreInteractions(activityRepository);
        verify(activityRepository, never()).delete(any());
    }

    @Test
    void deleteActivity_notFound_ifMissing() {
        var actId = UUID.randomUUID();
        when(activityRepository.findById(actId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                activityService.deleteActivity(actId, UUID.randomUUID())
        ).isInstanceOf(NotFoundException.class);

        verify(activityRepository).findById(actId);
        verifyNoMoreInteractions(activityRepository);
    }
}
