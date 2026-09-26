# Chain of Responsibility Design Pattern

## What Is It?

The Chain of Responsibility pattern **passes a request along a chain of handlers until one of them handles it**. Each handler decides whether to process the request or pass it to the next handler in the chain.

> "Avoid coupling the sender of a request to its receiver by giving more than one object a chance to handle the request. Chain the receiving objects and pass the request along the chain until an object handles it."
> — Gang of Four, *Design Patterns*

Think of a security checkpoint at an airport. A ticket inspector looks at your boarding pass — if something is wrong with the ticket, they stop you. Otherwise they pass you to the ID checker. The ID checker looks at your passport — they either stop you or pass you to the baggage scanner. Each checkpoint is independent: it only knows what it checks and who the next checkpoint is. Adding a new security step means inserting a new handler — the others don't change.

Or think of a relay race: the baton moves from runner to runner. The right runner picks it up and carries it to the end. The baton never knows which runner will take it — it just gets passed along.

---

## The Problem It Solves

A support system that routes tickets to the right team based on severity. The naive approach puts all the routing logic in one dispatcher:

```java
public class SupportRouter {

    public void route(SupportRequest request) {
        if (request.getLevel() == SupportLevel.LOW) {
            frontlineSupport.handle(request);
        } else if (request.getLevel() == SupportLevel.MEDIUM) {
            technicalSupport.handle(request);
        } else if (request.getLevel() == SupportLevel.HIGH) {
            seniorEngineer.handle(request);
        } else if (request.getLevel() == SupportLevel.CRITICAL) {
            managementEscalation.handle(request);
        } else {
            System.out.println("Unknown severity — dropped");
        }
    }
}
```

**Problems with this:**
- Adding a new severity level (e.g., `URGENT`) means editing `SupportRouter` — it grows forever
- The router is tightly coupled to every handler; it must know all of them
- You can't reorder the chain without editing the router
- You can't reuse the routing logic for a different subset of handlers
- Testing one handler in isolation is hard — it's buried inside the dispatcher

**With Chain of Responsibility**, each handler only knows about the next handler. The dispatcher (`SupportRouter`) disappears entirely — the chain is the router. Adding a new handler means adding a new class and wiring it into the chain; nothing else changes.

---

## Roles

| Role                   | Responsibility                                                              | In our code                                                  |
|------------------------|-----------------------------------------------------------------------------|--------------------------------------------------------------|
| **Handler**            | Defines the interface for handling requests; holds reference to next handler | `SupportHandler` (abstract class)                           |
| **Concrete Handler**   | Handles requests it can; passes the rest down the chain                     | `FrontlineSupport`, `TechnicalSupport`, `SeniorEngineer`, `ManagementEscalation` |
| **Request**            | Carries the data that handlers inspect to decide whether to handle          | `SupportRequest`                                             |
| **Client**             | Builds the chain and sends requests to the first handler                    | `Main`                                                       |

The Client only talks to the first handler in the chain. Each concrete handler either handles the request and stops, or calls `passToNext()` to continue. Handlers don't know which specific handler comes next — only that a next handler may exist.

---

## Structure

```
Main
  └── frontline.handle(request)        [FrontlineSupport]
           │
           ├─ level == LOW  → handles here, chain stops
           └─ level != LOW  → passToNext(request)
                    └── technical.handle(request)   [TechnicalSupport]
                             │
                             ├─ level == MEDIUM → handles here, chain stops
                             └─ level != MEDIUM → passToNext(request)
                                      └── senior.handle(request)   [SeniorEngineer]
                                               │
                                               ├─ level == HIGH → handles here, chain stops
                                               └─ level != HIGH → passToNext(request)
                                                        └── management.handle(request)
```

```
          ┌──────────────────────────────────┐
          │         «abstract»               │
          │         SupportHandler           │
          │  #SupportHandler next            │──────▶ SupportHandler (next)
          │  +setNext(SupportHandler) : SH   │
          │  +handle(SupportRequest)*        │
          │  #passToNext(SupportRequest)     │
          └────────────────┬─────────────────┘
                           │ extends
          ┌────────────────┴─────────────────┐
          │                                  │
   FrontlineSupport    TechnicalSupport    SeniorEngineer    ManagementEscalation
   (handles LOW)       (handles MEDIUM)    (handles HIGH)    (handles CRITICAL)
```

Class diagram: [`docs/diagrams/chain-of-responsibility.png`](diagrams/chain-of-responsibility.png)

---

## Our Implementation — Full Walkthrough

### Abstract Handler — `SupportHandler.java`

```java
public abstract class SupportHandler {

    protected SupportHandler next;

    public SupportHandler setNext(SupportHandler next) {
        this.next = next;
        return next;    // returning next enables fluent chaining
    }

    public abstract void handle(SupportRequest request);

    protected void passToNext(SupportRequest request) {
        if (next != null) {
            next.handle(request);
        } else {
            System.out.printf("[CHAIN  ] No handler could process: \"%s\" (level: %s)%n",
                    request.getDescription(), request.getLevel());
        }
    }
}
```

Three responsibilities: store the next handler, define the abstract `handle()` contract, and implement `passToNext()` as the shared fallback. The `setNext()` method returns `next` — not `this` — so calls can be chained: `a.setNext(b).setNext(c).setNext(d)`. Each call returns the newly set handler, which becomes the receiver of the next `setNext()` call.

### Request Object — `SupportRequest.java`

```java
public class SupportRequest {
    private final String description;
    private final SupportLevel level;

    public SupportRequest(String description, SupportLevel level) { ... }
    public String getDescription() { return description; }
    public SupportLevel getLevel() { return level; }
}
```

The request is a simple immutable value object. It carries everything handlers need to make their decision: a description (for logging) and a severity level (for routing). Handlers read the request but never modify it — the same object travels the entire chain unchanged.

### Concrete Handler — `FrontlineSupport.java`

```java
public class FrontlineSupport extends SupportHandler {

    @Override
    public void handle(SupportRequest request) {
        if (request.getLevel() == SupportLevel.LOW) {
            System.out.printf("[FRONT  ] Handling LOW request: \"%s\"%n", request.getDescription());
        } else {
            System.out.printf("[FRONT  ] Cannot handle %s — escalating%n", request.getLevel());
            passToNext(request);
        }
    }
}
```

Every concrete handler has the same structure: one `if` to check its level, one branch to handle, one branch to pass. This is the entire logic for `FrontlineSupport` — isolated in 10 lines. It has no knowledge of `TechnicalSupport` or any other handler. It only knows that a next handler may exist — and `passToNext()` takes care of that case.

### Concrete Handlers — `TechnicalSupport.java`, `SeniorEngineer.java`, `ManagementEscalation.java`

All three follow exactly the same structure as `FrontlineSupport` — they differ only in which `SupportLevel` they handle and their print prefix:

| Class                  | Handles    | Prefix    |
|------------------------|------------|-----------|
| `FrontlineSupport`     | `LOW`      | `[FRONT  ]` |
| `TechnicalSupport`     | `MEDIUM`   | `[TECH   ]` |
| `SeniorEngineer`       | `HIGH`     | `[SENIOR ]` |
| `ManagementEscalation` | `CRITICAL` | `[MGMT   ]` |

---

## How It Works — Call Trace

Tracing a `CRITICAL` request through the full chain:

```
Main
  └─ frontline.handle(CRITICAL "Complete system outage")
       └─ FrontlineSupport.handle()
            ├─ level != LOW
            ├─ prints: [FRONT  ] Cannot handle CRITICAL — escalating
            └─ passToNext(request)
                 └─ technical.handle(CRITICAL "Complete system outage")
                      └─ TechnicalSupport.handle()
                           ├─ level != MEDIUM
                           ├─ prints: [TECH   ] Cannot handle CRITICAL — escalating
                           └─ passToNext(request)
                                └─ senior.handle(CRITICAL "Complete system outage")
                                     └─ SeniorEngineer.handle()
                                          ├─ level != HIGH
                                          ├─ prints: [SENIOR ] Cannot handle CRITICAL — escalating
                                          └─ passToNext(request)
                                               └─ management.handle(CRITICAL "Complete system outage")
                                                    └─ ManagementEscalation.handle()
                                                         ├─ level == CRITICAL
                                                         └─ prints: [MGMT   ] Handling CRITICAL request: "Complete system outage"
```

The request object is created once by the client and passed unchanged through the entire chain. Each handler reads `request.getLevel()`, makes one decision, and either stops or delegates. The chain terminates the moment a handler matches.

---

## Chain Configuration

### Fluent `setNext()` chaining

`setNext()` returns the handler it was just given — not `this`. This enables reading the chain left-to-right in one statement:

```java
frontline.setNext(technical).setNext(senior).setNext(management);
//         sets technical    sets senior      sets management
//         as frontline's    as technical's   as senior's
//         next              next             next
```

This is equivalent to:

```java
frontline.setNext(technical);
technical.setNext(senior);
senior.setNext(management);
```

### Order matters

The chain processes from left to right. A `LOW` request is caught by the first handler — it never reaches `TechnicalSupport`. If you reverse the chain (`management → senior → technical → frontline`), Management would first try to handle every `LOW` request, find it can't, and escalate it all the way to Frontline.

For performance-critical chains, put the most frequent case first. For security or access-control chains, put the most restrictive check first.

### Partial chains and the fallback

Scenario 6 in `Main` builds a chain with no `ManagementEscalation` handler:

```java
partialFrontline.setNext(partialTechnical).setNext(partialSenior);
partialFrontline.handle(new SupportRequest("Alien invasion", SupportLevel.CRITICAL));
```

`SeniorEngineer` passes the request to `passToNext()`. Since `next == null`, the base class prints:

```
[CHAIN  ] No handler could process: "Alien invasion" (level: CRITICAL)
```

This fallback is defined once in `SupportHandler.passToNext()` — no concrete handler needs to handle the "end of chain" case itself.

### Reusing the same chain

Scenario 5 sends a second `MEDIUM` request through the same chain after the CRITICAL transaction in Scenario 4. The chain state is unchanged — there's no mutable state in any handler (the `next` field is set once at startup and never changes). The chain handles it identically to Scenario 2.

---

## More Real-World Examples

### Java Servlet Filters (`javax.servlet.Filter`)

Every HTTP request passes through a `FilterChain`. Each `Filter` decides whether to allow the request through or short-circuit:

```java
public class AuthFilter implements Filter {
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        if (!isAuthenticated(req)) {
            res.sendError(401);
            return;    // short-circuit — request goes no further
        }
        chain.doFilter(req, res);   // pass to next filter
    }
}
```

Filters form an explicit chain configured in `web.xml` or via annotations. Adding authentication, logging, rate-limiting, or compression means adding a filter — the existing filters don't change.

---

### Spring Security `FilterChain`

Spring Security is built entirely on a chain of `SecurityFilter` instances. Each filter handles one concern: `UsernamePasswordAuthenticationFilter`, `BearerTokenAuthenticationFilter`, `CsrfFilter`, `ExceptionTranslationFilter`, etc. A request that fails authentication is short-circuited early; one that passes all checks reaches the actual controller.

```java
// Spring internally builds something like:
corsFilter
  .setNext(csrfFilter)
  .setNext(authenticationFilter)
  .setNext(authorizationFilter)
  .setNext(exceptionTranslationFilter);
```

---

### Java `Logger` — log level propagation

Java's `java.util.logging.Logger` uses a chain through parent loggers. When a `Logger` receives a log record, it checks its own handlers; if `useParentHandlers == true`, it also propagates to its parent logger. The propagation continues up the hierarchy until it reaches the root logger.

```java
Logger child  = Logger.getLogger("com.myapp.service");
Logger parent = Logger.getLogger("com.myapp");
Logger root   = Logger.getLogger("");   // root

// child.log(record) → child handlers → parent handlers → root handlers
```

Each logger in the chain is a handler; the chain runs until the root is reached or `useParentHandlers` stops propagation.

---

### Express / Django Middleware

Both frameworks use a middleware chain for HTTP request handling. Each middleware function calls `next()` to pass control to the subsequent middleware:

```javascript
// Express
app.use((req, res, next) => {
    console.log(`[LOG] ${req.method} ${req.url}`);
    next();   // pass to next middleware
});

app.use((req, res, next) => {
    if (!req.headers.authorization) return res.status(401).send('Unauthorized');
    next();
});
```

---

### ATM Cash Dispensing

An ATM dispenses cash by chaining denomination handlers: `FiftyHandler` → `TwentyHandler` → `TenHandler` → `FiveHandler`. Each handler dispenses as many of its denomination as possible from the requested amount, then passes the remainder to the next handler.

```
Request: £85
FiftyHandler  → dispenses £50, passes £35 down
TwentyHandler → dispenses £20, passes £15 down
TenHandler    → dispenses £10, passes £5 down
FiveHandler   → dispenses £5, done
```

---

## When to Use Chain of Responsibility

**Use it when:**
- More than one object may handle a request, and you don't know which one at compile time
- You want to issue a request without specifying the handler explicitly
- The set of handlers or their order should be configurable at runtime
- You want to add new handlers without modifying existing ones (Open/Closed Principle)
- You have a growing `if/else` or `switch` dispatcher that selects a handler — each branch should be a handler class

**Don't use it when:**
- Exactly one handler always handles the request — just call it directly
- Every handler in the chain must handle the request (that's a different pattern — consider Decorator or Observer)
- The chain is trivially short (two handlers, fixed at compile time) — a simple `if/else` is more readable
- Tracing which handler handled a request is important for debugging — a chain can make this harder without explicit logging

---

## Chain of Responsibility vs Strategy vs Decorator

Three patterns that pass work between objects, but for different reasons:

| Dimension                    | Chain of Responsibility                              | Strategy                                              | Decorator                                             |
|------------------------------|------------------------------------------------------|-------------------------------------------------------|-------------------------------------------------------|
| **What it encapsulates**     | Who handles a request; pass-or-stop logic            | Which algorithm to apply                              | What extra behaviour to layer on an object            |
| **How many handle it**       | At most one (first to match; chain stops there)      | Exactly one (the selected strategy)                   | All of them (every decorator wraps and delegates)     |
| **Who builds the chain?**    | Client wires `setNext()` at startup                  | Client provides the strategy object                   | Client stacks decorators on construction              |
| **Direction of flow**        | Linear, one-way — request travels until handled      | Single delegation — context calls one strategy        | Wrapping — inner object called by each wrapper layer  |
| **Does it modify the input?**| No — request passes unchanged                        | No — context provides input, strategy transforms it   | Often yes — each decorator may add/transform output   |
| **Our analogy**              | Airport security checkpoints                         | GPS routing algorithm selection                       | Layered Java I/O streams                              |
| **Our example**              | Support tickets escalating to the right team         | `DataSorter` + interchangeable sort algorithms        | `RetryDecorator` wrapping `FormattingDecorator`       |

```java
// Chain of Responsibility — first matching handler takes it; the rest don't run
frontline.handle(new SupportRequest("crash", SupportLevel.MEDIUM));
// [FRONT] cannot handle → [TECH] handles it — chain stops here

// Strategy — ONE algorithm runs; client selects which one
sorter.setStrategy(new QuickSortStrategy());
sorter.sort(data);  // only QuickSort runs

// Decorator — ALL wrappers run; every layer adds behaviour
Notification n = new RetryDecorator(new FormattingDecorator(new SMS()));
n.send(message);  // FormattingDecorator formats → RetryDecorator adds retries → SMS sends
```

The fastest question to distinguish them: **how many handlers process the request?**
- At most one (first match wins) → **Chain of Responsibility**
- Exactly one (explicitly chosen) → **Strategy**
- All of them (layered wrapping) → **Decorator**
