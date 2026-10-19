// Implements the desktop FoodOS ui theme screen using Swing components.
// Handles user interactions in the view while delegating business work.

package com.foodapp.view;

import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.UIManager;

public final class UiTheme {
    private UiTheme() { }

    public static final Color BACKGROUND = new Color(247, 248, 250);
    public static final Color SURFACE = Color.WHITE;
    public static final Color SIDEBAR_BG = new Color(17, 24, 39);
    public static final Color SIDEBAR_HOVER = new Color(31, 41, 55);
    public static final Color SIDEBAR_ACTIVE = new Color(37, 99, 235);
    public static final Color BORDER = new Color(229, 231, 235);
    public static final Color BORDER_SOFT = new Color(243, 244, 246);
    public static final Color PRIMARY = new Color(37, 99, 235);
    public static final Color PRIMARY_DARK = new Color(29, 78, 216);
    public static final Color PRIMARY_LIGHT = new Color(219, 234, 254);
    public static final Color TEXT = new Color(17, 24, 39);
    public static final Color MUTED = new Color(107, 114, 128);
    public static final Color MUTED_LIGHT = new Color(156, 163, 175);
    public static final Color SUCCESS = new Color(22, 163, 74);
    public static final Color SUCCESS_LIGHT = new Color(220, 252, 231);
    public static final Color WARNING = new Color(217, 119, 6);
    public static final Color WARNING_LIGHT = new Color(254, 243, 199);
    public static final Color ERROR = new Color(220, 38, 38);
    public static final Color ERROR_LIGHT = new Color(254, 226, 226);
    public static final Color INFO = new Color(37, 99, 235);
    public static final Color INFO_LIGHT = new Color(219, 234, 254);

    public static final int SPACING_XS = 4;
    public static final int SPACING_SM = 8;
    public static final int SPACING_MD = 16;
    public static final int SPACING_LG = 24;
    public static final int SPACING_XL = 32;
    public static final int CARD_RADIUS = 10;

    public static Font font(int size, int style) { return new Font("SansSerif", style, size); }
    public static Font heading(int size) { return font(size, Font.BOLD); }
    public static Font body(int size) { return font(size, Font.PLAIN); }
    public static Font caption() { return font(12, Font.PLAIN); }
    public static Font label() { return font(13, Font.BOLD); }

    public static void apply() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) { }
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("ScrollPane.border", BorderFactory.createEmptyBorder());
    }

    public static void primaryButton(JButton button) {
        button.setBackground(PRIMARY); button.setForeground(Color.WHITE); button.setFont(font(14, Font.BOLD));
        button.setBorder(BorderFactory.createEmptyBorder(11, 22, 11, 22)); button.setFocusPainted(false);
        button.setOpaque(true); button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        button.setBorderPainted(false);
    }

    public static void secondaryButton(JButton button) {
        button.setBackground(SURFACE); button.setForeground(TEXT); button.setFont(font(14, Font.BOLD));
        button.setBorder(BorderFactory.createLineBorder(BORDER, 1)); button.setFocusPainted(false);
        button.setOpaque(true); button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }

    public static void dangerButton(JButton button) {
        button.setBackground(ERROR); button.setForeground(Color.WHITE); button.setFont(font(14, Font.BOLD));
        button.setBorder(BorderFactory.createEmptyBorder(11, 22, 11, 22)); button.setFocusPainted(false);
        button.setOpaque(true); button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        button.setBorderPainted(false);
    }

    public static void ghostButton(JButton button) {
        button.setBackground(new Color(0,0,0,0)); button.setForeground(MUTED); button.setFont(font(13, Font.BOLD));
        button.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12)); button.setFocusPainted(false);
        button.setOpaque(false); button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }
}
