package resume.web.dto.person;

import java.time.LocalDate;
import jakarta.validation.constraints.*;

public record PersonCreateRequestDto(

        @NotBlank
        @Size(max = 100)
        String firstName,

        @NotBlank
        @Size(max = 100)
        String lastName,

        @NotBlank
        @Email
        @Size(max = 255)
        String email,

        @Size(max = 255)
        @Pattern(
                regexp = "^(https?://).*$",
                message = "website must start with http:// or https://"
        )
        String website,

        @Past
        LocalDate birthDate,

        @NotBlank
        @Size(min = 6, max = 100)
        String rawPassword
) {}
