package org.example.service;

import org.example.notification.Observer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventManagerCoverageTest {

    @Test
    void subscribeObserver_observerReceivesNotification() {
        EventManager eventManager = new EventManager();
        TestObserver observer = new TestObserver();

        eventManager.subscribe(observer);
        eventManager.notifyObservers("hello");

        assertTrue(observer.wasNotified());
        assertEquals(1, observer.getNotificationCount());
    }

    @Test
    void unsubscribeObserver_observerDoesNotReceiveNotification() {
        EventManager eventManager = new EventManager();
        TestObserver observer = new TestObserver();

        eventManager.subscribe(observer);
        eventManager.unsubscribe(observer);
        eventManager.notifyObservers("hello");

        assertFalse(observer.wasNotified());
        assertEquals(0, observer.getNotificationCount());
    }

    @Test
    void noObservers_notifyDoesNotFail() {
        EventManager eventManager = new EventManager();

        assertDoesNotThrow(() -> eventManager.notifyObservers("hello"));
    }

    @Test
    void duplicateSubscription_observerNotifiedOnlyOnce() {
        EventManager eventManager = new EventManager();
        TestObserver observer = new TestObserver();

        eventManager.subscribe(observer);
        eventManager.subscribe(observer);
        eventManager.notifyObservers("hello");

        assertTrue(observer.wasNotified());
        assertEquals(1, observer.getNotificationCount());
    }

    @Test
    void nullObserverHandling_isIgnoredSafely() {
        EventManager eventManager = new EventManager();
        TestObserver observer = new TestObserver();

        assertDoesNotThrow(() -> eventManager.subscribe(null));
        assertDoesNotThrow(() -> eventManager.unsubscribe(null));

        eventManager.subscribe(observer);
        eventManager.notifyObservers("hello");

        assertTrue(observer.wasNotified());
        assertEquals(1, observer.getNotificationCount());
    }

    @Test
    void multipleObservers_allAreNotified() {
        EventManager eventManager = new EventManager();
        TestObserver observer1 = new TestObserver();
        TestObserver observer2 = new TestObserver();

        eventManager.subscribe(observer1);
        eventManager.subscribe(observer2);
        eventManager.notifyObservers("hello");

        assertTrue(observer1.wasNotified());
        assertTrue(observer2.wasNotified());
        assertEquals(1, observer1.getNotificationCount());
        assertEquals(1, observer2.getNotificationCount());
    }

    private static class TestObserver implements Observer {
        private boolean notified;
        private int notificationCount;

        @Override
        public void update(String message) {
            notified = true;
            notificationCount++;
        }

        boolean wasNotified() {
            return notified;
        }

        int getNotificationCount() {
            return notificationCount;
        }
    }
}

