package resume.repository;

import org.springframework.data.repository.query.Param;
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

    @Query(
            value = """
            select distinct p
            from Person p
            left join p.activities a
            where lower(p.firstName) like lower(concat('%', :q, '%'))
               or lower(p.lastName) like lower(concat('%', :q, '%'))
               or lower(a.title) like lower(concat('%', :q, '%'))
        """,
            countQuery = """
            select count(distinct p)
            from Person p
            left join p.activities a
            where lower(p.firstName) like lower(concat('%', :q, '%'))
               or lower(p.lastName) like lower(concat('%', :q, '%'))
               or lower(a.title) like lower(concat('%', :q, '%'))
        """
    )
    Page<Person> searchByNameOrActivityTitle(@Param("q") String q, Pageable pageable);


    Page<Person> findAll(Pageable pageable);
}