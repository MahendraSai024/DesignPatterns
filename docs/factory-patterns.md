# Factory Patterns

Factory patterns are **creational design patterns** — they deal with object creation. The core idea is to move the `new` keyword out of the code that uses an object and into a dedicated place, so the caller never depends on a concrete class.

There are three variants, each progressively more flexible:

| Pattern | Who decides the type? | Extensible without changing existing code? |
|---|---|---|
| Simple Factory | One static method with `if/else` | No |
| Factory Method | Subclass overrides a method | Yes |
| Abstract Factory | Family of related factories | Yes |

---

## 1. Simple Factory

### What it is

A single class with a static method that takes a parameter and returns the right concrete object. It is **not** an official GoF pattern — it is a programming idiom.

### Structure

```
PizzaShop  ──uses──▶  PizzaFactory.createPizza(type)
                            │
                    ┌───────┴────────┐
                    ▼                ▼
               CheesePizza      VeggiePizza
                    └────────────────┘
                         implements
                           Pizza
```

### Code (from this project)

**`Pizza.java`** — the product interface
```java
public interface Pizza {
    void prepare();
    void cook();
    void dress();
}
```

**`CheesePizza.java`** — a concrete product
```java
public class CheesePizza implements Pizza {
    public void prepare() { System.out.println("Preparing Cheese Pizza base"); }
    public void cook()    { System.out.println("Cooking Cheese Pizza"); }
    public void dress()   { System.out.println("Dressing Cheese Pizza"); }
}
```

**`VeggiePizza.java`** — another concrete product
```java
public class VeggiePizza implements Pizza {
    public void prepare() { System.out.println("Preparing Veggie Pizza base"); }
    public void cook()    { System.out.println("Cooking Veggie Pizza"); }
    public void dress()   { System.out.println("Dressing Veggie Pizza"); }
}
```

**`PizzaFactory.java`** — the factory (centralised creation logic)
```java
public class PizzaFactory {
    public static Pizza createPizza(String type) {
        if ("Cheese".equalsIgnoreCase(type)) return new CheesePizza();
        if ("Veggie".equalsIgnoreCase(type))  return new VeggiePizza();
        throw new IllegalArgumentException("Unknown pizza type: " + type);
    }
}
```

**`PizzaShop.java`** — the client (never calls `new Pizza...`)
```java
public class PizzaShop {
    public void orderPizza(String type) {
        Pizza pizza = PizzaFactory.createPizza(type); // only depends on interface
        pizza.prepare();
        pizza.cook();
        pizza.dress();
    }
}
```

### How it works step by step

1. `PizzaShop.orderPizza("Cheese")` is called.
2. It delegates to `PizzaFactory.createPizza("Cheese")`.
3. The factory's `if/else` matches `"Cheese"` and returns `new CheesePizza()`.
4. `PizzaShop` receives a `Pizza` reference — it never knows or cares that it is a `CheesePizza`.
5. It calls `prepare()`, `cook()`, `dress()` through the interface.

### When to use

- You have a small, stable set of types that rarely grows.
- You want to hide construction details from the caller.
- You don't need to swap factory logic at runtime.

### Limitation

Adding a new pizza type (e.g. `PepperoniPizza`) requires **editing `PizzaFactory`**. This violates the **Open/Closed Principle** — open for extension, closed for modification.

---

## 2. Factory Method

### What it is

An official GoF pattern. Instead of a static method, you define an **abstract method** in a base class (or interface). Each subclass overrides this method to decide which concrete product to create. The creation decision moves from a single class into a hierarchy of creators.

> "Define an interface for creating an object, but let subclasses decide which class to instantiate."
> — GoF

### Structure

```
        PizzaShop (abstract)
        ──────────────────────
        + orderPizza()            ← uses createPizza(), shared logic
        # createPizza() : Pizza   ← abstract factory method
               │
       ┌───────┴────────┐
       ▼                ▼
 CheesePizzaShop   VeggiePizzaShop
 createPizza()      createPizza()
   returns             returns
 CheesePizza        VeggiePizza
```

### Code

**`Pizza.java`** — same product interface as before
```java
public interface Pizza {
    void prepare();
    void cook();
    void dress();
}
```

**`CheesePizza.java`** / **`VeggiePizza.java`** — same concrete products as before.

**`PizzaShop.java`** — abstract creator with the factory method
```java
public abstract class PizzaShop {

    // The factory method — subclasses override this
    protected abstract Pizza createPizza();

    // Shared workflow — never changes regardless of pizza type
    public void orderPizza() {
        Pizza pizza = createPizza();   // calls the overridden version
        pizza.prepare();
        pizza.cook();
        pizza.dress();
    }
}
```

**`CheesePizzaShop.java`** — concrete creator for cheese pizzas
```java
public class CheesePizzaShop extends PizzaShop {
    @Override
    protected Pizza createPizza() {
        return new CheesePizza();
    }
}
```

**`VeggiePizzaShop.java`** — concrete creator for veggie pizzas
```java
public class VeggiePizzaShop extends PizzaShop {
    @Override
    protected Pizza createPizza() {
        return new VeggiePizza();
    }
}
```

**`Main.java`** — client code
```java
public class Main {
    public static void main(String[] args) {
        PizzaShop cheeseShop = new CheesePizzaShop();
        cheeseShop.orderPizza(); // creates and orders a CheesePizza

        PizzaShop veggieShop = new VeggiePizzaShop();
        veggieShop.orderPizza(); // creates and orders a VeggiePizza
    }
}
```

### How it works step by step

1. Client creates a `CheesePizzaShop` (a concrete creator).
2. Calls `orderPizza()` — defined in the abstract `PizzaShop`.
3. Inside `orderPizza()`, `createPizza()` is called — but which one? The **overridden** version in `CheesePizzaShop`.
4. `CheesePizzaShop.createPizza()` returns `new CheesePizza()`.
5. `orderPizza()` calls `prepare()`, `cook()`, `dress()` — same shared logic regardless of pizza type.

To add `PepperoniPizza`, you only:
- Create `PepperoniPizza implements Pizza`.
- Create `PepperoniPizzaShop extends PizzaShop` with `createPizza()` returning `new PepperoniPizza()`.
- **Touch zero existing classes.**

### When to use

- You have a shared workflow (`orderPizza`) but need flexibility in what object is created inside it.
- You want adding new types to require only new classes, not edits to existing ones.
- The type of object to create depends on which subclass is instantiated (decided at runtime by the caller).

---

## Simple Factory vs Factory Method — Key Difference

| | Simple Factory | Factory Method |
|---|---|---|
| Creation logic lives in | One static method | Each subclass |
| Adding a new type requires | Editing the factory class | Adding a new subclass |
| Follows Open/Closed Principle | No | Yes |
| Runtime flexibility | Low (pass a string) | High (choose a subclass) |
| Complexity | Low | Medium |

**Simple Factory** — one gatekeeper deciding everything.  
**Factory Method** — each creator knows its own product; the workflow is shared.

---

## 3. Abstract Factory

### What it is

An official GoF pattern. Where Factory Method solves *one* creation decision per subclass, Abstract Factory solves **a family of related creation decisions** at once. You define an interface with one factory method per product in the family, and each concrete factory implements the whole family consistently — no mixing parts from different families.

> "Provide an interface for creating families of related or dependent objects without specifying their concrete classes."
> — GoF

### The problem it solves

Imagine a NY pizza shop and a Chicago pizza shop. Both make pizza, but they use completely different ingredients:

| | NY Style | Chicago Style |
|---|---|---|
| Dough | Thin crust | Thick crust |
| Sauce | Marinara | Plum tomato |
| Cheese | Reggiano | Mozzarella |

Without Abstract Factory, `Pizza` would need to know the region and pick ingredients with `if/else` — mixing creation logic into the product itself. Worse, nothing stops you from accidentally combining NY dough with Chicago sauce.

Abstract Factory guarantees that **all ingredients always come from the same family**.

### Structure

```
         Pizza
           │ has
           ▼
  PizzaIngredientFactory        ← Abstract Factory (interface)
  ──────────────────────
  + createDough()  : Dough
  + createSauce()  : Sauce
  + createCheese() : Cheese
        │                   │
        ▼                   ▼
NYIngredientFactory    ChicagoIngredientFactory
  ThinCrustDough          ThickCrustDough
  MarinaraSauce           PlumTomatoSauce
  ReggianoCheese          MozzarellaCheese
```

### Code (from this project)

**Product interfaces** — one per ingredient type
```java
public interface Dough  { String getType(); }
public interface Sauce  { String getType(); }
public interface Cheese { String getType(); }
```

**Concrete products** — NY family
```java
public class ThinCrustDough  implements Dough  { public String getType() { return "Thin Crust Dough"; } }
public class MarinaraSauce   implements Sauce  { public String getType() { return "Marinara Sauce"; } }
public class ReggianoCheese  implements Cheese { public String getType() { return "Reggiano Cheese"; } }
```

**Concrete products** — Chicago family
```java
public class ThickCrustDough  implements Dough  { public String getType() { return "Thick Crust Dough"; } }
public class PlumTomatoSauce  implements Sauce  { public String getType() { return "Plum Tomato Sauce"; } }
public class MozzarellaCheese implements Cheese { public String getType() { return "Mozzarella Cheese"; } }
```

**`PizzaIngredientFactory.java`** — the Abstract Factory
```java
public interface PizzaIngredientFactory {
    Dough  createDough();
    Sauce  createSauce();
    Cheese createCheese();
}
```

**`NYIngredientFactory.java`** — Concrete Factory for NY family
```java
public class NYIngredientFactory implements PizzaIngredientFactory {
    public Dough  createDough()  { return new ThinCrustDough(); }
    public Sauce  createSauce()  { return new MarinaraSauce(); }
    public Cheese createCheese() { return new ReggianoCheese(); }
}
```

**`ChicagoIngredientFactory.java`** — Concrete Factory for Chicago family
```java
public class ChicagoIngredientFactory implements PizzaIngredientFactory {
    public Dough  createDough()  { return new ThickCrustDough(); }
    public Sauce  createSauce()  { return new PlumTomatoSauce(); }
    public Cheese createCheese() { return new MozzarellaCheese(); }
}
```

**`Pizza.java`** — the product; takes a factory, never mentions a region
```java
public class Pizza {
    private final String name;
    private final PizzaIngredientFactory ingredientFactory;

    public Pizza(String name, PizzaIngredientFactory ingredientFactory) {
        this.name = name;
        this.ingredientFactory = ingredientFactory;
    }

    public void prepare() {
        Dough  dough  = ingredientFactory.createDough();
        Sauce  sauce  = ingredientFactory.createSauce();
        Cheese cheese = ingredientFactory.createCheese();
        System.out.println("Preparing " + name + " with:");
        System.out.println("  Dough:  " + dough.getType());
        System.out.println("  Sauce:  " + sauce.getType());
        System.out.println("  Cheese: " + cheese.getType());
    }
}
```

**`Main.java`** — client swaps the entire family by swapping the factory
```java
public class Main {
    public static void main(String[] args) {
        PizzaIngredientFactory nyFactory = new NYIngredientFactory();
        Pizza nyPizza = new Pizza("NY Cheese Pizza", nyFactory);
        nyPizza.prepare();

        PizzaIngredientFactory chicagoFactory = new ChicagoIngredientFactory();
        Pizza chicagoPizza = new Pizza("Chicago Cheese Pizza", chicagoFactory);
        chicagoPizza.prepare();
    }
}
```

### How it works step by step

1. Client creates `NYIngredientFactory` — a concrete factory for the NY family.
2. Passes it into `Pizza` via the constructor.
3. When `pizza.prepare()` is called, it asks the factory for each ingredient.
4. The factory returns `ThinCrustDough`, `MarinaraSauce`, `ReggianoCheese` — all NY, all consistent.
5. To get a Chicago pizza, replace step 1 with `new ChicagoIngredientFactory()`. Nothing else changes.

### Advantages

**1. Family consistency is enforced at compile time**

There is no way to construct a `Pizza` with NY dough and Chicago sauce — each factory only creates its own family's products. The wrong combination simply cannot be expressed in the type system.

**2. Swapping the entire family is one line change**

```java
// Change this one line to switch every ingredient across the whole pizza
PizzaIngredientFactory factory = new ChicagoIngredientFactory();
```

Without Abstract Factory, swapping would mean hunting for every `new ThinCrustDough()`, `new MarinaraSauce()`, etc. scattered across the codebase.

**3. `Pizza` is completely decoupled from concrete ingredients**

`Pizza` only depends on the `PizzaIngredientFactory` interface and the product interfaces (`Dough`, `Sauce`, `Cheese`). It has zero knowledge of `ThinCrustDough` or `MozzarellaCheese`. This makes `Pizza` independently testable — inject a mock factory in tests.

**4. Adding a new region requires only new classes**

To support a "London style" pizza:
- Add `SourdoughDough`, `CurrySauce`, `CheddarCheese` (new concrete products).
- Add `LondonIngredientFactory implements PizzaIngredientFactory`.
- **Touch zero existing classes** — `Pizza`, `NYIngredientFactory`, and `ChicagoIngredientFactory` are unchanged.

**5. Dependency injection friendly**

Because `Pizza` takes the factory as a constructor argument, the factory can be injected from outside. In a Spring/DI context, you can bind `PizzaIngredientFactory` to different concrete factories per environment (e.g. regional deployment) without changing any product code.

### Limitation

Adding a **new product type** (e.g. adding `Topping` to the family) requires:
- Adding `createTopping()` to the `PizzaIngredientFactory` interface.
- Updating **every existing concrete factory** (`NYIngredientFactory`, `ChicagoIngredientFactory`, etc.).

This is the main trade-off: Abstract Factory is open to new *families* but closed to new *product types within the family*.

### When to use

- You need to create groups of related objects that must be used together.
- You want to enforce consistency across a family — no mismatched parts.
- You need to swap an entire product family at runtime (e.g. per region, per theme, per environment).
- You are designing a system where the client should not depend on concrete product classes at all.

---

## Real-World Analogies

| Concept | Real World |
|---|---|
| `Pizza` interface | A standard order form every pizza must fill |
| `PizzaFactory` (Simple) | A single chef who reads the order and decides what to make |
| `CheesePizzaShop` (Factory Method) | A specialist cheese-pizza restaurant — it only ever makes one thing, but makes it perfectly |
| Abstract Factory | A franchise kit (dough recipe + sauce recipe + cheese type) that ensures every outlet is consistent |

---

## When to Use What

### Use Simple Factory when…

| Condition | Reason |
|---|---|
| The set of types is **small and stable** | If you rarely add new types, the `if/else` in the factory never becomes a maintenance burden |
| You just want to **hide `new`** from the caller | The caller gets a clean interface without knowing concrete classes |
| You need a **quick solution** with minimal structure | No inheritance hierarchy needed — one class does the job |
| The creation logic is **not expected to vary** at runtime | A static method is sufficient; no need for polymorphism |

**Avoid it when** the type list keeps growing. Every new type requires opening `PizzaFactory` and editing it, which risks introducing bugs in existing logic.

---

### Use Factory Method when…

| Condition | Reason |
|---|---|
| You have a **shared workflow** but the object created inside it must vary | `orderPizza()` is the same steps for every shop — only `createPizza()` differs |
| You want **adding new types to require zero edits** to existing code | Each new type gets its own subclass; nothing existing is touched |
| The type of object to create is **decided by the caller at runtime** | Caller picks `CheesePizzaShop` vs `VeggiePizzaShop`; framework picks the product |
| You are designing a **framework or library** where you control the workflow but the user controls the product | The abstract class defines the algorithm; user subclasses fill in the creation step |

**Avoid it when** you have families of related objects. Factory Method handles one product per subclass — combining multiple related products leads to an explosion of subclasses.

---

### Use Abstract Factory when…

| Condition | Reason |
|---|---|
| You need to create **multiple related objects that must be used together** | NY pizza needs NY dough + NY sauce + NY cheese — parts from different families must not mix |
| You need to **swap an entire product family** as a single unit | Change one factory reference to switch every ingredient across the whole system |
| Your system must be **independent of how its products are created** | `Pizza` never mentions `ThinCrustDough` or `MarinaraSauce` — it only talks to interfaces |
| You are building for **multiple configurations or environments** | Different DB drivers per environment, different UI themes per platform, different ingredient sets per region |
| You want **compile-time enforcement** of family consistency | The type system makes it impossible to mix a NY factory's dough with Chicago factory's sauce |

**Avoid it when** you only have one product type. Using Abstract Factory for a single product is over-engineering — Factory Method is sufficient. Also avoid it when the family composition (which products belong together) changes frequently, since adding a new product type forces changes to every concrete factory.

---

### Side-by-side comparison

| Question | Simple Factory | Factory Method | Abstract Factory |
|---|---|---|---|
| How many product types? | One | One | Multiple (a family) |
| Who decides the concrete type? | Static method parameter | Subclass override | Concrete factory class |
| Can you add types without editing existing code? | No | Yes (new subclass) | Yes (new factory + products) |
| Are related objects guaranteed consistent? | No | No | Yes |
| Runtime family swap? | No | No | Yes |
| Complexity | Low | Medium | High |
| GoF official pattern? | No | Yes | Yes |

---

### Decision flowchart

```
Do you need to hide object creation from the caller?
│
├─ No  ──▶ Just use new directly
│
└─ Yes
    │
    Do you need to create a FAMILY of related objects together?
    │
    ├─ Yes ──▶ Abstract Factory
    │           (multiple products, family consistency required)
    │
    └─ No
        │
        Is there a shared workflow that should not change
        when the product type changes?
        │
        ├─ Yes ──▶ Factory Method
        │           (subclass controls what is created,
        │            base class controls how it is used)
        │
        └─ No  ──▶ Simple Factory
                    (small stable type set,
                     quick centralised creation)
```

---

### Real-world examples outside pizza

| Pattern | Java/Framework example |
|---|---|
| Simple Factory | `java.text.NumberFormat.getInstance(Locale)` — returns the right formatter for a locale |
| Factory Method | `java.util.Iterator` — `Collection.iterator()` is the factory method; each collection returns its own iterator type |
| Abstract Factory | `javax.xml.parsers.DocumentBuilderFactory` — swaps the entire XML parsing family (DOM, SAX) by changing the factory |
