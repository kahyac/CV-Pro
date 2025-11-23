package resume.web.dto.person;

import resume.web.dto.activity.ActivityResponseDto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PersonResponseDto(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String website,
        LocalDate birthDate,
        List<ActivityResponseDto> activities
) {}
