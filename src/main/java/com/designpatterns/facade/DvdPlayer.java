package com.designpatterns.facade;

// Subsystem class 2 — handles DVD disc operations
public class DvdPlayer {
    private final String name;

    public DvdPlayer(String name) { this.name = name; }

    public void on()               { System.out.println("[DVD]   " + name + " on"); }
    public void off()              { System.out.println("[DVD]   " + name + " off"); }
    public void play(String movie) { System.out.println("[DVD]   Playing: " + movie); }
    public void stop()             { System.out.println("[DVD]   Stopped"); }
    public void eject()            { System.out.println("[DVD]   Disc ejected"); }
}
