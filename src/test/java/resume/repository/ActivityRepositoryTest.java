package resume.repository;

import resume.model.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;


import java.time.LocalDate;
import java.util.List;


import static org.assertj.core.api.Assertions.*;


@DataJpaTest
class ActivityRepositoryTest {

    @Autowired
    PersonRepository personRepository;
    @Autowired
    ActivityRepository activityRepository;


    @Test
    void updateActivityFields() {
        var p = personRepository.save(Person.builder()
                .firstName("Ikram").lastName("Loukridi").email("ikram@mail.fr").passwordHash("x".repeat(60)).build());

        var a = activityRepository.save(Activity.builder()
                .person(p).year(2020).type(ActivityType.PROJECT).title("Old").build());

        // UPDATE
        a.setYear(2021);
        a.setType(ActivityType.EDUCATION);
        a.setTitle("New");
        activityRepository.saveAndFlush(a);

        var reloaded = activityRepository.findById(a.getId()).orElseThrow();
        assertThat(reloaded.getYear()).isEqualTo(2021);
        assertThat(reloaded.getType()).isEqualTo(ActivityType.EDUCATION);
        assertThat(reloaded.getTitle()).isEqualTo("New");
    }

    @Test
    void deleteSingleActivity() {
        var p = personRepository.save(Person.builder()
                .firstName("ikram").lastName("Loukridi").email("ikram@mail.fr").passwordHash("x".repeat(60)).build());

        var a = activityRepository.save(Activity.builder()
                .person(p).year(2025).type(ActivityType.PROJECT).title("ToDelete").build());

        assertThat(activityRepository.existsById(a.getId())).isTrue();

        activityRepository.deleteById(a.getId());
        activityRepository.flush();

        assertThat(activityRepository.findById(a.getId())).isEmpty();
    }

    @Test
    void createQueryAndOrphanRemoval() {
        var p = personRepository.save(Person.builder()
                .firstName("Ikram").lastName("Loukridi")
                .email("ikram@mail.fr")
                .birthDate(LocalDate.of(2012,12,12))
                .passwordHash("x".repeat(60))
                .build());

        p.addActivity(Activity.builder().year(2024).type(ActivityType.PROJECT).title("C").build());
        p.addActivity(Activity.builder().year(2025).type(ActivityType.PROJECT).title("X").build());
        personRepository.saveAndFlush(p);

        var page = activityRepository.findByPersonId(p.getId(), org.springframework.data.domain.PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isEqualTo(2);

        for (var a : List.copyOf(p.getActivities())) {
            p.removeActivity(a);
        }
        personRepository.saveAndFlush(p);

        assertThat(activityRepository.count()).isZero();
    }

    @Test
    void deletingPersonAlsoDeletesActivities() {
        var p = personRepository.save(Person.builder()
                .firstName("Ikram").lastName("Loukridi")
                .email("ikram@mail.fr").passwordHash("x".repeat(60)).build());

        p.addActivity(Activity.builder().year(2020).type(ActivityType.PROJECT).title("A").build());
        p.addActivity(Activity.builder().year(2021).type(ActivityType.PROJECT).title("B").build());
        personRepository.saveAndFlush(p);

        personRepository.delete(p);
        personRepository.flush();

        assertThat(activityRepository.findByPersonId(p.getId(), org.springframework.data.domain.PageRequest.of(0,10)).getTotalElements()).isZero();
        assertThat(personRepository.findById(p.getId())).isEmpty();
    }

    @Test
    void queryByEmailAndTop10Order() {
        var p = personRepository.save(Person.builder()
                .firstName("Ikram").lastName("Loukridi")
                .email("ikram@mail.fr").passwordHash("x".repeat(60)).build());

        for (int y = 2010; y < 2025; y++) {
            activityRepository.save(Activity.builder().person(p).year(y).type(ActivityType.PROJECT).title("Y"+y).build());
        }

        var page = activityRepository.findByPersonEmail("ikram@mail.fr",
                org.springframework.data.domain.PageRequest.of(0, 5, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Order.desc("year"))));
        assertThat(page.getTotalElements()).isEqualTo(15);
        assertThat(page.getContent().get(0).getYear()).isEqualTo(2024);

        var top10 = activityRepository.findTop10ByPersonIdOrderByYearDesc(p.getId());
        assertThat(top10).hasSize(10);
        assertThat(top10.get(0).getYear()).isEqualTo(2024);
        assertThat(top10.get(top10.size()-1).getYear()).isEqualTo(2015);
    }
}