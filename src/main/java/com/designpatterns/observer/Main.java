package com.designpatterns.observer;

public class Main {

    public static void main(String[] args) {
        System.out.println("=== Observer Pattern — Weather Station ===\n");

        WeatherStation station = new WeatherStation();

        CurrentConditionsDisplay current  = new CurrentConditionsDisplay("Living Room");
        StatisticsDisplay        stats    = new StatisticsDisplay();
        ForecastDisplay          forecast = new ForecastDisplay();

        station.registerObserver(current);
        station.registerObserver(stats);
        station.registerObserver(forecast);

        System.out.println("[Update 1]");
        station.setMeasurements(22.5f, 65.0f, 1013.0f);

        System.out.println("\n[Update 2]");
        station.setMeasurements(25.0f, 70.0f, 1010.0f);

        System.out.println("\n--- Removing ForecastDisplay ---");
        station.removeObserver(forecast);

        System.out.println("\n[Update 3 — ForecastDisplay unsubscribed]");
        station.setMeasurements(19.0f, 80.0f, 1015.0f);
    }
}
