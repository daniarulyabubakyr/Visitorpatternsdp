package kz.aitu.visitor;

/** Added without changing elements, accept methods, contracts or the walker. */
public final class StatisticsVisitor implements Visitor {
    private long fileCount;
    private long directoryCount;
    private int maximumDepth;

    @Override
    public void begin() { fileCount = 0; directoryCount = 0; maximumDepth = 0; }

    @Override
    public void enter(String relativePath, int depth) { maximumDepth = Math.max(maximumDepth, depth); }

    @Override
    public void visit(FileNode file) { fileCount++; }

    @Override
    public void visit(DirectoryNode directory) { directoryCount++; }

    public long fileCount() { return fileCount; }
    public long directoryCount() { return directoryCount; }
    public int maximumDepth() { return maximumDepth; }
}
