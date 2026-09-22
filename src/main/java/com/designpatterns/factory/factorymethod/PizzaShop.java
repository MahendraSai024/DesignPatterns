package com.designpatterns.factory.factorymethod;

// Creator — defines the factory method and the shared workflow
public abstract class PizzaShop {

    // Factory Method — subclasses decide what to instantiate
    protected abstract Pizza createPizza();

    // Shared workflow; never needs to change when new pizza types are added
    public void orderPizza() {
        Pizza pizza = createPizza();
        pizza.prepare();
        pizza.cook();
        pizza.dress();
    }
}
