package com.designpatterns.factory.simplefactory;

public class PizzaFactory {
    public static Pizza createPizza(String type) {
        if ("Cheese".equalsIgnoreCase(type)) {
            return new CheesePizza();
        } else if ("Veggie".equalsIgnoreCase(type)) {
            return new VeggiePizza();
        }
        throw new IllegalArgumentException("Unknown pizza type: " + type);
    }
}
