package com.designpatterns.facade;

// Subsystem class 4 — controls room lights
public class TheaterLights {
    private final String name;

    public TheaterLights(String name) { this.name = name; }

    public void on()              { System.out.println("[LITE]  " + name + " on"); }
    public void off()             { System.out.println("[LITE]  " + name + " off"); }
    public void dim(int percent)  { System.out.println("[LITE]  Dimmed to " + percent + "%"); }
}
