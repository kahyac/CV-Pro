package resume.repository;

import resume.model.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;


import java.util.*;


public interface ActivityRepository extends JpaRepository<Activity, UUID> {

    Page<Activity> findByPersonId(UUID personId, Pageable pageable);

    Page<Activity> findByPersonEmail(String email, Pageable pageable);

    List<Activity> findTop10ByPersonIdOrderByYearDesc(UUID personId);

    Page<Activity> findByTitleContainingIgnoreCase(String q, Pageable pageable);
}