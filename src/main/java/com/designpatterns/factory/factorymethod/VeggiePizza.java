package com.designpatterns.factory.factorymethod;

public class VeggiePizza implements Pizza {
    @Override
    public void prepare() { System.out.println("Preparing Veggie Pizza base"); }

    @Override
    public void cook() { System.out.println("Cooking Veggie Pizza"); }

    @Override
    public void dress() { System.out.println("Dressing Veggie Pizza"); }
}
