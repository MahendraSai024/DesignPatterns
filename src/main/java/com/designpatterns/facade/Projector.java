package com.designpatterns.facade;

// Subsystem class 3 — controls the projector
public class Projector {
    private final String name;

    public Projector(String name) { this.name = name; }

    public void on()             { System.out.println("[PROJ]  " + name + " on"); }
    public void off()            { System.out.println("[PROJ]  " + name + " off"); }
    public void wideScreenMode() { System.out.println("[PROJ]  Wide-screen mode (16x9)"); }
}
