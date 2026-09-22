package com.designpatterns.factory.abstractfactory;

// Concrete Factory — Chicago family: thick crust, plum tomato, mozzarella
public class ChicagoIngredientFactory implements PizzaIngredientFactory {
    @Override
    public Dough  createDough()  { return new ThickCrustDough(); }

    @Override
    public Sauce  createSauce()  { return new PlumTomatoSauce(); }

    @Override
    public Cheese createCheese() { return new MozzarellaCheese(); }
}
