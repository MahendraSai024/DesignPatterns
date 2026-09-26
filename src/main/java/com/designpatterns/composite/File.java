package com.designpatterns.composite;

// Leaf — extends the abstract component; has no children
public class File extends FileSystemItem {
    private final int size;

    public File(String name, int size) {
        super(name);
        this.size = size;
    }

    @Override
    public int getSize() { return size; }

    @Override
    public void print(String indent) {
        System.out.println(indent + "📄 " + name + " (" + size + " KB)");
    }
}
