// Implements the desktop FoodOS components screen using Swing components.
// Handles user interactions in the view while delegating business work.

package com.foodapp.view;

import com.foodapp.model.OrderStatus;
import java.awt.*;
import javax.swing.*;

public final class Components {
    private Components() { }

    public static JPanel card() {
        JPanel panel = new JPanel();
        panel.setBackground(UiTheme.SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER, 1),
                BorderFactory.createEmptyBorder(UiTheme.SPACING_LG, UiTheme.SPACING_LG, UiTheme.SPACING_LG, UiTheme.SPACING_LG)));
        return panel;
    }

    public static JPanel card(int padding) {
        JPanel panel = new JPanel();
        panel.setBackground(UiTheme.SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER, 1),
                BorderFactory.createEmptyBorder(padding, padding, padding, padding)));
        return panel;
    }

    public static JLabel heading(String text, int size) {
        JLabel label = new JLabel(text);
        label.setFont(UiTheme.heading(size));
        label.setForeground(UiTheme.TEXT);
        return label;
    }

    public static JLabel body(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UiTheme.body(14));
        label.setForeground(UiTheme.MUTED);
        return label;
    }

    public static JLabel statValue(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UiTheme.heading(28));
        label.setForeground(UiTheme.TEXT);
        return label;
    }

    public static JLabel statLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UiTheme.caption());
        label.setForeground(UiTheme.MUTED);
        return label;
    }

    public static JPanel statCard(String title, String value, String caption) {
        JPanel panel = card();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(title); t.setFont(UiTheme.caption()); t.setForeground(UiTheme.MUTED); t.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel v = statValue(value); v.setAlignmentX(Component.LEFT_ALIGNMENT); v.setBorder(BorderFactory.createEmptyBorder(UiTheme.SPACING_SM, 0, UiTheme.SPACING_XS, 0));
        JLabel c = new JLabel(caption); c.setFont(UiTheme.caption()); c.setForeground(UiTheme.MUTED_LIGHT); c.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(t); panel.add(v); panel.add(c);
        return panel;
    }

    public static JLabel statusBadge(OrderStatus status) {
        Color bg, fg;
        switch (status) {
            case PLACED -> { bg = UiTheme.INFO_LIGHT; fg = UiTheme.INFO; }
            case ACCEPTED -> { bg = new Color(219,234,254); fg = new Color(29,78,216); }
            case PREPARING -> { bg = UiTheme.WARNING_LIGHT; fg = UiTheme.WARNING; }
            case READY -> { bg = new Color(254,243,199); fg = new Color(180,83,9); }
            case OUT_FOR_DELIVERY -> { bg = new Color(219,234,254); fg = new Color(30,64,175); }
            case DELIVERED -> { bg = UiTheme.SUCCESS_LIGHT; fg = UiTheme.SUCCESS; }
            case CANCELLED, REJECTED -> { bg = UiTheme.ERROR_LIGHT; fg = UiTheme.ERROR; }
            default -> { bg = UiTheme.BORDER_SOFT; fg = UiTheme.MUTED; }
        }
        JLabel label = new JLabel("  " + status.name().replace('_', ' ') + "  ");
        label.setFont(UiTheme.font(11, Font.BOLD));
        label.setForeground(fg);
        label.setBackground(bg);
        label.setOpaque(true);
        label.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        return label;
    }

    public static JTextField styledTextField() {
        JTextField field = new JTextField();
        field.setFont(UiTheme.body(14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        return field;
    }

    public static JPanel searchField(JTextField field, JButton button) {
        JPanel panel = new JPanel(new BorderLayout(UiTheme.SPACING_SM, 0));
        panel.setOpaque(false);
        field.setFont(UiTheme.body(14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER, 1),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        panel.add(field, BorderLayout.CENTER);
        if (button != null) { UiTheme.primaryButton(button); panel.add(button, BorderLayout.EAST); }
        return panel;
    }

    public static JPanel emptyState(String title, String subtitle) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(title, SwingConstants.CENTER); t.setFont(UiTheme.heading(18)); t.setForeground(UiTheme.TEXT); t.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel s = new JLabel(subtitle, SwingConstants.CENTER); s.setFont(UiTheme.body(14)); s.setForeground(UiTheme.MUTED); s.setAlignmentX(Component.CENTER_ALIGNMENT);
        s.setBorder(BorderFactory.createEmptyBorder(UiTheme.SPACING_SM, 0, 0, 0));
        panel.add(Box.createVerticalGlue()); panel.add(t); panel.add(s); panel.add(Box.createVerticalGlue());
        return panel;
    }

    public static JPanel loadingState(String text) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setOpaque(false);
        JProgressBar bar = new JProgressBar(); bar.setIndeterminate(true); bar.setPreferredSize(new Dimension(60, 16));
        JLabel label = new JLabel(text); label.setFont(UiTheme.body(14)); label.setForeground(UiTheme.MUTED);
        label.setBorder(BorderFactory.createEmptyBorder(0, UiTheme.SPACING_SM, 0, 0));
        panel.add(bar); panel.add(label);
        return panel;
    }

    public static JPanel pageContainer() {
        JPanel panel = new JPanel(new BorderLayout(0, UiTheme.SPACING_LG));
        panel.setBackground(UiTheme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(UiTheme.SPACING_LG, UiTheme.SPACING_XL, UiTheme.SPACING_LG, UiTheme.SPACING_XL));
        return panel;
    }

    public static JPanel pageHeader(String title, String subtitle) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JPanel left = new JPanel(); left.setOpaque(false); left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(heading(title, 24));
        if (subtitle != null) { JLabel s = body(subtitle); s.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0)); left.add(s); }
        panel.add(left, BorderLayout.WEST);
        return panel;
    }
}
