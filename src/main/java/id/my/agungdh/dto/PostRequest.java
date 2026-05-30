package id.my.agungdh.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;

public record PostRequest(
        @NotBlank String title,
        String slug,
        String content,
        UUID categoryUuid,
        List<UUID> tagUuids
) {}
