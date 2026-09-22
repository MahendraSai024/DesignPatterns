package com.designpatterns.factory.abstractfactory;

public class Main {
    public static void main(String[] args) {
        // NY shop gets a NY ingredient factory — thin crust, marinara, reggiano
        PizzaIngredientFactory nyFactory = new NYIngredientFactory();
        Pizza nyPizza = new Pizza("NY Cheese Pizza", nyFactory);
        nyPizza.prepare();
        nyPizza.cook();
        nyPizza.dress();

        System.out.println();

        // Chicago shop gets a Chicago ingredient factory — thick crust, plum tomato, mozzarella
        PizzaIngredientFactory chicagoFactory = new ChicagoIngredientFactory();
        Pizza chicagoPizza = new Pizza("Chicago Cheese Pizza", chicagoFactory);
        chicagoPizza.prepare();
        chicagoPizza.cook();
        chicagoPizza.dress();
    }
}
