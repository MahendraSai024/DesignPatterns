package com.designpatterns.facade;

// Subsystem class 5 — controls screen (raise/lower)
public class Screen {
    private final String name;

    public Screen(String name) { this.name = name; }

    public void down() { System.out.println("[SCRN]  " + name + " down"); }
    public void up()   { System.out.println("[SCRN]  " + name + " up"); }
}
