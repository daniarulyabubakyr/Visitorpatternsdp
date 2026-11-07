package kz.aitu.visitor;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class VisitorTest {
    @Test
    void concreteElementsDispatchToMatchingOverloads() {
        int[] calls = {0, 0};
        FileNode file = new FileNode("a", 1);
        DirectoryNode directory = new DirectoryNode("b");
        Visitor spy = new Visitor() {
            public void visit(FileNode actual) { assertSame(file, actual); calls[0]++; }
            public void visit(DirectoryNode actual) { assertSame(directory, actual); calls[1]++; }
        };
        file.accept(spy);
        directory.accept(spy);
        assertArrayEquals(new int[]{1, 1}, calls);
    }

    @Test
    void sumsMultipleLevelsAndZeroByteFiles() {
        DirectoryNode root = fixture();
        root.add(new FileNode("zero", 0));
        TotalSizeVisitor size = new TotalSizeVisitor();
        TreeWalker.walk(root, size);
        assertEquals(126, size.totalBytes());
    }

    @Test
    void combinesFilenameAndInclusiveSizeConditions() {
        SearchVisitor search = new SearchVisitor(name -> name.endsWith(".txt"), 20);
        TreeWalker.walk(fixture(), search);
        assertEquals(List.of("alpha/b.txt", "z.txt"), search.paths());
    }

    @Test
    void arbitraryPredicateCanChangeFilenameCriterion() {
        SearchVisitor search = new SearchVisitor(name -> name.startsWith("c"), 0);
        TreeWalker.walk(fixture(), search);
        assertEquals(List.of("alpha/deep/c.bin"), search.paths());
    }

    @Test
    void pathsAreSortedAndRelativeToSelectedSubtree() {
        DirectoryNode subtree = new DirectoryNode("selected");
        subtree.add(new FileNode("z.txt", 10));
        DirectoryNode child = new DirectoryNode("a");
        child.add(new FileNode("x.txt", 10));
        subtree.add(child);
        DirectoryNode parent = new DirectoryNode("parent");
        parent.add(subtree);
        SearchVisitor search = new SearchVisitor(name -> true, 0);
        TreeWalker.walk(subtree, search);
        assertEquals(List.of("a/x.txt", "z.txt"), search.paths());
    }

    @Test
    void statisticsIncludeRootAndDeepestFile() {
        StatisticsVisitor statistics = new StatisticsVisitor();
        TreeWalker.walk(fixture(), statistics);
        assertEquals(4, statistics.fileCount());
        assertEquals(4, statistics.directoryCount());
        assertEquals(3, statistics.maximumDepth());
    }

    @Test
    void emptyDirectoryHasZeroBytesNoMatchesAndRootCountOne() {
        DirectoryNode empty = new DirectoryNode("empty");
        TotalSizeVisitor size = new TotalSizeVisitor();
        SearchVisitor search = new SearchVisitor(name -> true, 0);
        StatisticsVisitor statistics = new StatisticsVisitor();
        TreeWalker.walk(empty, size);
        TreeWalker.walk(empty, search);
        TreeWalker.walk(empty, statistics);
        assertEquals(0, size.totalBytes());
        assertTrue(search.paths().isEmpty());
        assertEquals(0, statistics.fileCount());
        assertEquals(1, statistics.directoryCount());
        assertEquals(0, statistics.maximumDepth());
    }

    @Test
    void nonemptyTreeCanHaveNoMatches() {
        SearchVisitor search = new SearchVisitor(name -> name.endsWith(".pdf"), 0);
        TreeWalker.walk(fixture(), search);
        assertTrue(search.paths().isEmpty());
    }

    @Test
    void visitorsResetAcrossRepeatedAndDifferentRoots() {
        TotalSizeVisitor size = new TotalSizeVisitor();
        SearchVisitor search = new SearchVisitor(name -> true, 0);
        StatisticsVisitor statistics = new StatisticsVisitor();
        for (int run = 0; run < 2; run++) {
            TreeWalker.walk(fixture(), statistics);
            TreeWalker.walk(fixture(), search);
            TreeWalker.walk(fixture(), size);
            assertEquals(126, size.totalBytes());
            assertEquals(4, search.paths().size());
            assertEquals(4, statistics.fileCount());
            assertEquals(4, statistics.directoryCount());
            assertEquals(3, statistics.maximumDepth());
        }
        DirectoryNode empty = new DirectoryNode("empty");
        TreeWalker.walk(empty, size);
        TreeWalker.walk(empty, search);
        TreeWalker.walk(empty, statistics);
        assertEquals(0, size.totalBytes());
        assertTrue(search.paths().isEmpty());
        assertEquals(0, statistics.fileCount());
        assertEquals(1, statistics.directoryCount());
        assertEquals(0, statistics.maximumDepth());
    }

    @Test
    void fileRootHasDepthZeroAndDotPath() {
        FileNode file = new FileNode("only.txt", 12);
        SearchVisitor search = new SearchVisitor(name -> true, 12);
        TotalSizeVisitor size = new TotalSizeVisitor();
        StatisticsVisitor statistics = new StatisticsVisitor();
        TreeWalker.walk(file, search);
        TreeWalker.walk(file, size);
        TreeWalker.walk(file, statistics);
        assertEquals(List.of("."), search.paths());
        assertEquals(12, size.totalBytes());
        assertEquals(1, statistics.fileCount());
        assertEquals(0, statistics.directoryCount());
        assertEquals(0, statistics.maximumDepth());
    }

    @Test
    void nestedEmptyDirectoriesContributeDepth() {
        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode inner = new DirectoryNode("inner");
        root.add(inner);
        inner.add(new DirectoryNode("empty"));
        StatisticsVisitor statistics = new StatisticsVisitor();
        TreeWalker.walk(root, statistics);
        assertEquals(0, statistics.fileCount());
        assertEquals(3, statistics.directoryCount());
        assertEquals(2, statistics.maximumDepth());
    }

    @Test
    void sizeOverflowIsExplicitAndNextRunResets() {
        DirectoryNode root = new DirectoryNode("large");
        root.add(new FileNode("a", Long.MAX_VALUE));
        root.add(new FileNode("b", 1));
        TotalSizeVisitor size = new TotalSizeVisitor();
        assertThrows(ArithmeticException.class, () -> TreeWalker.walk(root, size));
        TreeWalker.walk(new FileNode("small", 3), size);
        assertEquals(3, size.totalBytes());
    }

    @Test
    void searchResultsAreImmutableSnapshots() {
        SearchVisitor search = new SearchVisitor(name -> true, 0);
        TreeWalker.walk(fixture(), search);
        List<String> snapshot = search.paths();
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add("fake"));
        TreeWalker.walk(new DirectoryNode("empty"), search);
        assertEquals(4, snapshot.size());
    }

    @Test
    void rejectsInvalidSearchConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> new SearchVisitor(name -> true, -1));
        assertThrows(NullPointerException.class, () -> new SearchVisitor(null, 0));
    }

    private DirectoryNode fixture() {
        DirectoryNode root = new DirectoryNode("test-root");
        DirectoryNode alpha = new DirectoryNode("alpha");
        DirectoryNode deep = new DirectoryNode("deep");
        root.add(new FileNode("z.txt", 20));
        root.add(alpha);
        root.add(new DirectoryNode("empty"));
        alpha.add(new FileNode("b.txt", 25));
        alpha.add(new FileNode("small.txt", 1));
        alpha.add(deep);
        deep.add(new FileNode("c.bin", 80));
        return root;
    }
}
