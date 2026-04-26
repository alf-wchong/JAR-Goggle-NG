package chongwm.utility.java.jargoggle.service;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import chongwm.utility.java.jargoggle.model.ArchiveMatch;
import chongwm.utility.java.jargoggle.model.SearchResult;

public final class ResultSetOps {
    private ResultSetOps() {
    }

    public static SearchResult union(SearchResult left, SearchResult right, String name, String description) {
        Map<Path, ArchiveMatch> merged = copy(left);
        for (Map.Entry<Path, ArchiveMatch> entry : right.matchesByArchive().entrySet()) {
            merged.merge(entry.getKey(), entry.getValue().copy(), (a, b) -> {
                ArchiveMatch joined = new ArchiveMatch(a.archivePath());
                joined.addEntries(a.entries());
                for (String value : b.entries()) {
                    if (!joined.entries().contains(value)) {
                        joined.addEntry(value);
                    }
                }
                return joined;
            });
        }
        return new SearchResult(name, description, merged);
    }

    public static SearchResult intersect(SearchResult left, SearchResult right, String name, String description) {
        Map<Path, ArchiveMatch> intersected = new LinkedHashMap<>();
        for (Map.Entry<Path, ArchiveMatch> entry : left.matchesByArchive().entrySet()) {
            ArchiveMatch other = right.matchesByArchive().get(entry.getKey());
            if (other != null) {
                ArchiveMatch result = new ArchiveMatch(entry.getKey());
                for (String value : entry.getValue().entries()) {
                    if (other.entries().contains(value)) {
                        result.addEntry(value);
                    }
                }
                if (!result.entries().isEmpty()) {
                    intersected.put(entry.getKey(), result);
                }
            }
        }
        return new SearchResult(name, description, intersected);
    }

    public static SearchResult subtract(SearchResult left, SearchResult right, String name, String description) {
        Map<Path, ArchiveMatch> remaining = new LinkedHashMap<>();
        for (Map.Entry<Path, ArchiveMatch> entry : left.matchesByArchive().entrySet()) {
            ArchiveMatch other = right.matchesByArchive().get(entry.getKey());
            if (other == null) {
                remaining.put(entry.getKey(), entry.getValue().copy());
                continue;
            }
            ArchiveMatch result = new ArchiveMatch(entry.getKey());
            for (String value : entry.getValue().entries()) {
                if (!other.entries().contains(value)) {
                    result.addEntry(value);
                }
            }
            if (!result.entries().isEmpty()) {
                remaining.put(entry.getKey(), result);
            }
        }
        return new SearchResult(name, description, remaining);
    }

    private static Map<Path, ArchiveMatch> copy(SearchResult source) {
        Map<Path, ArchiveMatch> copied = new LinkedHashMap<>();
        for (Map.Entry<Path, ArchiveMatch> entry : source.matchesByArchive().entrySet()) {
            copied.put(entry.getKey(), entry.getValue().copy());
        }
        return copied;
    }
}
