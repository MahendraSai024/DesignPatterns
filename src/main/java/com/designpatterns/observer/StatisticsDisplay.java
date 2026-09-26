package com.designpatterns.observer;

public class StatisticsDisplay implements Observer {

    private float minTemp = Float.MAX_VALUE;
    private float maxTemp = Float.MIN_VALUE;
    private float sumTemp = 0;
    private int count = 0;

    @Override
    public void update(float temperature, float humidity, float pressure) {
        if (temperature < minTemp) minTemp = temperature;
        if (temperature > maxTemp) maxTemp = temperature;
        sumTemp += temperature;
        count++;
        float avg = sumTemp / count;
        System.out.printf("[STATS  ] Temp — min: %.1f°C  max: %.1f°C  avg: %.1f°C%n", minTemp, maxTemp, avg);
    }
}
