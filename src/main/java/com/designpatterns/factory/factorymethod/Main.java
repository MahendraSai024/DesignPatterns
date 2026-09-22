package com.designpatterns.factory.factorymethod;

public class Main {
    public static void main(String[] args) {
        PizzaShop cheeseShop = new CheesePizzaShop();
        cheeseShop.orderPizza();

        System.out.println();

        PizzaShop veggieShop = new VeggiePizzaShop();
        veggieShop.orderPizza();
    }
}
