package com.designpatterns.composite;

public class Main {
    public static void main(String[] args) {

        // Leaf files
        File resume    = new File("resume.pdf", 120);
        File photo     = new File("photo.jpg",  340);
        File notes     = new File("notes.txt",   15);
        File project   = new File("project.zip", 800);
        File readme    = new File("README.md",    10);

        // Nested folder structure
        Folder documents = new Folder("Documents");
        documents.add(resume);
        documents.add(notes);

        Folder pictures = new Folder("Pictures");
        pictures.add(photo);

        Folder work = new Folder("Work");
        work.add(project);
        work.add(readme);

        // Root folder contains sub-folders and a direct file
        Folder root = new Folder("Home");
        root.add(documents);
        root.add(pictures);
        root.add(work);

        // print() and getSize() work identically on File and Folder
        System.out.println("=== File System ===");
        root.print("");

        System.out.println();
        System.out.println("Total size of Documents: " + documents.getSize() + " KB");
        System.out.println("Total size of Home:      " + root.getSize() + " KB");
    }
}
