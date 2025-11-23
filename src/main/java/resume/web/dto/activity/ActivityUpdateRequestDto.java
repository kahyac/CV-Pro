package resume.web.dto.activity;

import jakarta.validation.constraints.*;
import resume.model.ActivityType;

public record ActivityUpdateRequestDto(

        @Min(1900)
        @Max(3000)
        Integer year,

        ActivityType type,

        @Size(max = 200)
        String title,

        @Size(max = 2000)
        String description,

        @Size(max = 255)
        @Pattern(
                regexp = "^(https?://).*$",
                message = "url must start with http:// or https://"
        )
        String url
) {}
