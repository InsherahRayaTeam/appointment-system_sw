package org.example.presentation.gui;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.event.ActionListener;
import java.awt.*;

/**
 * Shared Swing style helpers for consistent spacing, fonts, and control sizing.
 */
public final class UiStyle {

    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("SansSerif", Font.BOLD, 16);
    public static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font FONT_LABEL = new Font("SansSerif", Font.BOLD, 14);

    public static final Dimension BUTTON_PRIMARY = new Dimension(180, 36);
    public static final Dimension BUTTON_SECONDARY = new Dimension(140, 34);

    public static final int SPACE_XS = 8;
    public static final int SPACE_SM = 12;
    public static final int SPACE_MD = 16;

    public static final Color COLOR_BACKGROUND = new Color(246, 248, 251);
    public static final Color COLOR_CARD = Color.WHITE;
    public static final Color COLOR_BORDER = new Color(218, 223, 230);
    public static final Color COLOR_SUCCESS = new Color(21, 128, 61);
    public static final Color COLOR_ERROR = new Color(185, 28, 28);
    public static final Color COLOR_INFO = new Color(30, 64, 175);

    private UiStyle() {
    }

    public static Border createCardBorder() {
        return new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(18, 18, 18, 18)
        );
    }

    public static void stylePrimaryButton(JButton button) {
        button.setFont(FONT_LABEL);
        button.setPreferredSize(BUTTON_PRIMARY);
        button.setFocusPainted(false);
    }

    public static void styleSecondaryButton(JButton button) {
        button.setFont(FONT_BODY);
        button.setPreferredSize(BUTTON_SECONDARY);
        button.setFocusPainted(false);
    }

    public static JButton createPrimaryButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        stylePrimaryButton(button);
        button.addActionListener(listener);
        return button;
    }

    public static JButton createSecondaryButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        styleSecondaryButton(button);
        button.addActionListener(listener);
        return button;
    }

    public static JPanel createCardPanel(LayoutManager layoutManager) {
        JPanel panel = new JPanel(layoutManager);
        panel.setBackground(COLOR_CARD);
        panel.setBorder(createCardBorder());
        return panel;
    }

    public static JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_LABEL);
        return label;
    }

    public static void styleTextComponent(JTextComponent textComponent) {
        textComponent.setFont(FONT_BODY);
    }

    public static JPanel createPagePanel(LayoutManager layoutManager) {
        JPanel panel = new JPanel(layoutManager);
        panel.setBackground(COLOR_BACKGROUND);
        return panel;
    }
}

