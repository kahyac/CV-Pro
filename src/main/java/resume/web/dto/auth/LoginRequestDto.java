package resume.web.dto.auth;

import jakarta.validation.constraints.*;

public record LoginRequestDto(
        @NotBlank
        @Email
        @Size(max = 255)
        String email,

        @NotBlank
        @Size(min = 6, max = 100)
        String password
) {}
