# State Design Pattern

## What Is It?

The State pattern **allows an object to alter its behaviour when its internal state changes**. The object will appear to change its class.

> "Allow an object to alter its behavior when its internal state changes. The object will appear to change its class."
> — Gang of Four, *Design Patterns*

Think of a traffic light. It shows red, green, or amber — and each colour means a completely different set of rules for drivers. The light doesn't have three separate objects; it has one object (`TrafficLight`) that delegates to whichever state it's currently in (`RedState`, `GreenState`, `AmberState`). The transitions happen automatically — red → green → amber → red.

The pattern separates **what the object does in each state** from the object itself. Each state lives in its own class, with the same interface but different logic. The context (the traffic light) just delegates every operation to its current state.

---

## The Problem It Solves

A `VendingMachine` that can be idle, has received a coin, is dispensing, or is out of stock. The naive approach puts all the state logic inside the machine as a giant conditional:

```java
public class VendingMachine {

    private enum MachineState { IDLE, HAS_COIN, DISPENSING, OUT_OF_STOCK }
    private MachineState state = MachineState.IDLE;

    public void insertCoin() {
        if (state == MachineState.IDLE) {
            // ...
        } else if (state == MachineState.HAS_COIN) {
            // ...
        } else if (state == MachineState.DISPENSING) {
            // ...
        } else if (state == MachineState.OUT_OF_STOCK) {
            // ...
        }
    }

    // selectProduct(), dispense(), refund() — each with the same 4-branch switch
}
```

**Problems with this:**
- Adding a new state (e.g., `MAINTENANCE_MODE`) means editing every method — 4+ methods each gaining another branch
- Each method is a tangled mix of all states' logic — hard to read, hard to test
- The `MachineState` enum keeps growing; transitions are scattered across all methods
- You can't test the idle behaviour in isolation — it's buried inside the method

**With State**, each state is its own class. `IdleState.insertCoin()` contains exactly what happens when you insert a coin while idle. Adding a new state means adding a new class — the machine never changes.

---

## Roles

| Role                   | Responsibility                                                              | In our code                                                  |
|------------------------|-----------------------------------------------------------------------------|--------------------------------------------------------------|
| **State**              | Interface defining all operations every state must handle                   | `State`                                                      |
| **Concrete State**     | Implements operations for one specific state; triggers transitions          | `IdleState`, `HasCoinState`, `DispensingState`, `OutOfStockState` |
| **Context**            | Holds the current state; delegates every operation to it                    | `VendingMachine`                                             |
| **Client**             | Drives the context; initiates operations but doesn't know about states      | `Main`                                                       |

The Context delegates operations to the current State. The State objects hold a reference back to the Context so they can trigger transitions (`machine.setState(...)`).

---

## Structure

```
Main
  └── machine.insertCoin()           [VendingMachine — Context]
           └── currentState.insertCoin()
                    │
           ┌────────┴──────────────────────────┐
     [IDLE]           [HAS_COIN]          [DISPENSING]        [EMPTY]
  IdleState        HasCoinState        DispensingState    OutOfStockState
  transitions to   transitions to      transitions to
  HasCoinState     DispensingState     IdleState or
                   (then calls         OutOfStockState
                   machine.dispense())
```

```
          ┌────────────────────────────┐
          │       «interface»          │
          │           State            │
          │  +insertCoin()             │
          │  +selectProduct()          │
          │  +dispense()               │
          │  +refund()                 │
          └────────────┬───────────────┘
                       │ implements
          ┌────────────┴───────────────┐
          │      VendingMachine        │
          │  -State currentState       │
          │  -int itemCount            │───────▶ State (currentState)
          │  +insertCoin()             │
          │  +selectProduct()          │
          │  +dispense()               │
          │  +refund()                 │
          │  +setState(State s)        │
          └────────────────────────────┘
          ▲ (states hold a reference back to VendingMachine for transitions)
```

Class diagram: [`docs/diagrams/state.png`](diagrams/state.png)

---

## Our Implementation — Full Walkthrough

### State Interface — `State.java`

```java
public interface State {
    void insertCoin();
    void selectProduct();
    void dispense();
    void refund();
}
```

Four operations — every state must implement all four. This is the complete contract between the Context and its states. The Context calls these methods; it never calls state-specific methods.

### Context — `VendingMachine.java`

```java
public class VendingMachine implements State {

    private final State idleState;
    private final State hasCoinState;
    private final State dispensingState;
    private final State outOfStockState;

    private State currentState;
    private int itemCount;

    public VendingMachine(int itemCount) {
        this.itemCount   = itemCount;
        idleState        = new IdleState(this);
        hasCoinState     = new HasCoinState(this);
        dispensingState  = new DispensingState(this);
        outOfStockState  = new OutOfStockState(this);
        currentState     = (itemCount > 0) ? idleState : outOfStockState;
    }

    public void setState(State state) { this.currentState = state; }

    @Override public void insertCoin()    { currentState.insertCoin(); }
    @Override public void selectProduct() { currentState.selectProduct(); }
    @Override public void dispense()      { currentState.dispense(); }
    @Override public void refund()        { currentState.refund(); }
}
```

The machine creates all four concrete state objects at startup and holds them as fields. States are reused — there is only ever one `IdleState` instance per machine. The machine exposes `setState()` so state objects can drive transitions; it also exposes `getItemCount()` and `decrementItems()` so `DispensingState` can update the count.

All four public operations are pure delegates — the machine has no `if/else`, no switch, no state logic of its own.

### Concrete State — `IdleState.java`

```java
public class IdleState implements State {

    private final VendingMachine machine;

    public IdleState(VendingMachine machine) { this.machine = machine; }

    @Override
    public void insertCoin() {
        System.out.println("[IDLE   ] Coin inserted");
        machine.setState(machine.getHasCoinState());   // trigger transition
    }

    @Override public void selectProduct() { System.out.println("[IDLE   ] Insert a coin first"); }
    @Override public void dispense()      { System.out.println("[IDLE   ] No coin inserted"); }
    @Override public void refund()        { System.out.println("[IDLE   ] No coin to refund"); }
}
```

When idle, only `insertCoin()` is a valid action — it prints a message and transitions to `HasCoinState`. The other three operations print "invalid action" messages and take no state change. This is the idle state's entire responsibility, isolated in 15 lines.

### Concrete State — `HasCoinState.java`

```java
public class HasCoinState implements State {

    @Override
    public void selectProduct() {
        System.out.println("[COIN   ] Product selected");
        machine.setState(machine.getDispensingState());
        machine.dispense();    // immediately trigger dispense in the new state
    }

    @Override
    public void refund() {
        System.out.println("[COIN   ] Refunding coin");
        machine.setState(machine.getIdleState());
    }
    // ...
}
```

Two valid transitions from `HasCoinState`: select a product (→ dispense) or ask for a refund (→ idle). After transitioning to `DispensingState`, it calls `machine.dispense()` directly — the Context is now in `DispensingState`, so the call is handled by `DispensingState.dispense()`.

### Concrete State — `DispensingState.java`

```java
public class DispensingState implements State {

    @Override
    public void dispense() {
        machine.decrementItems();
        System.out.println("[DISPENSE] Dispensing your item!");
        if (machine.getItemCount() > 0) {
            machine.setState(machine.getIdleState());
        } else {
            machine.setState(machine.getOutOfStockState());
        }
        machine.printStatus();
    }
}
```

`DispensingState` is the only state that mutates the item count. After dispensing, it checks the count and transitions to either `IdleState` (items remain) or `OutOfStockState` (count is now 0). The transition decision is local to this state — no other class needs to know this rule.

### Concrete State — `OutOfStockState.java`

```java
public class OutOfStockState implements State {

    @Override
    public void insertCoin() {
        System.out.println("[EMPTY  ] Machine is out of stock, refunding coin");
    }

    @Override public void selectProduct() { System.out.println("[EMPTY  ] Machine is out of stock"); }
    @Override public void dispense()      { System.out.println("[EMPTY  ] No items to dispense"); }
    @Override public void refund()        { System.out.println("[EMPTY  ] No coin inserted"); }
}
```

Once in `OutOfStockState`, no operation causes a transition — the machine stays empty until items are restocked (which would require a `restock()` method and a transition back to `IdleState`, adding one new class and one new method on the interface).

---

## How It Works — Call Trace

Tracing the normal purchase flow (`insertCoin` → `selectProduct`):

```
Main
  └─ machine.insertCoin()                       [VendingMachine — delegates]
       └─ currentState.insertCoin()             [currentState == IdleState]
            └─ IdleState.insertCoin()
                 ├─ prints: [IDLE   ] Coin inserted
                 └─ machine.setState(hasCoinState)   → currentState = HasCoinState

  └─ machine.selectProduct()                    [VendingMachine — delegates]
       └─ currentState.selectProduct()          [currentState == HasCoinState]
            └─ HasCoinState.selectProduct()
                 ├─ prints: [COIN   ] Product selected
                 ├─ machine.setState(dispensingState) → currentState = DispensingState
                 └─ machine.dispense()          [Context delegates to new currentState]
                      └─ DispensingState.dispense()
                           ├─ machine.decrementItems()  → itemCount = 1
                           ├─ prints: [DISPENSE] Dispensing your item!
                           ├─ itemCount > 0 → machine.setState(idleState)
                           └─ machine.printStatus() → prints: [MACHINE] Items remaining: 1
```

Two calls to the Context trigger a chain of five state transitions, each state delegating back to the machine for the next step.

---

## State Transitions

All transitions are triggered by the state objects — never by client code directly:

```
                         insertCoin()
              ┌──────────────────────────────────────┐
              │                                      ▼
          ┌───────┐   insertCoin()   ┌──────────┐
          │       │────────────────▶│ HasCoin  │
          │ Idle  │                 │  State   │
          │       │◀────────────────│          │
          └───────┘    refund()     └────┬─────┘
              ▲                          │ selectProduct()
              │                          ▼
              │                    ┌──────────┐   itemCount > 0
              │                    │Dispensing│───────────────────▶ [back to Idle]
              │                    │  State   │
              │                    └─────┬────┘
              │                          │ itemCount == 0
              │                          ▼
              │                    ┌──────────┐
              └────────────────────│   Out    │  (no exit — machine stays empty
                                   │OfStock   │   until restocked)
                                   └──────────┘
```

Key insight: **the states drive the machine, not the machine driving the states**. `IdleState` says "transition to HasCoin when a coin arrives"; `DispensingState` says "transition to OutOfStock when the last item is dispensed." The Context is a passive shell that delegates every decision.

---

## More Real-World Examples

### TCP Connection States

A TCP socket has states: `CLOSED`, `LISTEN`, `SYN_SENT`, `ESTABLISHED`, `CLOSE_WAIT`, etc. Each state handles the same operations (open, close, acknowledge) differently, and transitions happen automatically based on events. The Berkeley socket API is a real-world State pattern implementation.

---

### Order Workflow

An e-commerce `Order` cycles through states: `PendingState` → `ConfirmedState` → `ShippedState` → `DeliveredState` → `CancelledState`.

```java
order.confirm();   // PendingState → ConfirmedState
order.ship();      // ConfirmedState → ShippedState; CancelledState prints "already shipped"
order.cancel();    // ConfirmedState → CancelledState; DeliveredState prints "cannot cancel"
```

Each state class handles `confirm()`, `ship()`, `cancel()`, and `deliver()` appropriately for that stage. Adding a `ReturnRequestedState` means adding one class — no existing state class changes.

---

### UI Component States

A `SubmitButton` has states: `IdleState` (clickable), `LoadingState` (spinner, ignores clicks), `DisabledState` (greyed out), `ErrorState` (shows retry).

```java
button.click();    // IdleState → LoadingState → (success) IdleState or (fail) ErrorState
```

Each state renders itself differently and handles click events differently. The button has no if/else about rendering — it delegates to the current state.

---

### `java.util.Iterator`

An `Iterator` is implicitly a state machine: it transitions from "has next" to "exhausted" as elements are consumed. `hasNext()` and `next()` behave differently depending on which state the iterator is in (implicitly — Java doesn't use named state classes, but the concept is identical).

---

### React Component Lifecycle

A React class component has states: `mounting`, `mounted`, `updating`, `unmounted`. Each lifecycle method (`componentDidMount`, `componentDidUpdate`, `componentWillUnmount`) only fires in certain states. The React internals are an explicit state machine driving which hooks are called.

---

## When to Use State

**Use it when:**
- An object's behaviour depends on its state, and the state must change at runtime
- Operations have large multi-branch conditionals (`if/else` or `switch`) that test the same state variable across multiple methods — each branch should be a state class
- You want to add new states without modifying existing code (Open/Closed Principle)
- Transitions between states are complex enough to need their own logic — State makes transitions explicit and localised

**Don't use it when:**
- The object has only two or three states with simple behaviour — a boolean flag and a couple of `if` statements are cleaner than four classes
- States are never re-entered and the flow is strictly linear — a simple sequence of method calls is more readable
- The transitions are always externally driven by client code — if the client manages all transitions explicitly, a plain enum + switch is fine and easier to follow

---

## State vs Strategy vs Observer

Three behavioural patterns that all involve delegation to another object, but for different reasons:

| Dimension                    | State                                                 | Strategy                                              | Observer                                              |
|------------------------------|-------------------------------------------------------|-------------------------------------------------------|-------------------------------------------------------|
| **What it encapsulates**     | State-specific behaviour + transitions                | An interchangeable algorithm                          | A reaction to a state change                          |
| **Who changes the delegate** | The delegate (state) changes itself                   | Client code changes the strategy                      | Subject registers/deregisters observers               |
| **How many delegates**       | One at a time (the current state)                     | One at a time (the current strategy)                  | Many simultaneously (all registered observers)        |
| **Purpose**                  | Model a state machine; eliminate state-based conditionals | Plug in different algorithms at runtime            | Broadcast a change to an open-ended audience          |
| **Context knows delegates?** | Yes — holds all states as fields; creates them        | No — client passes the strategy in                    | Yes — holds a list, but as the interface type         |
| **Transitions**              | Driven by state objects themselves                    | No concept of transitions                             | Observers join/leave; no transitions                  |
| **Our analogy**              | Traffic light cycling through states automatically    | GPS: user picks routing algorithm                     | Newspaper: publisher sends to all subscribers         |
| **Our example**              | `VendingMachine` cycling through four states          | `DataSorter` + interchangeable sort algorithms        | `WeatherStation` broadcasting to three displays       |

```java
// State — the object transitions itself based on its own logic
machine.insertCoin();     // IdleState → HasCoinState (state drives the transition)
machine.selectProduct();  // HasCoinState → DispensingState → IdleState

// Strategy — CLIENT swaps the algorithm; context is neutral
sorter.setStrategy(new MergeSortStrategy());   // explicit external selection
sorter.sort(data);

// Observer — subject broadcasts; observers react; subject doesn't know who
station.setMeasurements(22.5f, 65.0f, 1013.0f);  // all 3 displays updated
```

The fastest question to distinguish them: **who changes the behaviour, and why?**
- The object changes its own behaviour based on internal events → **State**
- Client code swaps in a different algorithm → **Strategy**
- A change in one object needs to propagate to many others → **Observer**
