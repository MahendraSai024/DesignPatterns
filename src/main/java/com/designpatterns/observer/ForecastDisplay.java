package com.designpatterns.observer;

public class ForecastDisplay implements Observer {

    // sentinel: no previous reading yet
    private float lastPressure = Float.NaN;

    @Override
    public void update(float temperature, float humidity, float pressure) {
        String forecast;
        if (Float.isNaN(lastPressure) || pressure == lastPressure) {
            forecast = "Stable — same as before";
        } else if (pressure > lastPressure) {
            forecast = "Improving — sunny skies ahead";
        } else {
            forecast = "Worsening — watch for rain";
        }
        lastPressure = pressure;
        System.out.println("[FORECAST] " + forecast);
    }
}
