package com.designpatterns.composite;

import java.util.ArrayList;
import java.util.List;

// Composite — extends the abstract component; delegates to children
public class Folder extends FileSystemItem {
    protected final List<FileSystemItem> children = new ArrayList<>();

    public Folder(String name) {
        super(name);
    }

    public void add(FileSystemItem item) {
        children.add(item);
    }

    public void remove(FileSystemItem item) {
        children.remove(item);
    }

    // Size is computed recursively — each child resolves its own size
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
