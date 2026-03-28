package org.example.service;

import org.example.notification.Observer;

import java.util.ArrayList;
import java.util.List;

public class EventManager {

    private final List<Observer> observers = new ArrayList<>();

    public void subscribe(Observer observer) {
        if (observer == null || observers.contains(observer)) {
            return;
        }
        observers.add(observer);
    }

    public void unsubscribe(Observer observer) {
        if (observer == null) {
            return;
        }
        observers.remove(observer);
    }

    public void notifyObservers(String message) {
        if (message == null) {
            return;
        }

        List<Observer> snapshot = new ArrayList<>(observers);
        for (Observer observer : snapshot) {
            if (observer == null) {
                continue;
            }
            observer.update(message);
        }
    }

    public void notifyAllObservers(String message) {
        notifyObservers(message);
    }
}