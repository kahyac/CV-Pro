package resume.config;

import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import resume.model.*;
import resume.repository.ActivityRepository;
import resume.repository.PersonRepository;
import resume.web.security.PasswordHasher;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.*;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final PersonRepository personRepository;
    private final ActivityRepository activityRepository;
    private final PasswordHasher passwordHasher;

    private final Faker faker = new Faker();

    @Override
    public void run(String... args) {

        if (personRepository.count() > 0) {
            System.out.println("Database already populated — skipping faker generation.");
            return;
        }

        System.out.println("Starting massive dataset generation with Java Faker");

        final String yacineHash = passwordHasher.hash("yacine");
        final String ikrameHash = passwordHasher.hash("ikrame");
        final String fakerHash  = passwordHasher.hash("password");

        // ------------------ comptes dev ------------------
        Person yacine = Person.builder()
                .firstName("Yacine")
                .lastName("Kartout")
                .email("yacine.kartout@gmail.com")
                .birthDate(LocalDate.of(2000, 3, 12))
                .passwordHash(yacineHash)
                .website("https://yacine.dev")
                .build();

        Person ikrame = Person.builder()
                .firstName("Ikrame")
                .lastName("Loukridi")
                .email("ikrame.loukridi@gmail.com")
                .birthDate(LocalDate.of(2002, 7, 8))
                .passwordHash(ikrameHash)
                .website("https://ikrame.dev")
                .build();

        personRepository.save(yacine);
        personRepository.save(ikrame);

        System.out.println("Added dev accounts: yacine.kartout@gmail.com & ikrame.loukridi@gmail.com");

        // ------------------ faker persons ------------------
        final int PERSON_COUNT = 100_000;
        List<Person> personsBatch = new ArrayList<>(5000);

        for (int i = 1; i <= PERSON_COUNT; i++) {

            String first = faker.name().firstName();
            String last  = faker.name().lastName();

            String email = (clean(first) + "." + clean(last) + i + "@gmail.com").toLowerCase();

            Person p = Person.builder()
                    .firstName(first)
                    .lastName(last)
                    .email(email)
                    .birthDate(LocalDate.of(
                            faker.number().numberBetween(1965, 2005),
                            faker.number().numberBetween(1, 12),
                            faker.number().numberBetween(1, 28)
                    ))
                    .passwordHash(fakerHash)
                    .build();

            personsBatch.add(p);

            if (i % 5000 == 0) {
                personRepository.saveAll(personsBatch);
                personsBatch.clear();
                System.out.println("Inserted persons: " + i);
            }
        }

        if (!personsBatch.isEmpty()) {
            personRepository.saveAll(personsBatch);
        }

        System.out.println("Persons generation complete.");

        // ------------------ faker activities ------------------
        List<Activity> actBatch = new ArrayList<>(10_000);

        var allPersons = personRepository.findAll();
        int counter = 0;

        for (Person p : allPersons) {

            int numActs = faker.number().numberBetween(1, 5);

            for (int j = 0; j < numActs; j++) {

                Activity a = Activity.builder()
                        .year(faker.number().numberBetween(2000, 2025))
                        .type(faker.options().option(ActivityType.class))
                        .title(faker.job().title())
                        .description(faker.lorem().paragraph())
                        .url(faker.internet().url())
                        .person(p)
                        .build();

                actBatch.add(a);
            }

            counter++;

            if (actBatch.size() >= 10_000) {
                activityRepository.saveAll(actBatch);
                actBatch.clear();
                System.out.println("Inserted activities for persons: " + counter);
            }
        }

        if (!actBatch.isEmpty()) {
            activityRepository.saveAll(actBatch);
        }

        System.out.println("DONE: Generated ~100k CV + activities !");
    }

    private String clean(String s) {
        String n = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");
        return n.replaceAll("[^a-zA-Z0-9]", "");
    }
}
