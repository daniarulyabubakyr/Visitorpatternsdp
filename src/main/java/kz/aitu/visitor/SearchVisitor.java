package kz.aitu.visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public final class SearchVisitor implements Visitor {
    private final Predicate<String> filenameCriterion;
    private final long minimumSize;
    private final List<String> matches = new ArrayList<>();
    private String currentPath = "";

    public SearchVisitor(Predicate<String> filenameCriterion, long minimumSize) {
        this.filenameCriterion = Objects.requireNonNull(filenameCriterion, "filenameCriterion");
        if (minimumSize < 0) throw new IllegalArgumentException("Minimum size must be non-negative");
        this.minimumSize = minimumSize;
    }

    @Override
    public void begin() { matches.clear(); currentPath = ""; }

    @Override
    public void enter(String relativePath, int depth) { currentPath = relativePath; }

    @Override
    public void visit(FileNode file) {
        if (filenameCriterion.test(file.name()) && file.size() >= minimumSize) {
            matches.add(currentPath.isEmpty() ? "." : currentPath);
        }
    }

    @Override
    public void visit(DirectoryNode directory) { }

    public List<String> paths() { return matches.stream().sorted().toList(); }
}
