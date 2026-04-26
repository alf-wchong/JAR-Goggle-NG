package chongwm.utility.java.jargoggle.util;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public final class PreferencesManager {
    private static final Preferences PREFS = Preferences.userRoot().node("com/example/jargoggle");
    private static final String KEY_ROOTS = "lastOpenedPaths";

    private PreferencesManager() {
    }

    public static List<Path> loadRoots() {
        String raw = PREFS.get(KEY_ROOTS, "");
        if (raw.isBlank()) {
            return List.of();
        }
        List<Path> roots = new ArrayList<>();
        for (String token : raw.split("\\n")) {
            if (!token.isBlank()) {
                roots.add(Path.of(token));
            }
        }
        return roots;
    }

    public static void saveRoots(List<Path> roots) {
        String value = roots.stream()
                .map(Path::toAbsolutePath)
                .map(Path::normalize)
                .map(Path::toString)
                .distinct()
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");
        PREFS.put(KEY_ROOTS, value);
    }
}
