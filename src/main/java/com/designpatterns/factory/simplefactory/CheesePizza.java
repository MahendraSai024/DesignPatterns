package com.designpatterns.factory.simplefactory;

public class CheesePizza implements Pizza {
    @Override
    public void prepare() { System.out.println("Preparing Cheese Pizza base"); }

    @Override
    public void cook() { System.out.println("Cooking Cheese Pizza"); }

    @Override
    public void dress() { System.out.println("Dressing Cheese Pizza"); }
}
