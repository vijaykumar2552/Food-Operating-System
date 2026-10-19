// Implements the desktop FoodOS settings panel screen using Swing components.
// Handles user interactions in the view while delegating business work.

package com.foodapp.view;

import com.foodapp.model.User;
import com.foodapp.service.AuthenticationService;
import com.foodapp.util.SessionContext;
import java.awt.*;
import javax.swing.*;

public class SettingsPanel extends JPanel {
    private final AuthenticationService auth;
    private final AppFrame app;
    private final JLabel nameLabel = new JLabel();
    private final JLabel emailLabel = new JLabel();
    private final JLabel roleLabel = new JLabel();

    public SettingsPanel(AuthenticationService auth, AppFrame app) {
        this.auth = auth; this.app = app;
        setBackground(UiTheme.BACKGROUND);
        setLayout(new BorderLayout(0, UiTheme.SPACING_MD));
        setBorder(BorderFactory.createEmptyBorder(UiTheme.SPACING_LG, UiTheme.SPACING_XL, UiTheme.SPACING_LG, UiTheme.SPACING_XL));
        buildUi();
    }

    private void buildUi() {
        add(Components.pageHeader("Settings", "Manage your profile and preferences"), BorderLayout.NORTH);

        JPanel center = new JPanel(); center.setOpaque(false); center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JPanel profile = Components.card();
        profile.setLayout(new BoxLayout(profile, BoxLayout.Y_AXIS));
        profile.add(Components.heading("Profile", 16));
        profile.add(Box.createVerticalStrut(UiTheme.SPACING_MD));

        JPanel info = new JPanel(new GridLayout(3, 2, UiTheme.SPACING_MD, UiTheme.SPACING_SM));
        info.setOpaque(false);
        info.add(rowLabel("Full Name")); info.add(nameLabel);
        info.add(rowLabel("Email")); info.add(emailLabel);
        info.add(rowLabel("Role")); info.add(roleLabel);
        profile.add(info);
        center.add(profile);

        center.add(Box.createVerticalStrut(UiTheme.SPACING_MD));

        JPanel prefs = Components.card();
        prefs.setLayout(new BoxLayout(prefs, BoxLayout.Y_AXIS));
        prefs.add(Components.heading("Preferences", 16));
        prefs.add(Box.createVerticalStrut(UiTheme.SPACING_SM));
        JCheckBox darkMode = new JCheckBox("Dark mode (coming soon)");
        darkMode.setOpaque(false); darkMode.setForeground(UiTheme.MUTED); darkMode.setFont(UiTheme.body(14));
        darkMode.setEnabled(false);
        prefs.add(darkMode);
        center.add(prefs);

        center.add(Box.createVerticalStrut(UiTheme.SPACING_MD));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actions.setOpaque(false);
        JButton logout = new JButton("Sign Out"); UiTheme.dangerButton(logout);
        logout.addActionListener(e -> app.logout());
        actions.add(logout);
        center.add(actions);

        add(center, BorderLayout.CENTER);
    }

    private JLabel rowLabel(String text) {
        JLabel l = new JLabel(text); l.setFont(UiTheme.caption()); l.setForeground(UiTheme.MUTED);
        return l;
    }

    public void refresh() {
        User user = SessionContext.currentUser();
        if (user != null) {
            nameLabel.setText(user.getFullName()); nameLabel.setFont(UiTheme.body(14)); nameLabel.setForeground(UiTheme.TEXT);
            emailLabel.setText(user.getEmail()); emailLabel.setFont(UiTheme.body(14)); emailLabel.setForeground(UiTheme.TEXT);
            roleLabel.setText(user.getRole().name().replace('_', ' ')); roleLabel.setFont(UiTheme.body(14)); roleLabel.setForeground(UiTheme.TEXT);
        }
    }
}
