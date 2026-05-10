package org.example.presentation.gui;

import javax.swing.AbstractButton;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * GuiTestSupport class.
 */
abstract class GuiTestSupport {

    protected static void runOnEdt(ThrowingRunnable action) {
        if (SwingUtilities.isEventDispatchThread()) {
            try {
                action.run();
                return;
            } catch (Exception ex) {
                throw rethrow(ex);
            }
        }

        FutureTask<Void> task = new FutureTask<>(() -> {
            action.run();
            return null;
        });
        try {
            SwingUtilities.invokeAndWait(task);
            task.get();
        } catch (InvocationTargetException ex) {
            throw rethrow(ex.getTargetException());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted while waiting for Swing action", ex);
        } catch (Exception ex) {
            throw rethrow(ex);
        }
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
            if (component instanceof AbstractButton button) {
                if (text.equals(button.getText())) {
                    return button;
                }
            }
            if (component instanceof Container container) {
                AbstractButton nested = findButton(container, text);
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

        Timer dialogCloser = new Timer(25, event -> closeModalDialogs());
        dialogCloser.setRepeats(true);
        try {
            runOnEdt(() -> {
                dialogCloser.start();
                button.doClick();
            });
        } finally {
            runOnEdt(dialogCloser::stop);
            closeModalDialogs();
        }
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

    private static void closeModalDialogs() {
        if (!SwingUtilities.isEventDispatchThread()) {
            runOnEdt(GuiTestSupport::closeModalDialogs);
            return;
        }

        for (Window window : Window.getWindows()) {
            if (window instanceof JDialog && window.isShowing()) {
                window.dispose();
            }
        }
    }

    @FunctionalInterface
    protected interface ThrowingRunnable {
        void run() throws Exception;
    }
}

