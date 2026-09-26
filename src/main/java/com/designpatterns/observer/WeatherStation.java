package com.designpatterns.observer;

import java.util.ArrayList;
import java.util.List;

public class WeatherStation implements Subject {

    private final List<Observer> observers = new ArrayList<>();
    private float temperature;
    private float humidity;
    private float pressure;

    @Override
    public void registerObserver(Observer o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers() {
        for (Observer o : observers) {
            o.update(temperature, humidity, pressure);
        }
    }

    public void setMeasurements(float temperature, float humidity, float pressure) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.pressure = pressure;
        System.out.printf("[STATION] New readings — temp: %.1f°C  humidity: %.1f%%  pressure: %.1f hPa%n",
                temperature, humidity, pressure);
        notifyObservers();
    }
}
