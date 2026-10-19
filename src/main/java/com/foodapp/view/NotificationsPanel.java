// Implements the desktop FoodOS notifications panel screen using Swing components.
// Handles user interactions in the view while delegating business work.

package com.foodapp.view;

import java.awt.*;
import javax.swing.*;

public class NotificationsPanel extends JPanel {
    public NotificationsPanel() {
        setBackground(UiTheme.BACKGROUND);
        setLayout(new BorderLayout(0, UiTheme.SPACING_MD));
        setBorder(BorderFactory.createEmptyBorder(UiTheme.SPACING_LG, UiTheme.SPACING_XL, UiTheme.SPACING_LG, UiTheme.SPACING_XL));
        add(Components.pageHeader("Notifications", "Recent alerts and updates"), BorderLayout.NORTH);

        JPanel list = new JPanel(); list.setOpaque(false); list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.add(Box.createVerticalStrut(UiTheme.SPACING_MD));

        list.add(notificationCard("New order received", "Order #1042 has been placed", UiTheme.INFO, UiTheme.INFO_LIGHT));
        list.add(Box.createVerticalStrut(UiTheme.SPACING_SM));
        list.add(notificationCard("Low stock alert", "Paneer Butter Masala is below reorder level (3 units)", UiTheme.WARNING, UiTheme.WARNING_LIGHT));
        list.add(Box.createVerticalStrut(UiTheme.SPACING_SM));
        list.add(notificationCard("Order completed", "Order #1038 has been delivered successfully", UiTheme.SUCCESS, UiTheme.SUCCESS_LIGHT));

        JScrollPane scroll = new JScrollPane(list); scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel notificationCard(String title, String message, Color fg, Color bg) {
        JPanel card = new JPanel(new BorderLayout(UiTheme.SPACING_MD, 0));
        card.setBackground(UiTheme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER, 1),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        JPanel dot = new JPanel(); dot.setBackground(bg); dot.setPreferredSize(new Dimension(4, 0));
        card.add(dot, BorderLayout.WEST);
        JPanel text = new JPanel(); text.setOpaque(false); text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(title); t.setFont(UiTheme.heading(14)); t.setForeground(fg); t.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel m = new JLabel(message); m.setFont(UiTheme.body(13)); m.setForeground(UiTheme.MUTED); m.setAlignmentX(Component.LEFT_ALIGNMENT);
        text.add(t); text.add(Box.createVerticalStrut(4)); text.add(m);
        card.add(text, BorderLayout.CENTER);
        return card;
    }
}
