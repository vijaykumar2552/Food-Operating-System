// Implements the desktop FoodOS sidebar screen using Swing components.
// Handles user interactions in the view while delegating business work.

package com.foodapp.view;

import com.foodapp.model.User;
import com.foodapp.util.SessionContext;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;

public final class Sidebar extends JPanel {
    public interface NavigationListener { void navigate(String page); void logout(); }

    private final NavigationListener listener;
    private final Map<String, JButton> buttons = new LinkedHashMap<>();
    private String activePage;

    public Sidebar(NavigationListener listener, String[] pages) {
        this.listener = listener;
        setPreferredSize(new Dimension(232, 0));
        setBackground(UiTheme.SIDEBAR_BG);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(24, 16, 16, 16));

        JLabel logo = new JLabel("FOODFLOW");
        logo.setForeground(new Color(147, 197, 253));
        logo.setFont(UiTheme.font(14, Font.BOLD));
        logo.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        add(logo);
        add(Box.createVerticalStrut(6));
        JLabel tagline = new JLabel("Food ordering system");
        tagline.setForeground(new Color(75, 85, 99));
        tagline.setFont(UiTheme.font(11, Font.PLAIN));
        tagline.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        add(tagline);
        add(Box.createVerticalStrut(28));

        for (String page : pages) {
            JButton button = createNavButton(page);
            buttons.put(page, button);
            add(button);
            add(Box.createVerticalStrut(2));
        }

        add(Box.createVerticalGlue());

        User user = SessionContext.currentUser();
        if (user != null) {
            JPanel userInfo = new JPanel();
            userInfo.setOpaque(false);
            userInfo.setLayout(new BoxLayout(userInfo, BoxLayout.Y_AXIS));
            JLabel name = new JLabel("  " + user.getFullName());
            name.setForeground(new Color(209, 213, 219));
            name.setFont(UiTheme.font(13, Font.BOLD));
            JLabel role = new JLabel("  " + user.getRole().name().replace('_', ' '));
            role.setForeground(new Color(107, 114, 128));
            role.setFont(UiTheme.font(11, Font.PLAIN));
            userInfo.add(name); userInfo.add(role);
            userInfo.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(31, 41, 55)));
            userInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(userInfo);
            add(Box.createVerticalStrut(12));
        }

        JButton logout = createNavButton("Sign out");
        logout.setForeground(new Color(239, 68, 68));
        logout.addActionListener(e -> listener.logout());
        add(logout);
    }

    private JButton createNavButton(String page) {
        JButton button = new JButton(page);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setForeground(new Color(156, 163, 175));
        button.setBackground(UiTheme.SIDEBAR_BG);
        button.setBorder(BorderFactory.createEmptyBorder(11, 16, 11, 16));
        button.setFocusPainted(false);
        button.setFont(UiTheme.font(14, Font.BOLD));
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(232, 42));
        button.addActionListener(e -> { setActive(page); listener.navigate(page); });
        return button;
    }

    public void setActive(String page) {
        activePage = page;
        for (Map.Entry<String, JButton> entry : buttons.entrySet()) {
            if (entry.getKey().equals(page)) {
                entry.getValue().setBackground(UiTheme.SIDEBAR_ACTIVE);
                entry.getValue().setForeground(Color.WHITE);
            } else {
                entry.getValue().setBackground(UiTheme.SIDEBAR_BG);
                entry.getValue().setForeground(new Color(156, 163, 175));
            }
        }
    }

    public String activePage() { return activePage; }
}
