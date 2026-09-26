# Observer Design Pattern

## What Is It?

The Observer pattern **defines a one-to-many dependency between objects** so that when one object changes state, all its dependents are notified and updated automatically.

> "Define a one-to-many dependency between objects so that when one object changes state, all its dependents are notified and updated automatically."
> — Gang of Four, *Design Patterns*

Think of a newspaper subscription. When the publisher prints a new edition, every subscriber gets a copy — they don't have to call the publisher to ask. Subscribers can join or cancel at any time, and the publisher doesn't need to know who they are, only that they want to receive the paper.

The pattern separates the **thing that changes state** (the Subject/Publisher) from the **things that react to that change** (the Observers/Subscribers). The subject doesn't know what observers do with the notification — it just sends it.

---

## The Problem It Solves

A `WeatherStation` needs to push new measurements to several display components: a current-conditions panel, a statistics chart, and a forecast widget.

The naive approach builds all the update logic directly into the station:

```java
public class WeatherStation {

    private CurrentConditionsDisplay current;
    private StatisticsDisplay stats;
    private ForecastDisplay forecast;

    public void setMeasurements(float temp, float humidity, float pressure) {
        this.temperature = temp;
        // ...
        current.update(temp, humidity, pressure);    // hard-coded
        stats.update(temp, humidity, pressure);      // hard-coded
        forecast.update(temp, humidity, pressure);   // hard-coded
        // Adding a new display means editing this method
    }
}
```

**Problems with this:**
- `WeatherStation` is coupled to every display class — adding a fourth display requires editing the station
- You can't remove a display at runtime — the station always calls all three
- Displays can't be added by outside code (plugins, tests) without modifying the station
- The station grows without bound; it must know the concrete type of every consumer

The station should broadcast a change and let interested parties react. It shouldn't know who those parties are or how many there are.

**With Observer**, the station holds only a `List<Observer>`. Any display registers itself; the station calls `notifyObservers()`; each observer handles the update however it likes. Adding a display means registering it — the station never changes.

---

## Roles

| Role                    | Responsibility                                                              | In our code                                                  |
|-------------------------|-----------------------------------------------------------------------------|--------------------------------------------------------------|
| **Subject**             | Interface for managing the observer list and triggering notifications       | `Subject`                                                    |
| **Concrete Subject**    | Holds actual state; calls `notifyObservers()` when state changes            | `WeatherStation`                                             |
| **Observer**            | Interface for receiving state-change notifications                          | `Observer`                                                   |
| **Concrete Observer**   | Reacts to notifications in a domain-specific way                            | `CurrentConditionsDisplay`, `StatisticsDisplay`, `ForecastDisplay` |
| **Client**              | Wires up the subject and registers observers                                | `Main`                                                       |

The Subject knows observers only through the `Observer` interface — never as `CurrentConditionsDisplay` or any concrete type.

---

## Structure

```
Main
  ├── station.registerObserver(current)
  ├── station.registerObserver(stats)
  ├── station.registerObserver(forecast)
  │
  └── station.setMeasurements(...)       [WeatherStation — Concrete Subject]
           └── notifyObservers()
                    ├── current.update(...)    [CurrentConditionsDisplay]
                    ├── stats.update(...)      [StatisticsDisplay]
                    └── forecast.update(...)   [ForecastDisplay]
```

```
          ┌────────────────────────────┐
          │       «interface»          │
          │          Subject           │
          │  +registerObserver(o)      │
          │  +removeObserver(o)        │
          │  +notifyObservers()        │
          └────────────┬───────────────┘
                       │ implements
          ┌────────────┴───────────────┐
          │      WeatherStation        │
          │  -List<Observer> observers │
          │  -float temperature        │
          │  -float humidity           │───────▶ «interface»
          │  -float pressure           │          Observer
          │  +setMeasurements(...)     │      +update(temp, hum, prs)
          └────────────────────────────┘              ▲
                                                      │ implements
                                          ┌───────────┼───────────┐
                               CurrentConditions  Statistics   Forecast
                                  Display          Display      Display
```

Class diagram: [`docs/diagrams/observer.png`](diagrams/observer.png)

---

## Our Implementation — Full Walkthrough

### Subject Interface — `Subject.java`

```java
public interface Subject {
    void registerObserver(Observer o);
    void removeObserver(Observer o);
    void notifyObservers();
}
```

The contract for any publisher. The Concrete Subject (`WeatherStation`) implements all three methods. Crucially, `registerObserver` and `removeObserver` accept `Observer` — not a concrete type — so any conforming class can subscribe.

### Observer Interface — `Observer.java`

```java
public interface Observer {
    void update(float temperature, float humidity, float pressure);
}
```

The contract for any subscriber. Every display class implements this. The Subject calls `update()` on each observer during `notifyObservers()` — it never calls any concrete method.

### Concrete Subject — `WeatherStation.java`

```java
public class WeatherStation implements Subject {

    private final List<Observer> observers = new ArrayList<>();
    private float temperature;
    private float humidity;
    private float pressure;

    @Override
    public void registerObserver(Observer o) { observers.add(o); }

    @Override
    public void removeObserver(Observer o) { observers.remove(o); }

    @Override
    public void notifyObservers() {
        for (Observer o : observers) {
            o.update(temperature, humidity, pressure);
        }
    }

    public void setMeasurements(float temperature, float humidity, float pressure) {
        this.temperature = temperature;
        this.humidity    = humidity;
        this.pressure    = pressure;
        System.out.printf("[STATION] ...");
        notifyObservers();    // state changed → broadcast
    }
}
```

The observer list is private — nothing outside `WeatherStation` can iterate or modify it except through `registerObserver` / `removeObserver`. `setMeasurements()` stores the new state, then immediately calls `notifyObservers()`. The notification always delivers the station's current state.

### Concrete Observer 1 — `CurrentConditionsDisplay.java`

```java
public class CurrentConditionsDisplay implements Observer {

    private final String name;

    public CurrentConditionsDisplay(String name) { this.name = name; }

    @Override
    public void update(float temperature, float humidity, float pressure) {
        System.out.printf("[CURRENT] %s — Temp: %.1f°C, Humidity: %.1f%%%n",
                name, temperature, humidity);
    }
}
```

The simplest observer — just print the temperature and humidity with the display's label. It ignores pressure entirely. The station doesn't know or care — it passes all three values, and each observer picks what it needs.

### Concrete Observer 2 — `StatisticsDisplay.java`

```java
public class StatisticsDisplay implements Observer {

    private float minTemp = Float.MAX_VALUE;
    private float maxTemp = Float.MIN_VALUE;
    private float sumTemp = 0;
    private int count = 0;

    @Override
    public void update(float temperature, float humidity, float pressure) {
        if (temperature < minTemp) minTemp = temperature;
        if (temperature > maxTemp) maxTemp = temperature;
        sumTemp += temperature;
        count++;
        System.out.printf("[STATS  ] Temp — min: %.1f°C  max: %.1f°C  avg: %.1f°C%n",
                minTemp, maxTemp, sumTemp / count);
    }
}
```

Each `update()` call accumulates history. The observer is stateful across multiple notifications — it tracks a running min, max, and average over the entire lifetime of its subscription. This state belongs to the observer, not the station.

### Concrete Observer 3 — `ForecastDisplay.java`

```java
public class ForecastDisplay implements Observer {

    private float lastPressure = Float.NaN;   // sentinel: no previous reading

    @Override
    public void update(float temperature, float humidity, float pressure) {
        String forecast;
        if (Float.isNaN(lastPressure) || pressure == lastPressure) {
            forecast = "Stable — same as before";
        } else if (pressure > lastPressure) {
            forecast = "Improving — sunny skies ahead";
        } else {
            forecast = "Worsening — watch for rain";
        }
        lastPressure = pressure;
        System.out.println("[FORECAST] " + forecast);
    }
}
```

The forecast is derived from the *change* in pressure, not its absolute value. `lastPressure` is initialised to `Float.NaN` so the first update always reads as "Stable". This is entirely the observer's own logic — the station knows nothing about pressure trends.

---

## How It Works — Call Trace

Tracing `station.setMeasurements(22.5, 65.0, 1013.0)` when all three observers are registered:

```
Main
  └─ station.setMeasurements(22.5f, 65.0f, 1013.0f)   [WeatherStation]
       ├─ this.temperature = 22.5, humidity = 65.0, pressure = 1013.0
       ├─ prints: [STATION] New readings — ...
       └─ notifyObservers()
            ├─ observers.get(0).update(22.5, 65.0, 1013.0)
            │    └─ CurrentConditionsDisplay.update()
            │         └─ prints: [CURRENT] Living Room — Temp: 22.5°C, Humidity: 65.0%
            │
            ├─ observers.get(1).update(22.5, 65.0, 1013.0)
            │    └─ StatisticsDisplay.update()
            │         └─ min=22.5, max=22.5, avg=22.5 → prints: [STATS  ] ...
            │
            └─ observers.get(2).update(22.5, 65.0, 1013.0)
                 └─ ForecastDisplay.update()
                      └─ lastPressure=NaN → "Stable" → prints: [FORECAST] ...
```

The Subject iterates its list and calls `update()` on each observer in registration order. Each observer runs its logic independently. The Subject doesn't check return values or care what the observers do — it fires and forgets.

---

## Dynamic Subscribe / Unsubscribe

The key capability that distinguishes Observer from hard-coded callbacks is **runtime membership**. Observers can join and leave while the subject is live.

```
Main
  ├─ station.registerObserver(current)     // observers: [current]
  ├─ station.registerObserver(stats)       // observers: [current, stats]
  ├─ station.registerObserver(forecast)    // observers: [current, stats, forecast]
  │
  ├─ station.setMeasurements(22.5, ...)    // all 3 fire → [CURRENT] [STATS  ] [FORECAST]
  ├─ station.setMeasurements(25.0, ...)    // all 3 fire → [CURRENT] [STATS  ] [FORECAST]
  │
  ├─ station.removeObserver(forecast)      // observers: [current, stats]
  │
  └─ station.setMeasurements(19.0, ...)    // only 2 fire → [CURRENT] [STATS  ]
                                           // no [FORECAST] line
```

After `removeObserver(forecast)`, the list shrinks to `[current, stats]`. The next `notifyObservers()` call only iterates those two. `ForecastDisplay` is never called — it's been deregistered.

This means you can:
- Add observers at startup or lazily (e.g., when a UI panel first opens)
- Remove observers cleanly (e.g., when a panel closes, or a subscription expires)
- Write observer logic without any coupling to the subject's internals
- Test `StatisticsDisplay` in isolation by calling `update()` directly

---

## More Real-World Examples

### Java `PropertyChangeListener` / `EventListener`

Every Swing / AWT component is a Subject. `JButton` fires `ActionEvent`s; you register `ActionListener` observers. The button doesn't know what your listener does.

```java
button.addActionListener(e -> System.out.println("Clicked!"));
```

`addActionListener` / `removeActionListener` — this is exactly `registerObserver` / `removeObserver`.

---

### Android `LiveData` and Kotlin `Flow`

`LiveData<T>` (Jetpack) is a Subject. Activities and Fragments are Observers. When the underlying data changes, every active observer receives the new value — automatically, lifecycle-aware.

```kotlin
viewModel.temperature.observe(viewLifecycleOwner) { temp ->
    binding.tempText.text = "$temp°C"
}
```

`observe()` is `registerObserver()`; the lifecycle owner handles `removeObserver()` automatically when the fragment stops.

---

### Spring `ApplicationEvent`

Spring's event bus is a full Observer implementation. Any bean can publish an `ApplicationEvent`; any bean annotated `@EventListener` receives it.

```java
@EventListener
public void onTemperatureChanged(TemperatureChangedEvent event) {
    log.info("Temp changed: {}", event.getTemperature());
}
```

The publisher calls `applicationEventPublisher.publishEvent(event)` — it doesn't know which listeners exist.

---

### DOM `addEventListener`

Every HTML element is a Subject. JavaScript handlers are observers.

```javascript
button.addEventListener('click', () => console.log('Clicked!'));
button.removeEventListener('click', handler);
```

The browser's event system is a GoF Observer with a string-keyed event type added on top.

---

### Kafka Consumers

A Kafka topic is a Subject. Consumer groups are Observers. Producers publish messages without knowing who consumes them; consumers subscribe to topics and receive messages as they arrive. Adding a new consumer group doesn't require any change to the producer.

---

### `java.util.Observable` (deprecated)

Java had a built-in `Observable` class and `Observer` interface since JDK 1.0. They were deprecated in Java 9 and removed guidance in later JDKs because:
- `Observable` is a class, not an interface — your Subject must extend it, consuming your one inheritance slot
- `setChanged()` must be called manually before `notifyObservers()` — easy to forget
- The `update(Observable o, Object arg)` signature is untyped — forces casting

The modern approach is to define your own `Observer` interface (as we do here) or use `PropertyChangeSupport`, reactive streams (`Flow.Publisher` / `Flow.Subscriber` in Java 9+), or a library like RxJava.

---

## When to Use Observer

**Use it when:**
- One object's state change should trigger updates in other objects, and you don't know in advance how many objects need to be notified
- Objects should be able to subscribe and unsubscribe at runtime without modifying the subject
- You want the subject and its observers to be loosely coupled — the subject should not import observer concrete types
- You're building an event system, notification feed, or reactive data pipeline

**Don't use it when:**
- The set of observers is fixed and small — hard-coded calls are simpler than a dynamic list
- Observers need a guaranteed, fixed call order — list-based notification order depends on registration order, which is fragile
- Cascading updates are possible (observer A's `update()` changes state that triggers observer B, which triggers A again) — cycles are hard to debug
- The notification path is performance-critical and observers are many — each `notifyObservers()` is a linear scan; for millions of observers, consider a more efficient event system

---

## Observer vs Strategy vs Mediator

All three deal with communication between objects, but each solves a different problem:

| Dimension                    | Observer                                              | Strategy                                              | Mediator                                               |
|------------------------------|-------------------------------------------------------|-------------------------------------------------------|--------------------------------------------------------|
| **Relationship**             | One subject → many observers                          | One context → one strategy                            | Many objects ↔ one mediator                            |
| **Who initiates**            | Subject pushes on state change                        | Context delegates on demand                           | Colleagues send to mediator; mediator fans out         |
| **Coupling direction**       | Subject doesn't know observer types                   | Context doesn't know strategy type                    | Colleagues don't know each other; all know the mediator |
| **Runtime membership**       | Yes — observers register/unregister dynamically       | Yes — strategy can be swapped                         | Fixed — colleagues are wired at setup                  |
| **Goal**                     | Broadcast state changes to an open-ended audience     | Plug in an interchangeable algorithm                  | Centralise complex many-to-many communication          |
| **Our analogy**              | Newspaper: publisher → all subscribers                | GPS: driver picks routing algorithm                   | Air traffic control: planes talk to tower, not each other |
| **Our example**              | `WeatherStation` → `*Display`                         | `DataSorter` → `SortStrategy`                         | *(not yet in this project)*                            |

```java
// Observer — subject broadcasts; observers react
station.setMeasurements(22.5f, 65.0f, 1013.0f);  // triggers all registered displays

// Strategy — context delegates to one pluggable algorithm
sorter.setStrategy(new MergeSortStrategy());
sorter.sort(data);                                 // delegates to exactly one strategy

// Mediator — colleagues send to mediator; mediator decides who else to notify
chatRoom.send(alice, "Hello!");                    // ChatRoom decides which members receive it
```

The fastest question to distinguish them: **who decides who gets notified?**
- The subject pushes to everyone subscribed → **Observer**
- The context delegates to one chosen object → **Strategy**
- A central hub routes messages between colleagues → **Mediator**
