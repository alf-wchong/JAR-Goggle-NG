package chongwm.utility.java.jargoggle.model;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public record SearchOptions(
        String query,
        SearchMode mode,
        boolean recursive,
        SearchTargetScope targetScope,
        CombineMode combineMode,
        List<Path> roots,
        List<Path> seedArchives
) {
    public SearchOptions {
        Objects.requireNonNull(query, "query");
        Objects.requireNonNull(mode, "mode");
        Objects.requireNonNull(targetScope, "targetScope");
        Objects.requireNonNull(combineMode, "combineMode");
        roots = List.copyOf(roots);
        seedArchives = List.copyOf(seedArchives);
    }
}
