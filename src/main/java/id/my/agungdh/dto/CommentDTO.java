package id.my.agungdh.dto;

import java.time.Instant;
import java.util.UUID;

public record CommentDTO(
        UUID uuid,
        String name,
        String email,
        String content,
        Instant createdAt
) {}
