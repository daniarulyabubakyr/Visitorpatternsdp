package kz.aitu.visitor;

public final class Main {
    private Main() { }

    /** Optional arguments: case-sensitive filename suffix and inclusive minimum bytes. */
    public static void main(String[] args) {
        if (args.length > 2) throw new IllegalArgumentException("Usage: java -jar visitor-file-system-1.0.0.jar [suffix] [minimumBytes]");
        String suffix = args.length > 0 ? args[0] : ".java";
        long minimum = args.length > 1 ? Long.parseLong(args[1]) : 200;
        DirectoryNode root = sampleTree();
        TotalSizeVisitor size = new TotalSizeVisitor();
        SearchVisitor search = new SearchVisitor(name -> name.endsWith(suffix), minimum);
        StatisticsVisitor statistics = new StatisticsVisitor();
        TreeWalker.walk(root, size);
        TreeWalker.walk(root, search);
        TreeWalker.walk(root, statistics);
        System.out.println("Root: " + root.name());
        System.out.println("Total bytes: " + size.totalBytes());
        System.out.println("Search: suffix=" + suffix + ", minimumBytes=" + minimum);
        search.paths().forEach(path -> System.out.println("  " + path));
        System.out.println("Files: " + statistics.fileCount());
        System.out.println("Directories (including root): " + statistics.directoryCount());
        System.out.println("Maximum depth: " + statistics.maximumDepth());
    }

    static DirectoryNode sampleTree() {
        DirectoryNode root = new DirectoryNode("coursework");
        DirectoryNode src = new DirectoryNode("src");
        DirectoryNode model = new DirectoryNode("model");
        DirectoryNode docs = new DirectoryNode("docs");
        root.add(new FileNode("README.md", 120));
        root.add(src);
        root.add(docs);
        root.add(new DirectoryNode("empty"));
        src.add(new FileNode("Main.java", 400));
        src.add(model);
        model.add(new FileNode("Node.java", 200));
        model.add(new FileNode("notes.txt", 50));
        docs.add(new FileNode("guide.md", 300));
        docs.add(new FileNode("draft.java", 80));
        return root;
    }
}
