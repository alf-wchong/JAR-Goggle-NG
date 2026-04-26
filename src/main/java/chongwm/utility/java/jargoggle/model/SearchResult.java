package chongwm.utility.java.jargoggle.model;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class SearchResult {
    private final String name;
    private final String description;
    private final Instant createdAt;
    private final Map<Path, ArchiveMatch> matchesByArchive;

    public SearchResult(String name, String description, Map<Path, ArchiveMatch> matchesByArchive) {
        this.name = Objects.requireNonNull(name, "name");
        this.description = Objects.requireNonNull(description, "description");
        this.createdAt = Instant.now();
        this.matchesByArchive = new LinkedHashMap<>(Objects.requireNonNull(matchesByArchive, "matchesByArchive"));
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Collection<ArchiveMatch> matches() {
        return Collections.unmodifiableCollection(matchesByArchive.values());
    }

    public Map<Path, ArchiveMatch> matchesByArchive() {
        return Collections.unmodifiableMap(matchesByArchive);
    }

    public List<Path> archivePaths() {
        return new ArrayList<>(matchesByArchive.keySet());
    }

    public int archiveCount() {
        return matchesByArchive.size();
    }

    public int entryCount() {
        return matchesByArchive.values().stream().mapToInt(m -> m.entries().size()).sum();
    }

    public boolean isEmpty() {
        return matchesByArchive.isEmpty();
    }

    public SearchResult copyWithName(String newName, String newDescription) {
        Map<Path, ArchiveMatch> copied = new LinkedHashMap<>();
        for (Map.Entry<Path, ArchiveMatch> entry : matchesByArchive.entrySet()) {
            copied.put(entry.getKey(), entry.getValue().copy());
        }
        return new SearchResult(newName, newDescription, copied);
    }
}
