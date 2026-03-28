package org.example.service;

import org.example.notification.Observer;

import java.util.ArrayList;
import java.util.List;

/**
 * Dispatches domain/application events to subscribed observers.
 *
 * @author appointment-system
 * @version 1.0
 */
public class EventManager {

    private final List<Observer> observers = new ArrayList<>();

    /**
     * Subscribes an observer if it is non-null and not already subscribed.
     *
     * @param observer observer to subscribe
     */
    public void subscribe(Observer observer) {
        if (observer == null || observers.contains(observer)) {
            return;
        }
        observers.add(observer);
    }

    /**
     * Unsubscribes an observer when present.
     *
     * @param observer observer to remove
     */
    public void unsubscribe(Observer observer) {
        if (observer == null) {
            return;
        }
        observers.remove(observer);
    }

    /**
     * Notifies all subscribed observers with the provided message.
     *
     * @param message message payload sent to observers
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
     * Backward-compatible alias for {@link #notifyObservers(String)}.
     *
     * @param message message payload sent to observers
     */
    public void notifyAllObservers(String message) {
        notifyObservers(message);
    }
}