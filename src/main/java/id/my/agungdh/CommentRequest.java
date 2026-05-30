package id.my.agungdh;

import jakarta.validation.constraints.NotBlank;

public record CommentRequest(
        @NotBlank String name,
        String email,
        @NotBlank String content
) {}
