package org.example.presentation.gui;

import javax.swing.JOptionPane;
import java.awt.Component;

/**
 * Production DialogDisplayer that delegates to JOptionPane.
 */
public enum DefaultDialogDisplayer implements DialogDisplayer {
    INSTANCE;

    @Override
    public void showMessage(Component parentComponent, Object message) {
        JOptionPane.showMessageDialog(parentComponent, message);
    }

    @Override
    public void showMessage(Component parentComponent, Object message, String title, int messageType) {
        JOptionPane.showMessageDialog(parentComponent, message, title, messageType);
    }
}

