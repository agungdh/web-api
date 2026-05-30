package id.my.agungdh.dto;

import jakarta.validation.constraints.NotBlank;

public record TagRequest(
        @NotBlank String name,
        String slug
) {}
