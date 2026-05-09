package org.example.presentation.gui;

import java.awt.Component;

/**
 * Abstraction for showing dialogs so tests can replace blocking JOptionPane calls.
 */
public interface DialogDisplayer {
    void showMessage(Component parentComponent, Object message);

    void showMessage(Component parentComponent, Object message, String title, int messageType);
}

