package id.my.agungdh;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PostDTO(
        UUID uuid,
        String title,
        String slug,
        String content,
        Instant publishedAt,
        CategoryDTO category,
        List<TagDTO> tags,
        Instant createdAt
) {}
