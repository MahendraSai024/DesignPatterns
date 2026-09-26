package com.designpatterns.facade;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Facade Pattern — Home Theater ===");

        // Wire up the subsystem components
        Amplifier    amp      = new Amplifier("Sony Surround Amp");
        DvdPlayer    dvd      = new DvdPlayer("LG Blu-ray");
        Projector    proj     = new Projector("Epson 4K");
        TheaterLights lights  = new TheaterLights("Ceiling Lights");
        Screen       screen   = new Screen("120\" Screen");

        // Client only talks to the Facade — all subsystem complexity is hidden
        HomeTheaterFacade theater = new HomeTheaterFacade(amp, dvd, proj, lights, screen);

        theater.watchMovie("Interstellar");
        theater.endMovie();
    }
}
