# Facade Design Pattern

## What Is It?

The Facade pattern provides a **single simplified interface over a complex subsystem** — hiding the subsystem's internal complexity from the client.

> "Provide a unified interface to a set of interfaces in a subsystem. Facade defines a higher-level interface that makes the subsystem easier to use."
> — Gang of Four, *Design Patterns*

The core insight: the client shouldn't need to know about, or coordinate between, all the components that make something work. The facade absorbs that coordination into one place and exposes only what the client actually needs.

---

## The Problem It Solves

Imagine setting up a home theater. Without a facade, the client has to orchestrate every component in the right order:

```java
// Without Facade — client must know every component and the correct order
lights.dim(10);
screen.down();
projector.on();
projector.wideScreenMode();
amp.on();
amp.setVolume(8);
dvd.on();
dvd.play(movie);
```

That's 8 calls, in the right sequence, knowing about 5 different objects. Every caller that wants to "watch a movie" duplicates this. If the startup sequence changes (e.g., the amplifier must come on before the projector), you have to find and update every caller.

**Facade collapses those 8 calls into one**: `theater.watchMovie("Interstellar")`.

---

## Roles

| Role            | Responsibility                                                     | In our code            |
|-----------------|--------------------------------------------------------------------|------------------------|
| **Facade**      | Single entry point; coordinates subsystem components              | `HomeTheaterFacade`    |
| **Subsystem**   | Individual classes with their own complex APIs                    | `Amplifier`, `DvdPlayer`, `Projector`, `TheaterLights`, `Screen` |
| **Client**      | Calls only the Facade — unaware of subsystem classes              | `Main`                 |

---

## Structure

```
Client
  └── HomeTheaterFacade.watchMovie()
           ├── TheaterLights.dim()
           ├── Screen.down()
           ├── Projector.on() + wideScreenMode()
           ├── Amplifier.on() + setVolume()
           └── DvdPlayer.on() + play()
```

Class diagram: [`docs/diagrams/facade.png`](diagrams/facade.png)

---

## Our Implementation

### Subsystem Classes

Each class owns its own domain — no class knows about any other:

```java
// Amplifier.java
public class Amplifier {
    public void on()               { System.out.println("[AMP]   on"); }
    public void off()              { System.out.println("[AMP]   off"); }
    public void setVolume(int vol) { System.out.println("[AMP]   Volume set to " + vol); }
}

// DvdPlayer.java
public class DvdPlayer {
    public void on()               { ... }
    public void play(String movie) { ... }
    public void stop()             { ... }
    public void eject()            { ... }
    public void off()              { ... }
}
// + Projector, TheaterLights, Screen — same pattern
```

Five independent subsystem classes. The facade is the only place that knows about all five.

### Facade — `HomeTheaterFacade.java`

```java
public class HomeTheaterFacade {
    private final Amplifier     amp;
    private final DvdPlayer     dvd;
    private final Projector     projector;
    private final TheaterLights lights;
    private final Screen        screen;

    public HomeTheaterFacade(Amplifier amp, DvdPlayer dvd,
                             Projector projector, TheaterLights lights, Screen screen) {
        this.amp = amp; this.dvd = dvd;
        this.projector = projector; this.lights = lights; this.screen = screen;
    }

    public void watchMovie(String movie) {
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
        dvd.stop(); dvd.eject(); dvd.off();
        amp.off(); projector.off();
        screen.up(); lights.on();
    }
}
```

- Holds references to all subsystem objects — injected at construction (not created inside)
- `watchMovie()` and `endMovie()` encapsulate the full setup/teardown sequences
- Adding a new step (e.g., `popcornMachine.start()`) means one line in this file, not changes to all callers

### Client — `Main.java`

```java
HomeTheaterFacade theater = new HomeTheaterFacade(amp, dvd, proj, lights, screen);

theater.watchMovie("Interstellar");
theater.endMovie();
```

Two calls. Zero knowledge of the startup sequence. Zero imports of subsystem classes.

---

## How It Works

Tracing `theater.watchMovie("Interstellar")`:

```
Main
  └─ theater.watchMovie("Interstellar")     [HomeTheaterFacade]
       ├─ lights.dim(10)                    [TheaterLights]
       ├─ screen.down()                     [Screen]
       ├─ projector.on()                    [Projector]
       ├─ projector.wideScreenMode()        [Projector]
       ├─ amp.on()                          [Amplifier]
       ├─ amp.setVolume(8)                  [Amplifier]
       ├─ dvd.on()                          [DvdPlayer]
       └─ dvd.play("Interstellar")          [DvdPlayer]
```

The client calls one method. The facade drives eight subsystem calls in the correct order.

---

## The Facade Does Not Lock You Out

A facade is not a wall — the subsystem classes remain public. Clients that need fine-grained control can still use them directly:

```java
amp.setVolume(11);    // still valid — bypass the facade when needed
```

The facade is a convenience layer, not a gatekeeper.

---

## When to Use Facade

Use Facade when:

- A subsystem has grown complex and clients only need a small slice of its functionality
- You want to **layer your system** — expose a high-level API for most callers, keep the detailed API accessible for power users
- You're wrapping a **legacy system** or third-party library behind a clean interface
- You want to **reduce coupling** between clients and subsystem internals (changing the internals doesn't ripple to callers)

Don't use it when:

- The subsystem is already simple — a facade over two methods adds noise, not clarity
- You need to expose the full subsystem API anyway — a facade that just delegates every call one-to-one adds no value
- You confuse it with Adapter — Facade **simplifies**; Adapter **translates incompatible interfaces**

---

## Real-World Examples

| Domain                  | Facade                        | Subsystem it hides                              |
|-------------------------|-------------------------------|-------------------------------------------------|
| Home theater            | `HomeTheaterFacade`           | Amp, DVD, Projector, Lights, Screen             |
| SLF4J logging           | `LoggerFactory.getLogger()`   | Log4j, Logback, JUL implementations            |
| Spring `JdbcTemplate`   | `JdbcTemplate`                | `Connection`, `PreparedStatement`, `ResultSet`  |
| Java `java.net.URL`     | `URL.openStream()`            | DNS, TCP sockets, HTTP protocol                 |
| AWS SDK high-level API  | `TransferManager`             | S3 multipart upload internals                   |
| Android `Camera2` API   | `CameraX`                     | `CameraDevice`, `CaptureSession`, `ImageReader` |

`JdbcTemplate` is the canonical Spring example — it hides 10+ lines of boilerplate (open connection, prepare statement, handle exceptions, close resources) behind one method call.

---

## Facade vs Adapter vs Decorator

| Pattern       | Intent                                              | Changes interface? | Wraps                  |
|---------------|-----------------------------------------------------|--------------------|------------------------|
| **Facade**    | Simplify a complex subsystem into one entry point   | Yes — simplifies   | Multiple classes       |
| **Adapter**   | Make an incompatible interface compatible           | Yes — translates   | One class              |
| **Decorator** | Add behaviour without changing the interface        | No                 | One object             |

- **Facade**: "One button that does everything I need."
- **Adapter**: "I need this plug to fit that socket."
- **Decorator**: "Same product, gift-wrapped."
