package org.example.service;

import org.example.notification.Observer;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages observers and sends text events to them.
 */
public class EventManager {

    private final List<Observer> observers = new ArrayList<>();

    /**
     * Adds an observer if it is not null and not already subscribed.
     *
     * @param observer value for observer
     */
    public void subscribe(Observer observer) {
        if (observer == null || observers.contains(observer)) {
            return;
        }
        observers.add(observer);
    }

    /**
     * Removes an observer when it exists.
     *
     * @param observer value for observer
     */
    public void unsubscribe(Observer observer) {
        if (observer == null) {
            return;
        }
        observers.remove(observer);
    }

    /**
     * Sends observers to listeners.
     *
     * @param message message text to show or send
     */
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

    /**
     * Sends all observers to listeners.
     *
     * @param message message text to show or send
     */
    public void notifyAllObservers(String message) {
        notifyObservers(message);
    }
}
