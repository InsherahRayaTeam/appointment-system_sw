package org.example.service;

import org.example.notification.Observer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class EventManagerTest {

    @Test
    void shouldNotifySubscribedObserverOnce() {
        EventManager eventManager = new EventManager();
        Observer observer = mock(Observer.class);

        eventManager.subscribe(observer);
        eventManager.notifyObservers("test message");

        verify(observer, times(1)).update("test message");
    }

    @Test
    void shouldNotNotifyWhenNoObserversSubscribed() {
        EventManager eventManager = new EventManager();

        assertDoesNotThrow(() -> eventManager.notifyObservers("test message"));
    }

    @Test
    void shouldIgnoreNullObserverSubscription() {
        EventManager eventManager = new EventManager();

        assertDoesNotThrow(() -> eventManager.subscribe(null));
        assertDoesNotThrow(() -> eventManager.notifyObservers("test message"));
    }

    @Test
    void shouldPreventDuplicateSubscription() {
        EventManager eventManager = new EventManager();
        Observer observer = mock(Observer.class);

        eventManager.subscribe(observer);
        eventManager.subscribe(observer);
        eventManager.notifyObservers("test message");

        verify(observer, times(1)).update("test message");
    }

    @Test
    void shouldNotifyMultipleObservers() {
        EventManager eventManager = new EventManager();
        Observer observer1 = mock(Observer.class);
        Observer observer2 = mock(Observer.class);

        eventManager.subscribe(observer1);
        eventManager.subscribe(observer2);
        eventManager.notifyObservers("test message");

        verify(observer1, times(1)).update("test message");
        verify(observer2, times(1)).update("test message");
    }

    @Test
    void shouldNotNotifyAfterUnsubscribe() {
        EventManager eventManager = new EventManager();
        Observer observer = mock(Observer.class);

        eventManager.subscribe(observer);
        eventManager.unsubscribe(observer);
        eventManager.notifyObservers("test message");

        verify(observer, never()).update("test message");
    }

    @Test
    void shouldIgnoreNullUnsubscribe() {
        EventManager eventManager = new EventManager();
        Observer observer = mock(Observer.class);

        eventManager.subscribe(observer);
        assertDoesNotThrow(() -> eventManager.unsubscribe(null));
        eventManager.notifyObservers("test message");

        verify(observer, times(1)).update("test message");
    }

    @Test
    void shouldKeepBackwardCompatibilityForNotifyAllObservers() {
        EventManager eventManager = new EventManager();
        Observer observer = mock(Observer.class);

        eventManager.subscribe(observer);
        eventManager.notifyAllObservers("test message");

        verify(observer, times(1)).update("test message");
    }
}

