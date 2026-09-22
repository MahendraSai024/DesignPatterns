package com.designpatterns.factory.factorymethod;

// Concrete Creator — overrides the factory method to return a VeggiePizza
public class VeggiePizzaShop extends PizzaShop {
    @Override
    protected Pizza createPizza() {
        return new VeggiePizza();
    }
}
