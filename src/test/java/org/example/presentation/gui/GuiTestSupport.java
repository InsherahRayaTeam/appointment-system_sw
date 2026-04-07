package org.example.presentation.gui;

import javax.swing.AbstractButton;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * Test-only utilities for interacting with Swing components in a safe, readable way.
 */
abstract class GuiTestSupport {

    protected static void runOnEdt(ThrowingRunnable action) {
        callOnEdt(() -> {
            action.run();
            return null;
        });
    }

    protected static <T> T callOnEdt(Callable<T> action) {
        if (SwingUtilities.isEventDispatchThread()) {
            try {
                return action.call();
            } catch (Exception ex) {
                throw rethrow(ex);
            }
        }

        FutureTask<T> task = new FutureTask<>(action);
        try {
            SwingUtilities.invokeAndWait(task);
            return task.get();
        } catch (InvocationTargetException ex) {
            throw rethrow(ex.getTargetException());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted while waiting for Swing action", ex);
        } catch (Exception ex) {
            throw rethrow(ex);
        }
    }

    protected static <T> T getPrivateField(Object target, String fieldName, Class<T> type) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return type.cast(field.get(target));
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError("Unable to read field '" + fieldName + "' from " + target.getClass(), ex);
        }
    }

    protected static AbstractButton findButton(Container root, String text) {
        for (Component component : root.getComponents()) {
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                if (text.equals(button.getText())) {
                    return button;
                }
            }
            if (component instanceof Container) {
                AbstractButton nested = findButton((Container) component, text);
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }

    protected static void clickButton(AbstractButton button) {
        if (button == null) {
            throw new AssertionError("Button not found");
        }
        runOnEdt(button::doClick);
    }

    protected static void disposeIfWindow(Object candidate) {
        if (candidate instanceof Window) {
            runOnEdt(((Window) candidate)::dispose);
        }
    }

    private static RuntimeException rethrow(Throwable throwable) {
        if (throwable instanceof RuntimeException) {
            return (RuntimeException) throwable;
        }
        if (throwable instanceof Error) {
            throw (Error) throwable;
        }
        return new RuntimeException(throwable);
    }

    @FunctionalInterface
    protected interface ThrowingRunnable {
        void run() throws Exception;
    }
}

