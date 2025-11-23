package resume.web.dto.person;

import java.time.LocalDate;
import jakarta.validation.constraints.*;

public record PersonUpdateRequestDto(

        @Size(max = 100)
        String firstName,

        @Size(max = 100)
        String lastName,

        @Size(max = 255)
        @Pattern(
                regexp = "^(https?://).*$",
                message = "website must start with http:// or https://"
        )
        String website,

        @Past
        LocalDate birthDate
) {}
