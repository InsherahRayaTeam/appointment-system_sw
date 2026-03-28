package org.example.service;

import org.example.notification.Observer;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class EventManagerTest {

    @Test
    void shouldNotifySubscribedObserverOnce() {
        EventManager eventManager = new EventManager();
        Observer observer = mock(Observer.class);

        eventManager.subscribe(observer);
        eventManager.notifyAllObservers("test message");

        verify(observer, times(1)).update("test message");
    }
}

