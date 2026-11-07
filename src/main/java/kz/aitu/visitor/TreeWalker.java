package kz.aitu.visitor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

/** Sole traversal owner. An explicit stack also supports very deep trees. */
public final class TreeWalker {
    private TreeWalker() { }

    public static void walk(Element root, Visitor visitor) {
        Objects.requireNonNull(root, "root");
        Objects.requireNonNull(visitor, "visitor");
        visitor.begin();
        Deque<Frame> pending = new ArrayDeque<>();
        pending.push(new Frame(root, "", 0));
        while (!pending.isEmpty()) {
            Frame frame = pending.pop();
            visitor.enter(frame.path(), frame.depth());
            frame.element().accept(new Visitor() {
                @Override
                public void visit(FileNode file) { visitor.visit(file); }

                @Override
                public void visit(DirectoryNode directory) {
                    visitor.visit(directory);
                    List<Node> children = directory.children();
                    for (int i = children.size() - 1; i >= 0; i--) {
                        Node child = children.get(i);
                        String path = frame.path().isEmpty() ? child.name() : frame.path() + "/" + child.name();
                        pending.push(new Frame(child, path, frame.depth() + 1));
                    }
                }
            });
        }
    }

    private record Frame(Element element, String path, int depth) { }
}
