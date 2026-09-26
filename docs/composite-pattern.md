# Composite Design Pattern

## What Is It?

The Composite pattern lets you build **tree structures** where **individual objects (leaves) and groups of objects (composites) are treated identically** through a shared interface.

> "Compose objects into tree structures to represent part-whole hierarchies. Composite lets clients treat individual objects and compositions of objects uniformly."
> — Gang of Four, *Design Patterns*

The key insight: the caller never needs to know whether it's talking to a single node or an entire subtree. Both answer the same messages.

---

## The Problem It Solves

Imagine you're building a file system. Without Composite, every caller that traverses the tree has to distinguish between files and folders:

```java
// Without Composite — messy, scattered instanceof checks
for (Object item : folder.getChildren()) {
    if (item instanceof File f) {
        System.out.println(f.getName() + " " + f.getSize());
    } else if (item instanceof Folder d) {
        printRecursively(d);   // you have to manage recursion yourself
    }
}
```

This logic gets duplicated everywhere: in print, in size calculation, in search, in deletion. Adding a new node type (e.g., `Symlink`) breaks every caller.

**Composite eliminates the `instanceof` checks entirely.** The tree manages its own traversal.

---

## Roles

| Role          | Responsibility                                                              | In our code        |
|---------------|-----------------------------------------------------------------------------|--------------------|
| **Component** | Abstract base — defines the interface both Leaf and Composite must honour  | `FileSystemItem`   |
| **Leaf**      | End node — has no children, implements behaviour directly                   | `File`             |
| **Composite** | Branch node — holds children, delegates operations down the tree            | `Folder`           |
| **Client**    | Works only with the Component interface — never casts                       | `Main`             |

---

## Structure

```
FileSystemItem  (abstract — Component)
├── File        (Leaf)
└── Folder      (Composite)
       ├── File
       ├── File
       └── Folder
              └── File
```

Class diagram: [`docs/diagrams/composite.png`](diagrams/composite.png)

---

## Our Implementation

### Component — `FileSystemItem.java`

```java
public abstract class FileSystemItem {
    protected final String name;

    protected FileSystemItem(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public abstract int getSize();
    public abstract void print(String indent);
}
```

- `name` is `protected` so subclasses inherit it directly without a getter call
- `getName()` is **concrete** here — both `File` and `Folder` share the same implementation
- `getSize()` and `print()` are **abstract** — each subclass resolves them differently

### Leaf — `File.java`

```java
public class File extends FileSystemItem {
    private final int size;

    public File(String name, int size) {
        super(name);          // name stored once, in the abstract base
        this.size = size;
    }

    @Override
    public int getSize() { return size; }   // terminal — no recursion

    @Override
    public void print(String indent) {
        System.out.println(indent + "📄 " + name + " (" + size + " KB)");
    }
}
```

- No children, no delegation — just returns its own data
- `super(name)` hands `name` to the abstract class; `File` doesn't duplicate it

### Composite — `Folder.java`

```java
public class Folder extends FileSystemItem {
    protected final List<FileSystemItem> children = new ArrayList<>();

    public Folder(String name) { super(name); }

    public void add(FileSystemItem item)    { children.add(item); }
    public void remove(FileSystemItem item) { children.remove(item); }

    @Override
    public int getSize() {
        return children.stream().mapToInt(FileSystemItem::getSize).sum();
    }

    @Override
    public void print(String indent) {
        System.out.println(indent + "📁 " + name + " (" + getSize() + " KB)");
        for (FileSystemItem item : children) {
            item.print(indent + "   ");
        }
    }
}
```

- `children` is `List<FileSystemItem>` — holds both `File` and `Folder` nodes
- `getSize()` doesn't know the depth; it just asks each child, and each child resolves it recursively
- `print()` passes a deepened indent string down — the tree draws itself

### Client — `Main.java`

```java
Folder root = new Folder("Home");
root.add(new Folder("Documents"));
root.add(new File("photo.jpg", 340));

root.print("");       // works — doesn't care what's inside
root.getSize();       // works — recursion is hidden in the nodes
```

`Main` builds the tree using concrete types but immediately forgets them — every subsequent call goes through `FileSystemItem`.

---

## How the Recursion Works

When you call `root.getSize()`:

```
root (Folder)
  └─ getSize() → sum of children
       ├─ documents (Folder)
       │    └─ getSize() → sum of children
       │         ├─ resume.pdf (File) → 120
       │         └─ notes.txt  (File) → 15
       │         = 135
       ├─ pictures (Folder)
       │    └─ photo.jpg (File) → 340
       │    = 340
       └─ work (Folder)
            ├─ project.zip (File) → 800
            └─ README.md   (File) → 10
            = 810
  = 1285
```

No caller orchestrates this. Each node is responsible for its own piece of the answer.

---

## Why `abstract class` Instead of `interface`

An `interface` would force every subclass to re-implement `getName()` identically. Using an `abstract class` lets us:

- Store `name` **once**, in the base — no duplication
- Make `getName()` **concrete** — inherited for free
- Use `protected` so subclasses access `name` directly (`name` vs `getName()`)
- Enforce `getSize()` and `print()` as contracts that must be overridden

| Concern            | `interface`          | `abstract class`              |
|--------------------|----------------------|-------------------------------|
| Shared state       | Not possible         | `protected final String name` |
| Shared behaviour   | `default` (awkward)  | Concrete methods              |
| Enforce override   | All methods          | Only `abstract` ones          |
| `protected` access | Not applicable       | Yes                           |

---

## When to Use Composite

Use Composite when:

- Your data is naturally **recursive** (trees, hierarchies, graphs)
- Client code should work the same on a **single item** and a **collection**
- You want to **add new node types** without changing caller code
- You need operations (size, render, validate) to **propagate automatically** down the tree

Don't use it when:

- Your structure is flat (a list, not a tree)
- Leaves and composites have **fundamentally different interfaces** — the uniform interface assumption breaks down
- You need to restrict what types of children a node can hold (Composite allows any `FileSystemItem` child)

---

## Real-World Examples

| Domain            | Component         | Leaf             | Composite               |
|-------------------|-------------------|------------------|-------------------------|
| File system       | `FileSystemItem`  | `File`           | `Folder`                |
| GUI widgets       | `Widget`          | `Button`, `Text` | `Panel`, `Window`       |
| Org chart         | `Employee`        | Individual staff | Manager with reports    |
| HTML/XML DOM      | `Node`            | `TextNode`       | `Element`               |
| Arithmetic expr   | `Expression`      | `Number`         | `BinaryOp` (`+`, `*`)  |
| Menu system       | `MenuItem`        | `ActionItem`     | `SubMenu`               |

### Arithmetic Expression Example

```
(1 + 2) * (3 + 4)

       *
      / \
     +   +
    / \ / \
   1  2 3  4
```

Every node is an `Expression` with `evaluate()`. Calling `root.evaluate()` returns 21 — no caller manages the traversal.

---

## Comparison: Composite vs Iterator vs Visitor

| Pattern       | Purpose                                          |
|---------------|--------------------------------------------------|
| **Composite** | Represents the tree structure itself             |
| **Iterator**  | Traverses a Composite without exposing internals |
| **Visitor**   | Adds new operations to a Composite without modifying nodes |

These three are often used together: Composite holds the structure, Iterator walks it, Visitor performs operations on each node.
