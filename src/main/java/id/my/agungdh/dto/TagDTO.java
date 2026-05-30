package id.my.agungdh.dto;

import java.util.UUID;

public record TagDTO(
        UUID uuid,
        String name,
        String slug
) {}
