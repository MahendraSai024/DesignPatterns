package com.designpatterns.factory.abstractfactory;

// Pizza knows how to prepare itself using whatever factory it was given
public class Pizza {
    private final String name;
    private final PizzaIngredientFactory ingredientFactory;

    public Pizza(String name, PizzaIngredientFactory ingredientFactory) {
        this.name = name;
        this.ingredientFactory = ingredientFactory;
    }

    public void prepare() {
        Dough  dough  = ingredientFactory.createDough();
        Sauce  sauce  = ingredientFactory.createSauce();
        Cheese cheese = ingredientFactory.createCheese();
        System.out.println("Preparing " + name + " with:");
        System.out.println("  Dough:  " + dough.getType());
        System.out.println("  Sauce:  " + sauce.getType());
        System.out.println("  Cheese: " + cheese.getType());
    }

    public void cook()  { System.out.println("Cooking "  + name); }
    public void dress() { System.out.println("Dressing " + name); }
}
