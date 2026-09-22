package com.designpatterns.factory.abstractfactory;

// Abstract Factory — declares creation methods for each ingredient in the family
public interface PizzaIngredientFactory {
    Dough  createDough();
    Sauce  createSauce();
    Cheese createCheese();
}
