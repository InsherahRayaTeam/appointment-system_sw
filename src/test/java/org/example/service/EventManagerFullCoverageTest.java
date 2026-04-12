package org.example.service;

import org.example.notification.Observer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventManagerFullCoverageTest {

    @Test
    void shouldNotifySingleObserver() {
        EventManager eventManager = new EventManager();
        TestObserver observer = new TestObserver();

        eventManager.subscribe(observer);
        eventManager.notifyObservers("message-1");

        assertTrue(observer.notified);
        assertEquals(1, observer.callCount);
    }

    @Test
    void shouldNotifyMultipleObservers() {
        EventManager eventManager = new EventManager();
        TestObserver observer1 = new TestObserver();
        TestObserver observer2 = new TestObserver();

        eventManager.subscribe(observer1);
        eventManager.subscribe(observer2);
        eventManager.notifyObservers("message-1");

        assertTrue(observer1.notified);
        assertTrue(observer2.notified);
        assertEquals(1, observer1.callCount);
        assertEquals(1, observer2.callCount);
    }

    @Test
    void shouldNotFailWithNoObservers() {
        EventManager eventManager = new EventManager();

        assertDoesNotThrow(() -> {
            eventManager.unsubscribe(null);
            eventManager.notifyObservers("message-1");
        });
    }

    @Test
    void shouldNotNotifyAfterUnsubscribe() {
        EventManager eventManager = new EventManager();
        TestObserver observer = new TestObserver();

        eventManager.subscribe(observer);
        eventManager.unsubscribe(observer);
        eventManager.notifyObservers("message-1");

        assertFalse(observer.notified);
        assertEquals(0, observer.callCount);
    }

    @Test
    void shouldIgnoreNullSubscribe() {
        EventManager eventManager = new EventManager();
        TestObserver observer = new TestObserver();

        eventManager.subscribe(null);
        eventManager.subscribe(observer);
        eventManager.notifyObservers("message-1");

        assertTrue(observer.notified);
        assertEquals(1, observer.callCount);
    }

    @Test
    void shouldIgnoreNullUnsubscribe() {
        EventManager eventManager = new EventManager();
        TestObserver observer = new TestObserver();

        eventManager.subscribe(observer);
        eventManager.unsubscribe(null);
        eventManager.notifyObservers("message-1");

        assertTrue(observer.notified);
        assertEquals(1, observer.callCount);
    }

    @Test
    void shouldNotDuplicateObserverSubscription() {
        EventManager eventManager = new EventManager();
        TestObserver observer = new TestObserver();

        eventManager.subscribe(observer);
        eventManager.subscribe(observer);
        eventManager.notifyObservers("message-1");

        assertTrue(observer.notified);
        assertEquals(1, observer.callCount);
    }

    @Test
    void shouldHandleNotifyMultipleTimes() {
        EventManager eventManager = new EventManager();
        TestObserver observer = new TestObserver();

        eventManager.subscribe(observer);
        eventManager.notifyObservers("message-1");
        eventManager.notifyObservers("message-2");
        eventManager.notifyObservers("message-3");

        assertTrue(observer.notified);
        assertEquals(3, observer.callCount);
    }

    private static class TestObserver implements Observer {
        private boolean notified;
        private int callCount;

        @Override
        public void update(String message) {
            notified = true;
            callCount++;
        }
    }
}

