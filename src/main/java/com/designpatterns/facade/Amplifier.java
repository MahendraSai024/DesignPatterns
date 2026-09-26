package com.designpatterns.facade;

// Subsystem class 1 — handles amplifier hardware
public class Amplifier {
    private final String name;

    public Amplifier(String name) { this.name = name; }

    public void on()               { System.out.println("[AMP]   " + name + " on"); }
    public void off()              { System.out.println("[AMP]   " + name + " off"); }
    public void setVolume(int vol) { System.out.println("[AMP]   Volume set to " + vol); }
}
