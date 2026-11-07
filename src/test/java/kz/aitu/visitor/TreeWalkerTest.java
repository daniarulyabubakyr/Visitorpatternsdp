package kz.aitu.visitor;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class TreeWalkerTest {
    @Test
    void visitsEveryReachableNodeExactlyOnceWithCorrectContext() {
        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode child = new DirectoryNode("child");
        FileNode file = new FileNode("file", 7);
        root.add(child);
        child.add(file);
        Set<Element> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        List<String> contexts = new ArrayList<>();
        TreeWalker.walk(root, new Visitor() {
            public void enter(String path, int depth) { contexts.add(path + ":" + depth); }
            public void visit(FileNode node) { assertTrue(seen.add(node)); }
            public void visit(DirectoryNode node) { assertTrue(seen.add(node)); }
        });
        assertEquals(Set.of(root, child, file), seen);
        assertEquals(List.of(":0", "child:1", "child/file:2"), contexts);
    }

    @Test
    void deepTreeDoesNotDependOnJavaCallStack() {
        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode current = root;
        for (int i = 0; i < 4000; i++) {
            DirectoryNode child = new DirectoryNode("d");
            current.add(child);
            current = child;
        }
        current.add(new FileNode("last", 9));
        StatisticsVisitor stats = new StatisticsVisitor();
        TreeWalker.walk(root, stats);
        assertEquals(4001, stats.directoryCount());
        assertEquals(1, stats.fileCount());
        assertEquals(4001, stats.maximumDepth());
    }
}
