package com.designpatterns.observer;

public class CurrentConditionsDisplay implements Observer {

    private final String name;

    public CurrentConditionsDisplay(String name) {
        this.name = name;
    }

    @Override
    public void update(float temperature, float humidity, float pressure) {
        System.out.printf("[CURRENT] %s — Temp: %.1f°C, Humidity: %.1f%%%n", name, temperature, humidity);
    }
}
