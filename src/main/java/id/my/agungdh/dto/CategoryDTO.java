package id.my.agungdh.dto;

import java.util.UUID;

public record CategoryDTO(
        UUID uuid,
        String name,
        String slug
) {}
