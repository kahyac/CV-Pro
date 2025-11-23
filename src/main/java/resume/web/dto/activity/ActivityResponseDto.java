package resume.web.dto.activity;

import resume.model.ActivityType;
import java.util.UUID;

public record ActivityResponseDto(
        UUID id,
        int year,
        ActivityType type,
        String title,
        String description,
        String url
) {}
