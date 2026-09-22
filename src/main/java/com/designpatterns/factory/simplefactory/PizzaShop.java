package com.designpatterns.factory.simplefactory;

public class PizzaShop {

    public void orderPizza(String type) {
        Pizza pizza = PizzaFactory.createPizza(type);
        pizza.prepare();
        pizza.cook();
        pizza.dress();
    }

    public static void main(String[] args) {
        PizzaShop shop = new PizzaShop();
        shop.orderPizza("Cheese");
        shop.orderPizza("Veggie");
    }
}
