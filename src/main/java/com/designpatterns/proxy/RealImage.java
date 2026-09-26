package com.designpatterns.proxy;

// Real Subject — expensive to create; loading simulates a disk/network read
public class RealImage implements Image {
    private final String fileName;

    public RealImage(String fileName) {
        this.fileName = fileName;
        loadFromDisk();
    }

    private void loadFromDisk() {
        System.out.println("[REAL]  Loading:  " + fileName);
    }

    @Override
    public void display() {
        System.out.println("[REAL]  Displaying: " + fileName);
    }
}
