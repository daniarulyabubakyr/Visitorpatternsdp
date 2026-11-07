package kz.aitu.visitor;

/** Walker lifecycle: begin, then enter + visit for each reachable element. */
public interface Visitor {
    default void begin() { }
    default void enter(String relativePath, int depth) { }
    void visit(FileNode file);
    void visit(DirectoryNode directory);
}
