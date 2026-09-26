# Strategy Design Pattern

## What Is It?

The Strategy pattern **encapsulates an algorithm behind an interface** so that it can be selected and swapped at runtime — independently of the clients that use it.

> "Define a family of algorithms, encapsulate each one, and make them interchangeable. Strategy lets the algorithm vary independently from clients that use it."
> — Gang of Four, *Design Patterns*

Think of a GPS navigation app. The destination is fixed (what), but you can choose how to get there: fastest route, shortest route, avoiding tolls, cycling-friendly. Each routing algorithm is a different strategy. The app doesn't need to know how each algorithm works — it just calls `navigate()`, and the selected strategy handles the rest.

The pattern separates **what your code does** from **how it does it**. The "what" lives in the Context; the "how" lives in interchangeable Strategy objects.

---

## The Problem It Solves

A `DataSorter` that needs to support multiple sorting algorithms. The naive approach puts all algorithms in one class:

```java
public class DataSorter {
    public void sort(int[] data, String algorithm) {
        if (algorithm.equals("bubble")) {
            // bubble sort implementation — 15 lines
        } else if (algorithm.equals("quick")) {
            // quick sort implementation — 25 lines
        } else if (algorithm.equals("merge")) {
            // merge sort implementation — 30 lines
        }
        // Adding a new algorithm means editing this class
    }
}
```

**Problems with this:**
- Every new algorithm requires modifying `DataSorter` — violates the Open/Closed Principle
- `DataSorter` grows without bound; it becomes a god class of unrelated logic
- You can't test bubble sort in isolation — it's tangled inside the sorter
- You can't reuse the merge sort in another context (e.g., a file sorter)
- Swapping algorithms at runtime requires passing a string — fragile, typo-prone

**With Strategy**, each algorithm lives in its own class. `DataSorter` holds a reference to a `SortStrategy` and delegates to it. Adding a new algorithm means adding a new class — the sorter never changes.

---

## Roles

| Role                  | Responsibility                                                   | In our code                                           |
|-----------------------|------------------------------------------------------------------|-------------------------------------------------------|
| **Strategy**          | Interface defining the algorithm contract                       | `SortStrategy`                                        |
| **Concrete Strategy** | One implementation of the algorithm                             | `BubbleSortStrategy`, `QuickSortStrategy`, `MergeSortStrategy` |
| **Context**           | Holds a Strategy reference; delegates work to it               | `DataSorter`                                          |
| **Client**            | Selects which strategy to use and passes it to the Context      | `Main`                                                |

The Context doesn't need to know which concrete strategy it holds. It only knows the `SortStrategy` interface.

---

## Structure

```
Client
  └── DataSorter.sort(data)     [Context]
           │
           └── strategy.sort(data)    [SortStrategy interface]
                    │
           ┌────────┼────────┐
    BubbleSort   QuickSort   MergeSort
    Strategy     Strategy    Strategy
```

```
                     ┌──────────────────────────┐
                     │      «interface»          │
                     │      SortStrategy         │
                     │  +sort(int[] data)         │
                     └────────────┬──────────────┘
              implements          │           implements
     ┌─────────────────┐          │     ┌─────────────────────┐
     │ BubbleSort      │          │     │ QuickSort           │
     │ Strategy        │          │     │ Strategy            │
     │ +sort(int[])    │          │     │ +sort(int[])        │
     └─────────────────┘          │     └─────────────────────┘
                     ┌────────────┴──────────────┐
                     │ MergeSort                 │
                     │ Strategy                  │
                     │ +sort(int[])              │
                     └───────────────────────────┘

    ┌──────────────────────────────┐
    │         DataSorter           │
    │  -SortStrategy strategy       │
    │  +DataSorter(SortStrategy)    │
    │  +setStrategy(SortStrategy)   │──────▶ SortStrategy
    │  +sort(int[] data)            │
    └──────────────────────────────┘
```

Class diagram: [`docs/diagrams/strategy.png`](diagrams/strategy.png)

---

## Our Implementation — Full Walkthrough

### Strategy Interface — `SortStrategy.java`

```java
public interface SortStrategy {
    void sort(int[] data);
}
```

This is the **only contract the Context ever knows about**. Any class that implements this interface can be plugged in as a strategy. The interface is deliberately minimal — one method, one responsibility.

### Concrete Strategy 1 — `BubbleSortStrategy.java`

```java
public class BubbleSortStrategy implements SortStrategy {

    @Override
    public void sort(int[] data) {
        int n = data.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (data[j] > data[j + 1]) {
                    int tmp = data[j];
                    data[j] = data[j + 1];
                    data[j + 1] = tmp;
                }
            }
        }
    }
}
```

O(n²) nested-loop bubble sort — correct, simple, and entirely self-contained. `DataSorter` knows nothing about these loops. The algorithm is the strategy's private business.

### Concrete Strategy 2 — `QuickSortStrategy.java`

```java
public class QuickSortStrategy implements SortStrategy {

    @Override
    public void sort(int[] data) {
        Arrays.sort(data);  // JDK Dual-Pivot Quicksort
    }
}
```

This demonstrates an important point: **a strategy can wrap a library**. You don't have to implement the algorithm from scratch. `Arrays.sort()` is a well-tested O(n log n) implementation — wrapping it as a strategy gives clients the same interface for a much faster result. The Context doesn't know or care that the implementation delegates.

### Concrete Strategy 3 — `MergeSortStrategy.java`

```java
public class MergeSortStrategy implements SortStrategy {

    @Override
    public void sort(int[] data) {
        mergeSort(data, 0, data.length - 1);
    }

    private void mergeSort(int[] data, int left, int right) {
        if (left >= right) return;
        int mid = left + (right - left) / 2;
        mergeSort(data, left, mid);
        mergeSort(data, mid + 1, right);
        merge(data, left, mid, right);
    }

    private void merge(int[] data, int left, int mid, int right) {
        // copy subarrays, merge back in sorted order
    }
}
```

Recursive O(n log n) merge sort — stable sort with guaranteed worst-case performance. The helper methods `mergeSort()` and `merge()` are private — they're implementation details invisible even to the interface. The strategy owns its own complexity.

### Context — `DataSorter.java`

```java
public class DataSorter {

    private SortStrategy strategy;      // holds a reference, not a concrete type

    public DataSorter(SortStrategy strategy) {
        this.strategy = strategy;        // strategy chosen at construction time
    }

    public void setStrategy(SortStrategy strategy) {
        this.strategy = strategy;        // can be swapped at any point
    }

    public void sort(int[] data) {
        System.out.println("Sorting with: " + strategy.getClass().getSimpleName());
        strategy.sort(data);             // delegates — Context has no sort logic
    }
}
```

`DataSorter` is the Context. It:
- Stores the strategy as the **interface type** `SortStrategy` — never as `BubbleSortStrategy` or any concrete class
- Delegates `sort()` entirely to the strategy — it adds no sorting logic of its own
- Exposes `setStrategy()` for runtime swapping — the same `DataSorter` instance can change behaviour mid-run
- Prints the class name for visibility — in production this would be a logger

---

## How It Works — Call Trace

Tracing `sorter.sort(data1)` when strategy is `BubbleSortStrategy`:

```
Main
  └─ sorter.sort(data1)                    [DataSorter — Context]
       └─ prints: "Sorting with: BubbleSortStrategy"
       └─ strategy.sort(data1)             [dispatches via SortStrategy interface]
            └─ BubbleSortStrategy.sort()   [Concrete Strategy]
                 └─ nested-loop swap...
                 └─ data1 is now sorted in-place
```

The call enters `DataSorter.sort()` and immediately leaves again via the strategy. The Context is a thin dispatcher — all the work happens in the strategy.

---

## Swapping at Runtime

```java
DataSorter sorter = new DataSorter(new BubbleSortStrategy());

sorter.sort(data1);                         // uses BubbleSortStrategy

sorter.setStrategy(new QuickSortStrategy()); // swap — same sorter object
sorter.sort(data2);                         // now uses QuickSortStrategy

sorter.setStrategy(new MergeSortStrategy()); // swap again
sorter.sort(data3);                         // now uses MergeSortStrategy
```

The `DataSorter` instance never changes. Its behaviour changes because the strategy it delegates to changes. This is the defining capability of the pattern — the "how" is a hot-swappable plug.

In a real application, the strategy might be swapped based on:
- The size of the input (bubble sort for tiny arrays, merge sort for large ones)
- A configuration file entry (`sort.algorithm=quicksort`)
- A user's preference setting
- The current environment (faster algorithm in production, simpler one in debug mode)

---

## More Real-World Examples

### Payment Processing

An e-commerce checkout needs to support multiple payment methods. Each method has a completely different flow (credit card → charge API; PayPal → redirect + token; crypto → wallet address).

```java
public interface PaymentStrategy {
    void pay(double amount);
}

public class CreditCardStrategy implements PaymentStrategy {
    public void pay(double amount) { /* call Stripe API */ }
}

public class PayPalStrategy implements PaymentStrategy {
    public void pay(double amount) { /* redirect to PayPal */ }
}

public class ShoppingCart {
    private PaymentStrategy paymentStrategy;
    public void setPaymentStrategy(PaymentStrategy s) { this.paymentStrategy = s; }
    public void checkout(double total) { paymentStrategy.pay(total); }
}
```

The user selects a payment method; `setPaymentStrategy()` is called; `checkout()` runs. Adding Bitcoin support means adding one class — `ShoppingCart` doesn't change.

---

### File Compression

```java
public interface CompressionStrategy {
    byte[] compress(byte[] data);
}

public class GzipStrategy  implements CompressionStrategy { ... }
public class ZipStrategy   implements CompressionStrategy { ... }
public class LZ4Strategy   implements CompressionStrategy { ... }   // fast, less compression
public class ZstdStrategy  implements CompressionStrategy { ... }   // best ratio
```

The right compression algorithm depends on context: latency vs ratio vs compatibility. The caller picks a strategy; the `FileCompressor` context just calls `compress()`.

---

### Route Planning (the GPS analogy made concrete)

```java
public interface RouteStrategy {
    List<Step> findRoute(Location from, Location to);
}

public class FastestRouteStrategy    implements RouteStrategy { ... }
public class ShortestRouteStrategy   implements RouteStrategy { ... }
public class ScenicRouteStrategy     implements RouteStrategy { ... }
public class CyclingRouteStrategy    implements RouteStrategy { ... }
```

The user switches the route type in the UI; the navigator's strategy is swapped; `findRoute()` recalculates with the new algorithm.

---

### Tax Calculation

Different countries have completely different tax rules. Wrapping each country's rules in a strategy avoids an ever-growing conditional inside the invoice system.

```java
public interface TaxStrategy {
    double calculateTax(double amount);
}

public class USTaxStrategy   implements TaxStrategy { ... }   // federal + state
public class EUTaxStrategy   implements TaxStrategy { ... }   // VAT
public class UKTaxStrategy   implements TaxStrategy { ... }   // UK post-Brexit VAT
```

The invoice generator selects the strategy based on the customer's country — the tax rules are isolated per class and independently testable.

---

### Authentication

```java
public interface AuthStrategy {
    boolean authenticate(String credentials);
}

public class PasswordAuthStrategy   implements AuthStrategy { ... }
public class OAuthStrategy          implements AuthStrategy { ... }
public class SamlSsoStrategy        implements AuthStrategy { ... }
public class MfaStrategy            implements AuthStrategy { ... }
```

The login service holds an `AuthStrategy`. The active strategy is determined by the tenant's configuration — without any `if/else` in the login flow.

---

## When to Use Strategy

**Use it when:**
- You have a family of related algorithms and you want to be able to switch between them
- An algorithm has variants based on configuration, input, or context — and you want to avoid a big conditional
- You have a class that does one thing multiple ways and those ways are growing (adding a new way currently means editing the class)
- You want to test each algorithm independently of the rest of the system
- You need runtime behaviour switching without changing the client

**Don't use it when:**
- You only have one algorithm and no expectation of adding more — an interface for one implementation adds indirection for no benefit
- The variations are trivial (e.g., a single boolean flag) — a strategy is heavier than `if (reverse) ...`
- The strategies need to share significant state with each other — they're supposed to be interchangeable; if one strategy needs data that another doesn't, the design is leaking

---

## Strategy vs Template Method vs State

All three address variations in behaviour. The difference is the mechanism:

| Dimension                   | Strategy                                    | Template Method                               | State                                           |
|-----------------------------|---------------------------------------------|-----------------------------------------------|-------------------------------------------------|
| **How behaviour varies**    | Delegation to a pluggable object            | Inheritance — subclass overrides steps        | Object replaces itself with a different state   |
| **Mechanism**               | Composition                                 | Inheritance                                   | Composition + self-mutation                     |
| **Who decides the variant** | Client — passes a strategy object           | Subclass — at compile time                    | Context — transitions based on internal events  |
| **Runtime switching**       | Yes — any time via `setStrategy()`          | No — fixed at construction                    | Yes — but driven by the object, not the client  |
| **Our analogy**             | GPS navigation: user picks the route type   | Recipe: steps are fixed; add your ingredients | Traffic light: state machine drives transitions |
| **Typical use**             | Algorithms, policies, payment methods       | Frameworks, lifecycle hooks, report generators | UI components, vending machines, order workflows |

```java
// Strategy — CLIENT controls the variant; context is neutral
sorter.setStrategy(new MergeSortStrategy());   // explicit selection
sorter.sort(data);

// Template Method — SUBCLASS controls the variant at compile time
class PdfReport extends ReportGenerator {
    @Override
    protected String formatData(Data d) { return toPdf(d); }  // override one step
}

// State — the OBJECT controls its own transitions
order.confirm();   // Order internally transitions from PENDING to CONFIRMED
order.ship();      // Order internally transitions from CONFIRMED to SHIPPED
```

The fastest question to distinguish them: **who makes the decision to change behaviour?**
- Client code → **Strategy**
- Subclass (at compile time) → **Template Method**
- The object itself (based on internal events) → **State**
