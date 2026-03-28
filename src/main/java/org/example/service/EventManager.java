package org.example.service;

import org.example.notification.Observer;

import java.util.ArrayList;
import java.util.List;

public class EventManager {

    private final List<Observer> observers = new ArrayList<>();

    public void subscribe(Observer observer) {
        observers.add(observer);
    }

    public void notifyAllObservers(String message) {
        for (Observer observer : observers) {
            observer.update(message);
        }
    }
}