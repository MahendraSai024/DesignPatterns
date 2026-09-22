package com.designpatterns.factory.abstractfactory;

// Concrete Factory — NY family: thin crust, marinara, reggiano
public class NYIngredientFactory implements PizzaIngredientFactory {
    @Override
    public Dough  createDough()  { return new ThinCrustDough(); }

    @Override
    public Sauce  createSauce()  { return new MarinaraSauce(); }

    @Override
    public Cheese createCheese() { return new ReggianoCheese(); }
}
