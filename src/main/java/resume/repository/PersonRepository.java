package resume.repository;

import resume.model.Person;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;


import java.util.*;


public interface PersonRepository extends JpaRepository<Person, UUID> {

    Optional<Person> findByEmail(String email);

    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = {"activities"})
    Optional<Person> findWithActivitiesById(UUID id);

    Page<Person> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String first, String last, Pageable pageable
    );

    Page<Person> findAll(Pageable pageable);
}