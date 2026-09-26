package com.designpatterns.composite;

// Component — abstract base enforcing the contract for both Leaf and Composite
public abstract class FileSystemItem {
    protected final String name;

    protected FileSystemItem(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public abstract int getSize();

    public abstract void print(String indent);
}
