# Design Patterns — Differences and When to Use What

A concise reference covering all patterns in this project. Use this when you know *something* is wrong with your design but aren't sure which pattern to reach for.

---

## The Three Categories

| Category        | Theme                                      | Patterns in this project                              |
|-----------------|--------------------------------------------|-------------------------------------------------------|
| **Creational**  | How objects are created                    | Singleton, Builder, Factory Method, Abstract Factory  |
| **Structural**  | How objects are composed into larger units | Adapter, Composite, Decorator, Facade, Proxy          |
| **Behavioural** | How objects communicate and share work     | Strategy, Observer, State, Chain of Responsibility    |

---

## At a Glance

| Pattern           | One-Line Purpose                                              |
|-------------------|---------------------------------------------------------------|
| Singleton         | Guarantee exactly one instance of a class                     |
| Builder           | Construct a complex object step by step                       |
| Factory Method    | Let subclasses decide which concrete class to instantiate     |
| Abstract Factory  | Create families of related objects without naming their classes |
| Adapter           | Make an incompatible interface work where another is expected  |
| Composite         | Treat individual objects and groups of objects uniformly      |
| Decorator         | Add behaviour to an object without changing its class         |
| Facade            | Simplify a complex subsystem behind a single entry point      |
| Proxy             | Control access to an object (lazy load, guard, cache)         |
| Strategy          | Encapsulate a family of algorithms and make them interchangeable |
| Observer          | Notify many dependents automatically when one object's state changes |
| State             | Change an object's behaviour when its internal state changes         |
| Chain of Responsibility | Pass a request along a chain of handlers until one handles it |

---

## Creational Patterns

These answer the question: **"How should I create this object?"**

### Singleton vs Builder vs Factory Method vs Abstract Factory

```
Need exactly ONE instance of something?
  → Singleton

Need to build ONE complex object with many optional parts?
  → Builder

Need to create ONE kind of object, but the exact type varies?
  → Factory Method

Need to create SEVERAL related objects that must be compatible?
  → Abstract Factory
```

| Dimension             | Singleton          | Builder                  | Factory Method              | Abstract Factory               |
|-----------------------|--------------------|--------------------------|-----------------------------|--------------------------------|
| **What it creates**   | 1 instance, always | 1 complex object          | 1 object of a varying type  | A family of related objects    |
| **Who decides type**  | Fixed              | You, step by step         | Subclass                    | A factory class                |
| **Optional config**   | No                 | Yes — that's its purpose  | No                          | No                             |
| **Key mechanism**     | Private constructor + static instance | Fluent builder class | `createProduct()` overridden by subclass | Interface per factory, one per family |
| **Our example**       | `DatabaseConnection` | `Student` / `StudentBuilder` | (CreatorFactory hierarchy) | (UIFactory / ButtonFactory)  |

**When to use Singleton:**
- A shared resource that must be initialised once: DB connection pool, config loader, logger, thread pool.
- Danger: global state is hard to test. Prefer dependency injection; use Singleton only at the outermost wiring layer.

**When to use Builder:**
- An object has more than 3–4 constructor parameters, especially optional ones.
- Telescoping constructors (`Student(name)`, `Student(name, age)`, `Student(name, age, address)`) are getting out of hand.
- You want readable, step-by-step construction: `new StudentBuilder("Alice").setAge(22).setAddress("Berlin").build()`.

**When to use Factory Method:**
- You know *what* you need to create but want subclasses to decide *which* concrete class to use.
- You're building a framework and need to leave the "which product" decision open for extenders.
- Adding a new product type means adding a new subclass, not touching existing code (Open/Closed Principle).

**When to use Abstract Factory:**
- You must guarantee that a set of related objects are from the same family (e.g., `WindowsButton` + `WindowsCheckbox`, never `WindowsButton` + `MacCheckbox`).
- Swapping a whole product family (e.g., switching from light theme to dark theme) at once.

---

## Structural Patterns

These answer the question: **"How should I connect these objects?"**

### Adapter vs Composite vs Decorator vs Facade vs Proxy

All five wrap or connect objects — the difference is *why*:

```
Incompatible interfaces that need to talk?
  → Adapter

Tree of objects that should be treated the same as a single object?
  → Composite

Add behaviour to one object at runtime without subclassing?
  → Decorator

Too many classes/steps for clients to deal with directly?
  → Facade

Control when/whether a client accesses an object?
  → Proxy
```

| Dimension                  | Adapter            | Composite             | Decorator              | Facade                  | Proxy                      |
|----------------------------|--------------------|-----------------------|------------------------|-------------------------|----------------------------|
| **Wraps**                  | One object         | A tree of objects      | One object             | Multiple classes         | One object                  |
| **Changes interface?**     | Yes — translates   | No                    | No                     | Yes — simplifies         | No — same interface         |
| **Client aware of it?**    | No (ideally)       | No                    | Often yes (chains)     | Yes (calls it directly)  | No (transparent)            |
| **Creates the real object?** | No — receives it | Yes (children added)  | No — receives it       | No — receives subsystem  | Often yes (lazy)            |
| **Our example**            | `MediaAdapter`     | `Folder` / `File`     | `RetryDecorator`       | `HomeTheaterFacade`      | `ProxyImage`                |

### The "they all wrap something" confusion — resolved

Three patterns share the same interface as the thing they wrap: **Proxy**, **Decorator**, and (sometimes) **Adapter**. Here's how to tell them apart:

| Pattern       | Same interface? | Why it wraps                                        | Typical relationship          |
|---------------|-----------------|-----------------------------------------------------|-------------------------------|
| **Proxy**     | Always yes      | Control access — the client shouldn't notice        | Proxy often *creates* the real object |
| **Decorator** | Always yes      | Add behaviour — client deliberately stacks layers   | Client *provides* the real object |
| **Adapter**   | No — translates | Compatibility — connect two systems that can't talk | Adapter *receives* the adaptee |

```java
// Decorator — layering is the point; client builds the stack
Notification n = new RetryDecorator(new FormattingDecorator(new SMS()));

// Proxy — transparency is the point; client just thinks it has an Image
Image img = new ProxyImage("photo.jpg");   // no hint it's a proxy

// Adapter — translation is the point; client uses MediaPlayer, adapter speaks AdvancedMediaPlayer
MediaPlayer player = new AudioPlayer();
player.play("clip.vlc");   // MediaAdapter translates play() → playVlc() internally
```

### Facade vs Adapter — the most common mix-up

Both provide a "new face" for something existing, but:

| | Facade | Adapter |
|--|--------|---------|
| **How many things does it cover?** | Many classes (a whole subsystem) | One class |
| **Why is the interface different?** | Simplification — fewer methods, higher level | Translation — incompatible signatures |
| **Did you design both sides?** | Usually yes — your own subsystem | Usually no — adapting legacy/third-party code |

> Rule of thumb: if you're cleaning up your own messy subsystem → **Facade**. If you're connecting to someone else's incompatible API → **Adapter**.

### Facade vs Proxy — both hide the real object

| | Facade | Proxy |
|--|--------|-------|
| **Same interface as real object?** | No — simplified | Yes — identical |
| **Wraps one or many?** | Many classes | One class |
| **Purpose** | Reduce complexity | Control access |

---

## Behavioural Patterns

These answer the question: **"How should these objects share work and communicate?"**

### Strategy

Strategy encapsulates an algorithm behind an interface so it can be swapped at runtime. The Context holds a Strategy reference and delegates to it — it never implements the algorithm itself.

```
One operation, many ways to do it?
  → Strategy

The variant is selected by the client at runtime?
  → Strategy (not Template Method, not State)
```

| Dimension          | Strategy                                       |
|--------------------|------------------------------------------------|
| **What varies**    | The algorithm / policy                         |
| **Who decides**    | Client — passes a strategy object              |
| **Mechanism**      | Composition — Context delegates to Strategy   |
| **Runtime switch** | Yes — via `setStrategy()`                      |
| **Our example**    | `DataSorter` + `BubbleSortStrategy` / `QuickSortStrategy` / `MergeSortStrategy` |

**When to use Strategy:**
- You have a family of related algorithms and clients need to pick between them
- An `if/else` or `switch` that selects an algorithm is growing — each branch should be a strategy
- You want to test each algorithm independently (each concrete strategy is a standalone class)
- You need to swap behaviour at runtime based on config, user preference, or input size

---

### Observer

Observer defines a one-to-many dependency so that when one object changes state, all registered dependents are notified automatically. The Subject holds a list of Observers and calls each one's `update()` method — it doesn't know who they are, only that they implement the interface.

```
One object changes state; many others must react?
  → Observer

Dependents can join or leave at runtime?
  → Observer (not hard-coded callbacks)

Subject should not know the concrete types of its listeners?
  → Observer
```

| Dimension          | Observer                                           |
|--------------------|----------------------------------------------------|
| **What varies**    | Who is listening and how they react                |
| **Who decides**    | Observers register themselves                      |
| **Mechanism**      | Subject iterates `List<Observer>` and calls `update()` |
| **Runtime switch** | Yes — `registerObserver()` / `removeObserver()`    |
| **Our example**    | `WeatherStation` + `CurrentConditionsDisplay` / `StatisticsDisplay` / `ForecastDisplay` |

**When to use Observer:**
- One object's change must propagate to an unknown number of other objects
- An object should notify others without knowing who or how many they are
- Objects should be able to subscribe and unsubscribe independently (event systems, UI panels, plugin registries)
- You want to decouple a data source from its consumers (reactive data pipelines, domain event broadcasting)

---

### State

State allows an object to change its behaviour when its internal state changes. The Context holds a reference to the current State and delegates all operations to it. Concrete States implement the same interface and trigger transitions by calling `context.setState(...)` directly.

```
Object must behave differently depending on its current mode?
  → State

Behaviour varies across multiple methods for the same state value — giant if/else or switch?
  → State (not Strategy, not polymorphism)

Transitions are complex and should be encapsulated — not driven by client code?
  → State
```

| Dimension          | State                                                         |
|--------------------|---------------------------------------------------------------|
| **What varies**    | Behaviour per state + which transitions are valid             |
| **Who decides**    | The current State — it transitions itself                     |
| **Mechanism**      | Context delegates to `currentState`; states call `setState()` |
| **Runtime switch** | Automatic — states trigger transitions on operations          |
| **Our example**    | `VendingMachine` + `IdleState` / `HasCoinState` / `DispensingState` / `OutOfStockState` |

**When to use State:**
- An object's behaviour depends on its state and must change at runtime
- Multiple methods have large conditionals branching on the same state variable — each branch should be its own class
- Transitions between states have their own logic that belongs near the state, not scattered across every method
- You want to add new states without modifying existing classes (Open/Closed Principle)

---

### Chain of Responsibility

Chain of Responsibility passes a request along a chain of handlers. Each handler decides to process the request or pass it to the next handler. The sender doesn't know which handler will ultimately process it — they only talk to the first link.

```
Multiple objects could handle a request, but you don't know which one at compile time?
  → Chain of Responsibility

Adding new handlers should not require changing existing ones?
  → Chain of Responsibility (not a giant if/else dispatcher)

The handler set or order should be configurable at runtime?
  → Chain of Responsibility
```

| Dimension          | Chain of Responsibility                                                   |
|--------------------|---------------------------------------------------------------------------|
| **What varies**    | Which handler processes the request; the pass-or-stop logic               |
| **Who decides**    | Each handler independently — checks, then either handles or passes        |
| **Mechanism**      | Linked handlers; each calls `passToNext()` if it can't handle             |
| **Runtime switch** | Chain wired at startup; partial chains and reordering possible            |
| **Our example**    | `SupportRequest` escalating through `FrontlineSupport` → `TechnicalSupport` → `SeniorEngineer` → `ManagementEscalation` |

**When to use Chain of Responsibility:**
- More than one object may handle a request and you don't know which at compile time
- You want to issue a request without specifying the receiver explicitly
- The set of handlers should be configurable or extendable without touching existing handlers
- You have a growing if/else or switch that selects a handler — each branch should be a class

---

```
What problem am I solving?
│
├─ Creating objects
│    ├─ Need only one instance ever ──────────────────────────→ Singleton
│    ├─ Complex object with many optional parameters ─────────→ Builder
│    ├─ Type varies but creation logic should be centralised
│    │    ├─ Subclass decides the type ───────────────────────→ Factory Method
│    │    └─ Creating a family of related types together ─────→ Abstract Factory
│
├─ Connecting / composing objects
│    ├─ Incompatible APIs need to talk ───────────────────────→ Adapter
│    ├─ Recursive tree, uniform treatment ────────────────────→ Composite
│    ├─ Add behaviour at runtime, stackable ──────────────────→ Decorator
│    ├─ Subsystem is complex, client needs simplicity ────────→ Facade
│    └─ Control access (lazy load / guard / cache) ───────────→ Proxy
│
     └─ Varying behaviour
          ├─ Family of algorithms, client picks one at runtime ────→ Strategy
          ├─ One object changes; many others must react ──────────→ Observer
          ├─ Object's own behaviour changes based on its mode ────→ State
          └─ Request travels a chain; first match handles it ─────→ Chain of Responsibility
```

---

## Real-World Anchors

| Pattern          | Where you see it in the wild                                                    |
|------------------|---------------------------------------------------------------------------------|
| Singleton        | Spring beans (default scope), `Runtime.getRuntime()`, `Logger` instances        |
| Builder          | `StringBuilder`, `HttpRequest.Builder`, Lombok `@Builder`, JPA `CriteriaQuery` |
| Factory Method   | `Calendar.getInstance()`, `NumberFormat.getCurrencyInstance()`                  |
| Abstract Factory | Swing `LookAndFeel`, JDBC `DriverManager` + `Connection` + `Statement`          |
| Adapter          | `Arrays.asList()` (array → List), SLF4J binding adapters, Spring MVC `HandlerAdapter` |
| Composite        | `java.awt.Container`, JUnit `TestSuite`, XML/HTML DOM tree                      |
| Decorator        | Java I/O streams (`BufferedReader(new FileReader(...))`), Spring `@Cacheable`   |
| Facade           | `JdbcTemplate`, SLF4J `LoggerFactory`, `java.net.URL.openStream()`              |
| Proxy            | Spring AOP (`@Transactional`, `@Secured`), Hibernate lazy entities, Java RMI    |
| Strategy         | `java.util.Comparator`, `Arrays.sort(array, comparator)`, Spring `ResourceLoader`, `javax.servlet.Filter` chain |
| Observer         | `java.beans.PropertyChangeListener`, `EventListener` (Swing/AWT), Android `LiveData.observe()`, Spring `@EventListener`, DOM `addEventListener` |
| State            | TCP socket states (`CLOSED`→`ESTABLISHED`→`CLOSE_WAIT`), `javax.faces.lifecycle.Lifecycle`, UI button states (idle / loading / error), order workflows (PENDING → SHIPPED) |
| Chain of Responsibility | `javax.servlet.Filter` + `FilterChain`, Spring Security `SecurityFilterChain`, Java `Logger.getParent()` log propagation, Express/Django middleware `next()` |

---

## Quick Smell-to-Pattern Map

| Code smell / problem                              | Pattern to consider     |
|---------------------------------------------------|-------------------------|
| `new Foo()` scattered everywhere, hard to swap    | Factory Method          |
| 5-parameter constructor, most params optional     | Builder                 |
| Only one instance makes sense, but it's duplicated | Singleton              |
| `instanceof` / casting to call the right method   | Adapter                 |
| Recursive structure, clients distinguish leaf vs container | Composite       |
| Adding features via subclass explosion            | Decorator               |
| Client must know 6 classes just to do one thing   | Facade                  |
| Expensive object created even when not used       | Proxy (Virtual)         |
| Same object accessed by untrusted code            | Proxy (Protection)      |
| Object lives on a remote server                   | Proxy (Remote)          |
| One operation done in multiple ways; way is chosen at runtime | Strategy    |
| One object changes; many others must be notified automatically | Observer   |
| Object behaves differently depending on its current mode; transitions are complex | State |
| Multiple objects could handle a request; dispatcher grows with every new handler  | Chain of Responsibility |
