package resume.config;

import resume.model.*;
import resume.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.bcrypt.BCrypt;


import java.time.LocalDate;


@Configuration
public class DataInit {
    @Bean
    CommandLineRunner seed(PersonRepository personRepo) {
        return args -> {
            if (personRepo.count() > 0) return;
            var hash = BCrypt.hashpw("password", BCrypt.gensalt());
            var p = Person.builder()
                    .firstName("Ada").lastName("Lovelace")
                    .email("ada@example.com")
                    .website("https://ada.dev")
                    .birthDate(LocalDate.of(1815, 12, 10))
                    .passwordHash(hash)
                    .build();
            p.addActivity(Activity.builder().year(1843).type(ActivityType.PROJECT).title("Notes on the Analytical Engine").build());
            personRepo.save(p);
        };
    }
}