package kz.aitu.visitor;

/** Reusable sequentially through TreeWalker.walk; fails explicitly on overflow. */
public final class TotalSizeVisitor implements Visitor {
    private long totalBytes;

    @Override
    public void begin() { totalBytes = 0; }

    @Override
    public void visit(FileNode file) { totalBytes = Math.addExact(totalBytes, file.size()); }

    @Override
    public void visit(DirectoryNode directory) { }

    public long totalBytes() { return totalBytes; }
}
