// Implements the desktop FoodOS login frame screen using Swing components.
// Handles user interactions in the view while delegating business work.

package com.foodapp.view;

import com.foodapp.exception.AppException;
import com.foodapp.model.User;
import com.foodapp.service.AuthenticationService;
import com.foodapp.util.SessionContext;
import java.awt.*;
import javax.swing.*;

public class LoginFrame extends JFrame {
    private final AuthenticationService authenticationService;
    private final JTextField emailField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JLabel messageLabel = new JLabel(" ");
    private final JButton loginButton = new JButton("Sign in");

    public LoginFrame(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
        setTitle("FoodOS | Sign in"); setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); setSize(880, 560);
        setLocationRelativeTo(null); setMinimumSize(new Dimension(720, 480)); buildUi();
    }

    private void buildUi() {
        JPanel root = new JPanel(new GridLayout(1, 2)); root.setBackground(UiTheme.BACKGROUND);
        JPanel brand = new JPanel(); brand.setBackground(new Color(20, 43, 82)); brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS)); brand.setBorder(BorderFactory.createEmptyBorder(80, 56, 40, 56));
        JLabel mark = new JLabel("FOODFLOW"); mark.setForeground(new Color(147, 197, 253)); mark.setFont(UiTheme.font(15, Font.BOLD));
        JLabel title = new JLabel("Good food,\nwell managed."); title.setText("<html>Good food,<br>well managed.</html>"); title.setForeground(Color.WHITE); title.setFont(UiTheme.font(36, Font.BOLD)); title.setBorder(BorderFactory.createEmptyBorder(32, 0, 18, 0));
        JLabel copy = new JLabel("<html>A focused workspace for customers,<br>restaurants and operations teams.</html>"); copy.setForeground(new Color(219, 234, 254)); copy.setFont(UiTheme.font(15, Font.PLAIN)); brand.add(mark); brand.add(title); brand.add(copy);

        JPanel form = new JPanel(new GridBagLayout()); form.setBackground(UiTheme.SURFACE); form.setBorder(BorderFactory.createEmptyBorder(45, 60, 45, 60));
        GridBagConstraints c = new GridBagConstraints(); c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1; c.insets = new Insets(7, 0, 7, 0);
        JLabel heading = new JLabel("Welcome back"); heading.setFont(UiTheme.font(28, Font.BOLD)); heading.setForeground(UiTheme.TEXT); add(form, heading, c, 0);
        JLabel subheading = new JLabel("Sign in to continue to your workspace"); subheading.setFont(UiTheme.font(14, Font.PLAIN)); subheading.setForeground(UiTheme.MUTED); add(form, subheading, c, 1);
        add(form, label("Email address"), c, 3); styleField(emailField); add(form, emailField, c, 4);
        add(form, label("Password"), c, 5); styleField(passwordField); add(form, passwordField, c, 6);
        JCheckBox showPassword = new JCheckBox("Show password"); showPassword.setBackground(UiTheme.SURFACE); showPassword.setForeground(UiTheme.MUTED); showPassword.addActionListener(e -> passwordField.setEchoChar(showPassword.isSelected() ? (char) 0 : '•')); add(form, showPassword, c, 7);
        messageLabel.setForeground(new Color(185, 28, 28)); messageLabel.setFont(UiTheme.font(13, Font.PLAIN)); add(form, messageLabel, c, 8);
        UiTheme.primaryButton(loginButton); loginButton.addActionListener(e -> login()); add(form, loginButton, c, 9);
        JButton register = new JButton("Create a customer account"); UiTheme.secondaryButton(register); register.addActionListener(e -> new RegisterDialog(this, authenticationService).setVisible(true)); add(form, register, c, 10);
        root.add(brand); root.add(form); setContentPane(root); getRootPane().setDefaultButton(loginButton);
    }

    private JLabel label(String text) { JLabel label = new JLabel(text); label.setFont(UiTheme.font(13, Font.BOLD)); label.setForeground(UiTheme.TEXT); return label; }
    private void styleField(JTextField field) { field.setFont(UiTheme.font(15, Font.PLAIN)); field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UiTheme.BORDER), BorderFactory.createEmptyBorder(10, 12, 10, 12))); }
    private void add(JPanel panel, Component component, GridBagConstraints base, int row) { GridBagConstraints c = (GridBagConstraints) base.clone(); c.gridy = row; panel.add(component, c); }

    private void login() {
        String email = emailField.getText(); String password = new String(passwordField.getPassword());
        loginButton.setEnabled(false); messageLabel.setText("Signing in...");
        new SwingWorker<User, Void>() {
            protected User doInBackground() throws Exception { return authenticationService.login(email, password); }
            protected void done() { try { SessionContext.signIn(get()); dispose(); new AppFrame(authenticationService).setVisible(true); } catch (Exception exception) { Throwable cause = exception.getCause(); messageLabel.setText(cause instanceof AppException ? cause.getMessage() : "Could not sign in. Check the database connection."); loginButton.setEnabled(true); } }
        }.execute();
    }
}
