package com.designpatterns.observer;

// Subject — manages the observer list and drives notifications
public interface Subject {
    void registerObserver(Observer o);
    void removeObserver(Observer o);
    void notifyObservers();
}
