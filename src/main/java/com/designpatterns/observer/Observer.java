package com.designpatterns.observer;

// Observer — implemented by any class that wants to receive state-change notifications
public interface Observer {
    void update(float temperature, float humidity, float pressure);
}
