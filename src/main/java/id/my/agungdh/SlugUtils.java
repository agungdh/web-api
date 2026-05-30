package id.my.agungdh;

public class SlugUtils {
    public static String toSlug(String input) {
        if (input == null || input.isBlank()) return null;
        return input.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}
