# Proxy Design Pattern

## What Is It?

The Proxy pattern places a **surrogate object in front of another object** to control access to it — intercepting calls to add behaviour (lazy loading, access control, caching, logging) without the client knowing.

> "Provide a surrogate or placeholder for another object to control access to it."
> — Gang of Four, *Design Patterns*

The core insight: sometimes you don't want to give a client direct access to an object — because it's expensive to create, sensitive, remote, or needs guarding. The proxy sits between the client and the real object, looking identical from the outside.

---

## The Problem It Solves

Loading a high-resolution image from disk is expensive. If you have a gallery of 50 images, loading all of them upfront is wasteful — the user may only view 3.

Without Proxy, the client loads everything eagerly:

```java
// Without Proxy — all images loaded at startup, even if never displayed
List<Image> gallery = List.of(
    new RealImage("photo1.jpg"),    // loads from disk immediately
    new RealImage("photo2.jpg"),    // loads from disk immediately
    new RealImage("photo3.jpg")     // loads from disk immediately
    // ... 47 more
);
```

The cost is paid upfront, regardless of what the user actually opens.

**With Proxy, the cost is paid only when needed:**

```java
List<Image> gallery = List.of(
    new ProxyImage("photo1.jpg"),   // no disk I/O yet
    new ProxyImage("photo2.jpg"),   // no disk I/O yet
    new ProxyImage("photo3.jpg")    // no disk I/O yet
);
gallery.get(0).display();           // only NOW does photo1 load
```

The client code is identical in both cases — it calls `display()` on an `Image`. The proxy is invisible.

---

## Three Common Proxy Types

| Type                  | What it controls                                              | Example                          |
|-----------------------|---------------------------------------------------------------|----------------------------------|
| **Virtual Proxy**     | Defers expensive object creation until first use (lazy init) | `ProxyImage` in our code         |
| **Protection Proxy**  | Guards access based on permissions or roles                  | `AdminProxy` checking user role  |
| **Remote Proxy**      | Represents an object in a different address space            | Java RMI stub, gRPC client stub  |

**We implement a Virtual Proxy** — the most common type and clearest demonstration of the pattern.

---

## Roles

| Role             | Responsibility                                                   | In our code   |
|------------------|------------------------------------------------------------------|---------------|
| **Subject**      | Interface implemented by both Proxy and Real Subject            | `Image`       |
| **Real Subject** | The actual object doing the work — expensive or sensitive       | `RealImage`   |
| **Proxy**        | Same interface; controls access; holds a reference to Real Subject | `ProxyImage`  |
| **Client**       | Works only with the Subject interface — can't tell proxy from real | `Main`        |

---

## Structure

```
Client
  └── image.display()        [via Image interface]
          │
     ProxyImage              [Proxy]
       ├── if first call → create RealImage → delegate
       └── if cached → delegate directly

     RealImage               [Real Subject]
       └── loadFromDisk() + display()
```

Class diagram: [`docs/diagrams/proxy.png`](diagrams/proxy.png)

---

## Our Implementation

### Subject — `Image.java`

```java
public interface Image {
    void display();
}
```

The shared contract. Client code depends only on this — it never imports `RealImage` or `ProxyImage` directly.

### Real Subject — `RealImage.java`

```java
public class RealImage implements Image {
    private final String fileName;

    public RealImage(String fileName) {
        this.fileName = fileName;
        loadFromDisk();             // expensive — happens in constructor
    }

    private void loadFromDisk() {
        System.out.println("[REAL]  Loading:  " + fileName);
    }

    @Override
    public void display() {
        System.out.println("[REAL]  Displaying: " + fileName);
    }
}
```

- Loading happens in the constructor — creating a `RealImage` always pays the cost
- This is exactly the behaviour the proxy defers

### Proxy — `ProxyImage.java`

```java
public class ProxyImage implements Image {
    private final String fileName;
    private RealImage realImage;    // null until first access

    public ProxyImage(String fileName) {
        this.fileName = fileName;
        // RealImage is NOT created here — that's the whole point
    }

    @Override
    public void display() {
        if (realImage == null) {
            System.out.println("[PROXY] First access — creating RealImage for: " + fileName);
            realImage = new RealImage(fileName);    // lazy initialisation
        } else {
            System.out.println("[PROXY] Cache hit — reusing RealImage for: " + fileName);
        }
        realImage.display();
    }
}
```

- Constructor is cheap — no disk I/O
- `realImage` starts null; created only on first `display()` call
- Subsequent calls reuse the cached instance — no reload
- The client calls `display()` on a `ProxyImage` exactly as it would on a `RealImage`

### Client — `Main.java`

```java
Image img1 = new ProxyImage("photo_4k.jpg");   // cheap — no load yet
Image img2 = new ProxyImage("wallpaper.png");   // cheap — no load yet

img1.display();   // first call → loads + displays
img1.display();   // second call → cache hit, displays only
// img2.display() never called → wallpaper.png never loaded
```

The client declares both as `Image`. It cannot tell — and doesn't need to tell — which is real and which is a proxy.

---

## How It Works

Tracing `img1.display()` on first and second call:

```
First call:
Main
  └─ img1.display()                        [ProxyImage]
       └─ realImage == null → create
            └─ new RealImage("photo_4k.jpg")
                 └─ loadFromDisk()         [prints: Loading]
       └─ realImage.display()              [RealImage]
            └─ prints: Displaying

Second call:
Main
  └─ img1.display()                        [ProxyImage]
       └─ realImage != null → cache hit
       └─ realImage.display()              [RealImage]
            └─ prints: Displaying          (no load)
```

The expensive `loadFromDisk()` runs exactly once, no matter how many times `display()` is called.

---

## When to Use Proxy

Use Proxy when:

- Object creation is **expensive** and you want to defer it until actually needed (Virtual Proxy)
- You need **access control** — only certain callers should reach the real object (Protection Proxy)
- The real object is **remote** and you need a local stand-in that handles the network call (Remote Proxy)
- You want to **log, cache, or audit** calls to an object without modifying it (Smart Proxy)
- You need to **count references** to an object and free it when no longer used (Reference-counting Proxy)

Don't use it when:

- The object is cheap to create — a proxy adds indirection for no gain
- You need to **change the interface** — use Adapter instead; a proxy always exposes the same interface
- You need to **add behaviour visibly** — use Decorator; a proxy is meant to be transparent

---

## Real-World Examples

| Domain                 | Subject Interface     | Real Subject          | Proxy                         |
|------------------------|-----------------------|-----------------------|-------------------------------|
| Image gallery          | `Image`               | `RealImage`           | `ProxyImage`                  |
| Java RMI               | Remote interface      | Remote object         | RMI stub                      |
| Hibernate ORM          | Entity class          | Loaded entity         | Lazy-loaded proxy entity      |
| Spring AOP             | Service interface     | Service bean          | CGLIB / JDK dynamic proxy     |
| `java.lang.reflect.Proxy` | Any interface      | Any object            | `InvocationHandler` proxy     |
| CDN                    | Origin server         | Content on disk       | CDN edge node                 |
| Security firewall      | Network resource      | Internal server       | Proxy server                  |

Spring AOP is the canonical Java example — every `@Transactional`, `@Cacheable`, and `@Secured` method is intercepted by a Spring-generated proxy that wraps the real bean.

---

## Proxy vs Decorator vs Adapter

All three wrap an object — but with different goals:

| Pattern       | Same interface as wrapped? | Creates the real object? | Primary intent                             |
|---------------|----------------------------|--------------------------|--------------------------------------------|
| **Proxy**     | Yes — always               | Often yes (lazy)         | Control access; transparent to client      |
| **Decorator** | Yes — always               | No — receives it         | Add behaviour; client knows it's decorated |
| **Adapter**   | No — translates            | No — receives it         | Make incompatible interfaces compatible    |

The subtlest distinction is **Proxy vs Decorator**:
- A **Proxy** controls **whether and when** the real object is accessed — the client doesn't know a proxy is involved
- A **Decorator** controls **what extra happens** around an access — the client typically chains decorators deliberately

```java
// Decorator — client explicitly stacks layers
new LoggingImage(new ResizingImage(realImage))

// Proxy — client thinks it has the real thing
Image img = new ProxyImage("photo.jpg");   // looks like RealImage to caller
```
