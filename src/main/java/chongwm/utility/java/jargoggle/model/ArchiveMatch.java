package chongwm.utility.java.jargoggle.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ArchiveMatch {
    private final Path archivePath;
    private final List<String> entries = new ArrayList<>();

    public ArchiveMatch(Path archivePath) {
        this.archivePath = Objects.requireNonNull(archivePath, "archivePath");
    }

    public Path archivePath() {
        return archivePath;
    }

    public List<String> entries() {
        return Collections.unmodifiableList(entries);
    }

    public void addEntry(String entry) {
        entries.add(entry);
    }

    public void addEntries(List<String> entryList) {
        entries.addAll(entryList);
    }

    public ArchiveMatch copy() {
        ArchiveMatch copy = new ArchiveMatch(archivePath);
        copy.addEntries(entries);
        return copy;
    }
}
