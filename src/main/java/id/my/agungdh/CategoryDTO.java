package id.my.agungdh;

import java.util.UUID;

public record CategoryDTO(
        UUID uuid,
        String name,
        String slug
) {}
