package com.designpatterns.factory.factorymethod;

// Concrete Creator — overrides the factory method to return a CheesePizza
public class CheesePizzaShop extends PizzaShop {
    @Override
    protected Pizza createPizza() {
        return new CheesePizza();
    }
}
