package kz.aitu.visitor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class DirectoryNode extends Node {
    private final Map<String, Node> children = new LinkedHashMap<>();

    public DirectoryNode(String name) { super(name); }

    public void add(Node child) {
        Objects.requireNonNull(child, "child");
        if (child.parent() != null) throw new IllegalArgumentException("Child already has a parent");
        if (children.containsKey(child.name())) throw new IllegalArgumentException("Duplicate sibling name");
        for (Node ancestor = this; ancestor != null; ancestor = ancestor.parent()) {
            if (ancestor == child) throw new IllegalArgumentException("Adding this child would create a cycle");
        }
        children.put(child.name(), child);
        child.attachTo(this);
    }

    /** Immutable snapshot; callers cannot mutate the backing collection. */
    public List<Node> children() { return List.copyOf(children.values()); }

    @Override
    public void accept(Visitor visitor) {
        Objects.requireNonNull(visitor, "visitor").visit(this);
    }
}
