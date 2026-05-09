package org.example.presentation.gui;

import java.awt.Component;

/**
 * Test dialog displayer - no-op so modal dialogs don't block tests.
 */
public class TestDialogDisplayer implements DialogDisplayer {
    @Override
    public void showMessage(Component parentComponent, Object message) {
        // no-op for tests
    }

    @Override
    public void showMessage(Component parentComponent, Object message, String title, int messageType) {
        // no-op for tests
    }
}

