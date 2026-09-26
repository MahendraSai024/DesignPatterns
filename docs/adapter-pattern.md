# Adapter Design Pattern

## What Is It?

The Adapter pattern lets two **incompatible interfaces work together** by wrapping one inside a class that speaks the other's language — without modifying either side.

> "Convert the interface of a class into another interface clients expect. Adapter lets classes work together that couldn't otherwise because of incompatible interfaces."
> — Gang of Four, *Design Patterns*

Think of it exactly like a physical power adapter. A US laptop plug and a European wall socket have incompatible shapes. You don't rebuild the wall or redesign the laptop — you put an adapter between them. The adapter accepts one shape on one side and presents the other shape on the other side. Neither device knows the adapter is there.

The same idea in code: you have a **client** that calls a specific interface, and a **useful class** (the adaptee) that does what you need — but with a completely different method signature. Instead of rewriting either, you write a thin wrapper (the adapter) that translates one into the other.

```
Client  ──calls──▶  Target interface  ◀──implements──  Adapter  ──wraps──▶  Adaptee
```

The client only ever sees the Target interface. The Adapter is invisible to it.

---

## Why This Problem Arises

Interface mismatches are one of the most common integration problems in software:

- You're using a **third-party library** whose API you cannot change
- You're working with **legacy code** written before your current interface existed
- Two teams built two systems independently; now they need to talk
- You're upgrading a dependency and the new version renamed its methods
- You're writing a reusable component that must work with classes that don't share a common interface

In all of these cases, the Adapter is the right tool — it absorbs the translation in one place so neither side has to change.

---

## The Problem Without Adapter

### Example 1 — Media Player

`AudioPlayer` handles MP3 natively. `Mp4Player` and `VlcPlayer` exist and work, but they expose `playMp4()` and `playVlc()` — not `play()`.

Without Adapter, every caller that needs multiple formats has to know about every concrete class and every method name:

```java
// Caller is tightly coupled to every format's class AND method name
public void play(String fileName) {
    if (fileName.endsWith(".mp3")) {
        // native
    } else if (fileName.endsWith(".mp4")) {
        Mp4Player p = new Mp4Player();   // must import Mp4Player
        p.playMp4(fileName);             // must know the method is playMp4, not play
    } else if (fileName.endsWith(".vlc")) {
        VlcPlayer p = new VlcPlayer();   // must import VlcPlayer
        p.playVlc(fileName);             // must know the method is playVlc
    }
}
```

**Problems:**
- Every new format means editing this method
- The caller has knowledge it shouldn't (concrete classes, method names)
- You can't test different formats in isolation
- If `Mp4Player` is renamed or replaced, you find all the call sites manually

**With Adapter**, the caller becomes:

```java
public void play(String fileName) {
    if (fileName.endsWith(".mp3")) { /* native */ }
    else new MediaAdapter(format).play(fileName);   // same interface, every time
}
```

---

## Roles

| Role | Responsibility | In our code |
|---|---|---|
| **Target** | The interface the client already uses and depends on | `MediaPlayer` |
| **Adaptee** | The existing class with the incompatible interface — the "legacy" side | `Mp4Player`, `VlcPlayer` |
| **Adaptee Interface** | Optional shared type for all Adaptees | `AdvancedMediaPlayer` |
| **Adapter** | Implements Target, wraps Adaptee, translates calls | `MediaAdapter` |
| **Client** | Uses only the Target interface — never sees the Adaptee | `AudioPlayer`, `Main` |

---

## Structure

```
                        ┌──────────────────┐
                        │   «interface»    │
                        │   MediaPlayer    │
                        │  + play(String)  │
                        └────────┬─────────┘
                                 │ implements
               ┌─────────────────┴──────────────────┐
               │                                     │
    ┌──────────┴──────────┐              ┌───────────┴──────────┐
    │    AudioPlayer      │              │    MediaAdapter       │
    │  (Client)           │              │  (Adapter)            │
    │  + play(String)     │              │  - advancedPlayer     │
    │    handles .mp3     │              │  + play(String)  ─────┼──▶ AdvancedMediaPlayer
    │    creates Adapter  │              │    translates call     │       ↑ implements
    └─────────────────────┘              └──────────────────────┘    Mp4Player / VlcPlayer
```

Class diagram: [`docs/diagrams/adapter.png`](diagrams/adapter.png)

---

## Our Implementation — Full Walkthrough

### Target — `MediaPlayer.java`

```java
public interface MediaPlayer {
    void play(String fileName);
}
```

This is the **contract the entire client world depends on**. `AudioPlayer`, `Main`, any future caller — they all see only this. Adding new formats never changes this interface.

### Adaptee Interface — `AdvancedMediaPlayer.java`

```java
public interface AdvancedMediaPlayer {
    void playMp4(String fileName);
    void playVlc(String fileName);
}
```

This is the "other world" — the legacy API with different method names. We cannot and do not change it.

### Concrete Adaptees — `Mp4Player.java` and `VlcPlayer.java`

```java
public class Mp4Player implements AdvancedMediaPlayer {
    @Override
    public void playMp4(String fileName) {
        System.out.println("[MP4]  Playing mp4 file: " + fileName);
    }
    @Override
    public void playVlc(String fileName) { /* no-op — Mp4Player can't handle VLC */ }
}
```

```java
public class VlcPlayer implements AdvancedMediaPlayer {
    @Override
    public void playVlc(String fileName) {
        System.out.println("[VLC]  Playing vlc file: " + fileName);
    }
    @Override
    public void playMp4(String fileName) { /* no-op — VlcPlayer can't handle MP4 */ }
}
```

Each player only implements one format meaningfully. The no-op methods exist purely to satisfy the interface contract. This is exactly the incompatibility that makes an adapter necessary — you can't call `playMp4` from code that only knows about `play`.

### Adapter — `MediaAdapter.java`

```java
public class MediaAdapter implements MediaPlayer {       // ← speaks the Target language

    private final AdvancedMediaPlayer advancedPlayer;   // ← holds the Adaptee
    private final String format;

    public MediaAdapter(String format) {
        this.format = format;
        if (format.equals("mp4"))      advancedPlayer = new Mp4Player();
        else if (format.equals("vlc")) advancedPlayer = new VlcPlayer();
        else throw new IllegalArgumentException("No advanced player for format: " + format);
    }

    @Override
    public void play(String fileName) {             // ← called by client (Target API)
        if (format.equals("mp4")) advancedPlayer.playMp4(fileName);  // ← translated to Adaptee API
        else                      advancedPlayer.playVlc(fileName);
    }
}
```

This is the only class in the system that imports both worlds. It:
- **Looks like** a `MediaPlayer` to any caller (implements the Target)
- **Talks to** an `AdvancedMediaPlayer` internally (holds the Adaptee)
- **Translates** `play()` → `playMp4()` or `playVlc()` depending on format

If the underlying library is ever replaced (e.g., `Mp4Player` becomes `FfmpegMp4Player`), only this file changes.

### Client — `AudioPlayer.java`

```java
public class AudioPlayer implements MediaPlayer {

    @Override
    public void play(String fileName) {
        if (fileName.endsWith(".mp3")) {
            System.out.println("[MP3]  Playing natively:  " + fileName);
        } else if (fileName.endsWith(".mp4")) {
            new MediaAdapter("mp4").play(fileName);   // same call as MP3 — just play()
        } else if (fileName.endsWith(".vlc")) {
            new MediaAdapter("vlc").play(fileName);
        } else {
            System.out.println("[ERR]  Unsupported format: " + fileName);
        }
    }
}
```

`AudioPlayer` knows about `MediaAdapter` — but **not** about `Mp4Player` or `VlcPlayer`. Adding a new format (e.g., `.mkv`) means writing a new concrete Adaptee, a new adapter condition here, and nothing else.

### Main — `Main.java`

```java
MediaPlayer player = new AudioPlayer();

player.play("song.mp3");    // [MP3]  Playing natively
player.play("movie.mp4");   // [MP4]  Playing mp4 file
player.play("clip.vlc");    // [VLC]  Playing vlc file
player.play("video.avi");   // [ERR]  Unsupported format
```

`Main` uses only `MediaPlayer`. It never imports `MediaAdapter`, `Mp4Player`, or `VlcPlayer`. The entire subsystem is invisible.

---

## How the Translation Works — Call Trace

Tracing `player.play("movie.mp4")` step by step:

```
Main
  └─ player.play("movie.mp4")
       └─ AudioPlayer.play("movie.mp4")         ← client, sees only MediaPlayer
            └─ new MediaAdapter("mp4")
                 └─ advancedPlayer = new Mp4Player()
            └─ adapter.play("movie.mp4")         ← still the Target API
                 └─ MediaAdapter.play()          ← adapter intercepts
                      └─ advancedPlayer.playMp4("movie.mp4")  ← Adaptee API
                           └─ "[MP4]  Playing mp4 file: movie.mp4"
```

The call starts and ends as `play()`. Inside the adapter, it becomes `playMp4()`. The translation is invisible to the caller.

---

## Two Variants — Object Adapter vs Class Adapter

### Object Adapter (what we use) — Composition

```java
public class MediaAdapter implements MediaPlayer {
    private final AdvancedMediaPlayer advancedPlayer;  // wraps an instance
}
```

- Wraps a **reference** to the Adaptee
- Can work with any subclass of Adaptee (swap `Mp4Player` for a new implementation)
- Works even if Adaptee is `final`
- **Preferred in almost every situation**

### Class Adapter — Inheritance

```java
// Only possible in languages that support multiple inheritance (C++, or via mixins)
// In Java: only works if Adaptee is a class (not interface) and you want exactly one
public class Mp4Adapter extends Mp4Player implements MediaPlayer {
    @Override
    public void play(String fileName) {
        playMp4(fileName);   // inherited directly from Mp4Player
    }
}
```

- Inherits the Adaptee instead of wrapping it
- Tightly couples adapter to one specific Adaptee class
- Cannot adapt subclasses or swap the implementation
- In Java, limited because Java has no multiple class inheritance
- **Rarely the right choice** — use only when you need to override Adaptee behaviour, not just translate it

| | Object Adapter | Class Adapter |
|---|---|---|
| Mechanism | Composition | Inheritance |
| Can adapt multiple Adaptee types | Yes | No |
| Works if Adaptee is `final` | Yes | No |
| Can override Adaptee methods | No | Yes |
| Coupling to Adaptee | Low | High |
| Java support | Full | Limited |

---

## More Real-World Examples

### Example 2 — Payment Gateway Integration

Your system has a `PaymentProcessor` interface. You integrate Stripe, then later PayPal. Both SDKs have completely different method names.

```java
// Target — what your system calls
public interface PaymentProcessor {
    void processPayment(double amount, String currency);
    boolean refund(String transactionId);
}

// Adaptee — Stripe SDK (cannot modify)
public class StripeClient {
    public StripeCharge charge(long amountInCents, String currency) { ... }
    public StripeRefund refund(String chargeId) { ... }
}

// Adapter — translates your interface into Stripe's API
public class StripeAdapter implements PaymentProcessor {
    private final StripeClient stripe = new StripeClient();

    @Override
    public void processPayment(double amount, String currency) {
        long cents = Math.round(amount * 100);      // unit conversion
        stripe.charge(cents, currency);              // method name translation
    }

    @Override
    public boolean refund(String transactionId) {
        StripeRefund result = stripe.refund(transactionId);
        return result.getStatus().equals("succeeded");  // return type translation
    }
}
```

Your checkout code calls `processor.processPayment(49.99, "USD")`. It never changes whether Stripe, PayPal, or a new provider is underneath.

---

### Example 3 — Java's Own `InputStreamReader`

This is the most famous Adapter in the Java standard library. Java I/O has two separate hierarchies:

- `InputStream` / `OutputStream` — **byte-oriented** (reads `byte[]`)
- `Reader` / `Writer` — **character-oriented** (reads `char[]`, handles encoding)

They serve overlapping purposes but have completely different APIs. `InputStreamReader` bridges them:

```java
// Target: Reader interface
public abstract class Reader {
    public abstract int read(char[] cbuf, int off, int len);
}

// Adaptee: InputStream (byte-based)
public abstract class InputStream {
    public abstract int read(byte[] b, int off, int len);
}

// Adapter: InputStreamReader
public class InputStreamReader extends Reader {
    private final InputStream in;          // wraps an InputStream (Object Adapter)
    private final Charset charset;

    public InputStreamReader(InputStream in, String charsetName) {
        this.in = in;
        this.charset = Charset.forName(charsetName);
    }

    @Override
    public int read(char[] cbuf, int off, int len) {
        // reads bytes from InputStream, decodes them to chars using charset
        byte[] bytes = new byte[len];
        int bytesRead = in.read(bytes, 0, len);
        // ... decode bytes → chars ...
    }
}
```

Usage:

```java
// Client only knows Reader — doesn't care that the source is a socket InputStream
Reader reader = new InputStreamReader(socket.getInputStream(), "UTF-8");
reader.read(buffer, 0, 1024);
```

The client calls `read(char[])`. The adapter reads `byte[]` internally and decodes. The byte/character mismatch is invisible.

---

### Example 4 — `Arrays.asList()` — Array to List

Arrays and `List` are both sequences, but they have entirely different APIs. `Arrays.asList()` is a built-in Adapter:

```java
// Adaptee: T[] (plain Java array)
String[] array = {"Alice", "Bob", "Carol"};

// Adapter: Arrays.asList() wraps the array behind the List interface (Target)
List<String> list = Arrays.asList(array);

// Client: uses only List API — never touches array indices
list.forEach(System.out::println);
list.contains("Bob");
list.size();
```

The returned `List` is backed by the original array. It's not a copy — it's an adapter that translates `List` method calls into array operations. The client code works with any `List` and never needs to know an array is underneath.

---

### Example 5 — Legacy Logger Integration

Your application uses `SLF4J` (a modern logging facade). A third-party library you pulled in uses `java.util.logging` (JUL) directly. Log output from that library goes to a different destination.

```java
// Target: SLF4J Logger (what your app uses)
Logger logger = LoggerFactory.getLogger(MyClass.class);
logger.info("Application started");

// Adaptee: java.util.logging (what the third-party lib uses)
java.util.logging.Logger julLogger = java.util.logging.Logger.getLogger("ThirdParty");
julLogger.log(Level.INFO, "Third party event");

// Adapter: SLF4JBridgeHandler
// Installs itself as a JUL Handler; translates JUL log records → SLF4J calls
SLF4JBridgeHandler.install();
// Now JUL calls are transparently routed through SLF4J
```

`SLF4JBridgeHandler` implements `java.util.logging.Handler` (Adaptee's extension point) and internally calls `SLF4J` methods. All log output flows through one system. Neither your code nor the third-party code changes.

---

### Example 6 — Database Driver (JDBC)

JDBC's entire design is Adapter-based. You write to the `java.sql` interfaces (Target). Each database vendor ships a driver (Adapter) that translates those calls into the vendor's wire protocol.

```java
// Target: standard JDBC interface — same code for every database
Connection conn = DriverManager.getConnection(url, user, pass);
PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE id = ?");
stmt.setInt(1, userId);
ResultSet rs = stmt.executeQuery();

// Underneath:
// PostgreSQL Driver: translates to Postgres wire protocol
// MySQL Driver:      translates to MySQL protocol
// Oracle Driver:     translates to Oracle TNS protocol
```

Switching from MySQL to PostgreSQL means changing the JDBC URL. Your SQL queries and all `Connection`/`Statement`/`ResultSet` calls are unchanged — the adapter (driver) absorbs the difference.

---

## When to Use Adapter

**Use it when:**
- You want to use an existing class but its interface doesn't match what you need
- You're integrating a third-party or legacy library you cannot modify
- You need to unify several classes with different APIs behind one interface
- You want to insulate your code from future changes in a dependency

**Don't use it when:**
- You own both sides — just change the interface; adapters add indirection for no benefit
- The translation is complex business logic, not just method-name mapping — that's a service, not an adapter
- You need bidirectional translation — a single Adapter only converts one direction

---

## Adapter vs Facade vs Decorator vs Proxy

All four wrap something. The difference is *why*:

| Pattern | Wraps | Changes interface? | Typical target | Intent |
|---|---|---|---|---|
| **Adapter** | One class | Yes — translates | Third-party / legacy code | Make incompatible interfaces compatible |
| **Facade** | A subsystem (many classes) | Yes — simplifies | Your own complex subsystem | Reduce complexity for callers |
| **Decorator** | One object | No — same interface | Any object | Add behaviour at runtime |
| **Proxy** | One object | No — same interface | Expensive or sensitive objects | Control access |

The fastest distinguishing questions:

- Are the interfaces **incompatible**? → **Adapter**
- Are the interfaces compatible but there are **too many of them**? → **Facade**
- Same interface, want to **add behaviour** transparently? → **Decorator**
- Same interface, want to **control when** the real object is used? → **Proxy**
