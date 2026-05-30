package id.my.agungdh;

import jakarta.validation.constraints.NotBlank;

public record TagRequest(
        @NotBlank String name,
        String slug
) {}
