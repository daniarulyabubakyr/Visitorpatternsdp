package kz.aitu.visitor;

import java.util.Objects;

/** Shared identity and ownership; names are immutable path segments. */
public abstract sealed class Node implements Element permits FileNode, DirectoryNode {
    private final String name;
    private DirectoryNode parent;

    protected Node(String name) {
        Objects.requireNonNull(name, "name");
        if (name.isBlank() || name.equals(".") || name.equals("..")
                || name.contains("/") || name.contains("\\") || name.indexOf('\0') >= 0) {
            throw new IllegalArgumentException("Name must be a nonblank, single path segment");
        }
        this.name = name;
    }

    @Override
    public final String name() { return name; }

    public final DirectoryNode parent() { return parent; }

    // Only the model package changes ownership after validating all invariants.
    final void attachTo(DirectoryNode parent) { this.parent = parent; }
}
