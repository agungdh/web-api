package id.my.agungdh;

import java.util.UUID;

public record TagDTO(
        UUID uuid,
        String name,
        String slug
) {}
