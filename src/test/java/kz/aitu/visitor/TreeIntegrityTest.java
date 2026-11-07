package kz.aitu.visitor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class TreeIntegrityTest {
    @Test
    void rejectsNegativeSizeButAcceptsZero() {
        assertThrows(IllegalArgumentException.class, () -> new FileNode("bad", -1));
        assertEquals(0, new FileNode("empty", 0).size());
    }

    @Test
    void rejectsDuplicateSiblingNamesAcrossTypesAtomically() {
        DirectoryNode root = new DirectoryNode("root");
        FileNode first = new FileNode("same", 1);
        DirectoryNode rejected = new DirectoryNode("same");
        root.add(first);
        assertThrows(IllegalArgumentException.class, () -> root.add(rejected));
        assertEquals(List.of(first), root.children());
        assertNull(rejected.parent());
        new DirectoryNode("other").add(rejected);
    }

    @Test
    void rejectsMultipleParentsAndRepeatedInsertion() {
        DirectoryNode first = new DirectoryNode("first");
        DirectoryNode second = new DirectoryNode("second");
        FileNode file = new FileNode("file", 10);
        first.add(file);
        assertThrows(IllegalArgumentException.class, () -> second.add(file));
        assertThrows(IllegalArgumentException.class, () -> first.add(file));
        assertSame(first, file.parent());
        assertTrue(second.children().isEmpty());
        assertEquals(List.of(file), first.children());
    }

    @Test
    void rejectsSelfCycleAndAncestorCycleWithoutCorruptingTree() {
        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode middle = new DirectoryNode("middle");
        DirectoryNode leaf = new DirectoryNode("leaf");
        root.add(middle);
        middle.add(leaf);
        assertThrows(IllegalArgumentException.class, () -> root.add(root));
        assertThrows(IllegalArgumentException.class, () -> leaf.add(root));
        assertNull(root.parent());
        assertTrue(leaf.children().isEmpty());
        StatisticsVisitor stats = new StatisticsVisitor();
        TreeWalker.walk(root, stats);
        assertEquals(3, stats.directoryCount());
        assertEquals(2, stats.maximumDepth());
    }

    @Test
    void allowsSameNameInDifferentDirectories() {
        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode child = new DirectoryNode("child");
        root.add(child);
        root.add(new FileNode("same", 3));
        child.add(new FileNode("same", 4));
        SearchVisitor search = new SearchVisitor(name -> true, 0);
        TreeWalker.walk(root, search);
        assertEquals(List.of("child/same", "same"), search.paths());
    }

    @Test
    void childCollectionIsAnImmutableSnapshot() {
        DirectoryNode root = new DirectoryNode("root");
        List<Node> snapshot = root.children();
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(new FileNode("x", 1)));
        root.add(new FileNode("real", 2));
        assertTrue(snapshot.isEmpty());
        assertEquals(1, root.children().size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", ".", "..", "a/b", "a\\b", "\u0000"})
    void rejectsAmbiguousPathSegments(String name) {
        assertThrows(IllegalArgumentException.class, () -> new DirectoryNode(name));
        assertThrows(IllegalArgumentException.class, () -> new FileNode(name, 0));
    }

    @Test
    void rejectsNullInputs() {
        DirectoryNode root = new DirectoryNode("root");
        assertThrows(NullPointerException.class, () -> root.add(null));
        assertThrows(NullPointerException.class, () -> new DirectoryNode(null));
        assertThrows(NullPointerException.class, () -> root.accept(null));
        assertThrows(NullPointerException.class, () -> TreeWalker.walk(null, new TotalSizeVisitor()));
        assertThrows(NullPointerException.class, () -> TreeWalker.walk(root, null));
    }
}
