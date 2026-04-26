package chongwm.utility.java.jargoggle.service;

import javax.swing.SwingWorker;

import chongwm.utility.java.jargoggle.model.ArchiveMatch;
import chongwm.utility.java.jargoggle.model.CombineMode;
import chongwm.utility.java.jargoggle.model.SearchMode;
import chongwm.utility.java.jargoggle.model.SearchOptions;
import chongwm.utility.java.jargoggle.model.SearchProgress;
import chongwm.utility.java.jargoggle.model.SearchResult;
import chongwm.utility.java.jargoggle.model.SearchTargetScope;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class SearchService {

    public SearchWorker createWorker(
            SearchOptions options,
            SearchResult baseResult,
            Consumer<SearchProgress> progressConsumer
    ) {
        return new SearchWorker(options, baseResult, progressConsumer);
    }

    public static final class SearchWorker extends SwingWorker<SearchResult, SearchProgress> {
        private final SearchOptions options;
        private final SearchResult baseResult;
        private final Consumer<SearchProgress> progressConsumer;

        private SearchWorker(SearchOptions options, SearchResult baseResult, Consumer<SearchProgress> progressConsumer) {
            this.options = Objects.requireNonNull(options, "options");
            this.baseResult = baseResult;
            this.progressConsumer = Objects.requireNonNull(progressConsumer, "progressConsumer");
        }

        @Override
        protected SearchResult doInBackground() throws Exception {
            List<Path> targets = collectTargets(options);
            Map<Path, ArchiveMatch> resultMap = new LinkedHashMap<>();
            int total = targets.size();
            int matchedArchives = 0;

            for (int i = 0; i < targets.size(); i++) {
                if (isCancelled()) {
                    throw new CancellationException("Search cancelled");
                }
                Path archive = targets.get(i);
                publish(new SearchProgress(i, total, matchedArchives, archive.toString()));
                ArchiveMatch match = scanArchive(archive, options.query(), options.mode());
                if (match != null) {
                    resultMap.put(archive, match);
                    matchedArchives++;
                }
                int progress = total == 0 ? 100 : (int) Math.round(((i + 1) * 100.0) / total);
                setProgress(progress);
                publish(new SearchProgress(i + 1, total, matchedArchives, archive.toString()));
            }

            String resultName = buildResultName();
            String description = buildResultDescription(targets.size());
            SearchResult freshResult = new SearchResult(resultName, description, resultMap);
            return combine(baseResult, freshResult, options.combineMode());
        }

        @Override
        protected void process(List<SearchProgress> chunks) {
            if (!chunks.isEmpty()) {
                progressConsumer.accept(chunks.get(chunks.size() - 1));
            }
        }

        private SearchResult combine(SearchResult current, SearchResult fresh, CombineMode mode) {
            if (current == null || mode == CombineMode.REPLACE) {
                return fresh;
            }
            String name = fresh.name();
            String description = fresh.description() + " · combined with " + current.name() + " via " + mode.displayName();
            return switch (mode) {
                case REPLACE -> fresh;
                case UNION -> ResultSetOps.union(current, fresh, name, description);
                case INTERSECT -> ResultSetOps.intersect(current, fresh, name, description);
                case SUBTRACT -> ResultSetOps.subtract(current, fresh, name, description);
            };
        }

        private List<Path> collectTargets(SearchOptions options) throws IOException {
            Set<Path> targets = new LinkedHashSet<>();
            if (options.targetScope() == SearchTargetScope.ALL_ROOTS || options.targetScope() == SearchTargetScope.CURRENT_RESULT_AND_ROOTS) {
                for (Path root : options.roots()) {
                    if (Files.isRegularFile(root) && isArchive(root)) {
                        targets.add(root.toAbsolutePath().normalize());
                    } else if (Files.isDirectory(root)) {
                        targets.addAll(scanRoots(root, options.recursive()));
                    }
                }
            }
            if (options.targetScope() == SearchTargetScope.CURRENT_RESULT_ONLY || options.targetScope() == SearchTargetScope.CURRENT_RESULT_AND_ROOTS) {
                for (Path path : options.seedArchives()) {
                    if (Files.isRegularFile(path) && isArchive(path)) {
                        targets.add(path.toAbsolutePath().normalize());
                    }
                }
            }
            return targets.stream().sorted(Comparator.comparing(Path::toString, String.CASE_INSENSITIVE_ORDER)).toList();
        }

        private Collection<Path> scanRoots(Path root, boolean recursive) throws IOException {
            if (recursive) {
                try (var stream = Files.walk(root)) {
                    return stream.filter(Files::isRegularFile)
                            .filter(SearchWorker::isArchive)
                            .map(path -> path.toAbsolutePath().normalize())
                            .toList();
                }
            }
            try (var stream = Files.list(root)) {
                return stream.filter(Files::isRegularFile)
                        .filter(SearchWorker::isArchive)
                        .map(path -> path.toAbsolutePath().normalize())
                        .toList();
            }
        }

        private static boolean isArchive(Path path) {
            String fileName = path.getFileName().toString().toLowerCase();
            return fileName.endsWith(".jar") || fileName.endsWith(".zip");
        }

        private ArchiveMatch scanArchive(Path archive, String rawQuery, SearchMode mode) {
            String query = normalizeQuery(rawQuery);
            PathMatcher matcher = mode == SearchMode.GLOB ? FileSystems.getDefault().getPathMatcher("glob:" + query) : null;
            ArchiveMatch match = new ArchiveMatch(archive);
            try (ZipFile zipFile = new ZipFile(archive.toFile())) {
                var entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    if (isCancelled()) {
                        throw new CancellationException("Search cancelled");
                    }
                    ZipEntry entry = entries.nextElement();
                    String entryName = entry.getName();
                    if (matches(entryName, query, mode, matcher)) {
                        match.addEntry(entryName);
                    }
                }
            } catch (IOException ignored) {
                return null;
            }
            return match.entries().isEmpty() ? null : match;
        }

        private boolean matches(String entryName, String query, SearchMode mode, PathMatcher matcher) {
            return switch (mode) {
                case SUBSTRING -> entryName.contains(query);
                case EXACT -> entryName.equals(query) || entryName.equals(query + ".class");
                case GLOB -> matcher.matches(Path.of(entryName));
            };
        }

        private String normalizeQuery(String query) {
            return query.trim().replace('.', '/');
        }

        private String buildResultName() {
            return options.combineMode().displayName() + " · " + options.query().trim();
        }

        private String buildResultDescription(int archiveTargets) {
            List<String> parts = new ArrayList<>();
            parts.add("Query: " + options.query().trim());
            parts.add("Mode: " + options.mode().displayName());
            parts.add("Scope: " + options.targetScope().displayName());
            parts.add("Targets: " + archiveTargets + " archive(s)");
            return String.join(" | ", parts);
        }
    }
}
