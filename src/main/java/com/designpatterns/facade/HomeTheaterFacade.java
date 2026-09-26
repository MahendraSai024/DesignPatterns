package com.designpatterns.facade;

// Facade — single simplified interface over the entire home-theater subsystem
public class HomeTheaterFacade {
    private final Amplifier    amp;
    private final DvdPlayer    dvd;
    private final Projector    projector;
    private final TheaterLights lights;
    private final Screen        screen;

    public HomeTheaterFacade(Amplifier amp, DvdPlayer dvd,
                             Projector projector, TheaterLights lights, Screen screen) {
        this.amp       = amp;
        this.dvd       = dvd;
        this.projector = projector;
        this.lights    = lights;
        this.screen    = screen;
    }

    // One call replaces ~10 subsystem calls the client would otherwise make
    public void watchMovie(String movie) {
        System.out.println("\n>> Get ready to watch a movie...");
        lights.dim(10);
        screen.down();
        projector.on();
        projector.wideScreenMode();
        amp.on();
        amp.setVolume(8);
        dvd.on();
        dvd.play(movie);
    }

    public void endMovie() {
        System.out.println("\n>> Shutting movie theater down...");
        dvd.stop();
        dvd.eject();
        dvd.off();
        amp.off();
        projector.off();
        screen.up();
        lights.on();
    }
}
