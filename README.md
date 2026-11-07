# Visitor Pattern: in-memory file system

Java 17 / Maven / JUnit 5. Repository: https://github.com/daniarulyabubakyr/Visitorpatternsdp

## Run

Prerequisites: JDK 17 and Maven 3.9+ on `PATH` (`JAVA_HOME` must point to the JDK).
Run these commands from the project root:

```sh
mvn test
mvn package
java -jar target/visitor-file-system-1.0.0.jar
```

Change the case-sensitive filename suffix and inclusive size threshold without editing code:

```sh
java -jar target/visitor-file-system-1.0.0.jar .md 100
```

This workspace also contains an ignored local Maven installation. On this Windows machine,
`run.cmd test` and `run.cmd package` find Maven and an installed JDK automatically.
The repository itself has no dependency on those local tools, IntelliJ, or interactive input.

## Demonstration data

```text
coursework/
  README.md             120 bytes
  src/
    Main.java           400 bytes
    model/
      Node.java         200 bytes
      notes.txt          50 bytes
  docs/
    guide.md            300 bytes
    draft.java           80 bytes
  empty/
```

Default output:

```text
Root: coursework
Total bytes: 1150
Search: suffix=.java, minimumBytes=200
  src/Main.java
  src/model/Node.java
Files: 6
Directories (including root): 5
Maximum depth: 3
```

## API and traversal contract

`DirectoryNode.add(Node)` validates ownership, sibling names and ancestor cycles before
changing either the collection or the child's parent. Names and file sizes are immutable;
child collections and search results are immutable snapshots. This model deliberately
offers insertion only; removal, rename, concurrent mutation and symbolic links are outside its scope.

`TreeWalker.walk(root, visitor)` is the **only traversal owner**. It uses an explicit stack
for preorder depth-first traversal, so nesting does not consume the Java call stack.
Each element is accepted once and receives exactly one operation visit. Visitors never
recurse. The walker adapter dispatches file/directory behavior through overloaded `visit`
methods, without runtime type checks or casts.

Double dispatch: the runtime element selects `FileNode.accept` or `DirectoryNode.accept`;
`visitor.visit(this)` then selects the matching overload, with the visitor's runtime
implementation executing that operation. The walker's adapter forwards the same typed
argument to the operation visitor.

Each walk calls `begin()` first to clear accumulated results, then `enter(path, depth)`
before each overloaded visit. Reuse visitors sequentially via `TreeWalker.walk`;
calling `accept` directly performs only one dispatch, without resetting or traversing.
Visitors are not thread-safe. If an operation throws, discard its partial result; a new
walk resets it. Byte totals use checked `long` addition and throw on overflow.

Root depth is zero; each child increases depth by one. Both files and empty directories
contribute to maximum depth. Directory counts include the selected root directory.
Paths start at the selected root, omit its own name, and use `/`. A file selected as the
root is represented by `.`. Search combines `Predicate<String>` on the filename with
`size >= minimumSize`; returned paths use Java's natural lexicographic ordering,
independent of insertion order. Names are case-sensitive single path segments.

## Extension boundary

The initial `Visitor` contract already covers both element types. `StatisticsVisitor`
was added in a separate commit without modifying `FileNode`, `DirectoryNode`, either
`accept` method, `Visitor`, or `TreeWalker`. New operations implement the existing
contract. A new element type is more expensive: update the sealed model hierarchy,
add its `accept` and visitor overload, and update visitors and traversal accordingly.

See [UML](docs/uml.md) for the class relationships.

## Automated verification

- `VisitorTest`: overload dispatch, nested totals, combined filters, sorted subtree paths,
  statistics, empty/no-match cases, repeated runs, file roots, overflow and result snapshots.
- `TreeIntegrityTest`: invalid sizes/names, duplicate siblings, shared ownership, cycles,
  nulls, safe snapshots and valid nesting.
- `TreeWalkerTest`: exact-once dispatch with context and a tree with 4,000 directory levels.

JUnit XML and text results are generated in `target/surefire-reports/` by `mvn test`.
Verified with JDK 17 and Maven 3.9.9: **30 tests, 0 failures, 0 errors, 0 skipped**;
`mvn package` produced the executable JAR successfully. Both demonstration commands above
were executed and their results checked.

## Development provenance

Implementation and tests were prepared with OpenAI Codex assistance. Review and understand
the code, and follow the course's rules on AI assistance before submitting it as individual work.
The assignment PDF was used as the specification; no reference implementation was copied.
The PDF report has intentionally not been prepared.
