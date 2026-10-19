// Implements the desktop FoodOS register dialog screen using Swing components.
// Handles user interactions in the view while delegating business work.

package com.foodapp.view;

import com.foodapp.exception.AppException;
import com.foodapp.service.AuthenticationService;
import java.awt.*;
import javax.swing.*;

public class RegisterDialog extends JDialog {
    private final AuthenticationService service; private final JTextField name = new JTextField(); private final JTextField email = new JTextField(); private final JTextField phone = new JTextField(); private final JPasswordField password = new JPasswordField(); private final JLabel message = new JLabel(" "); private final JButton submit = new JButton("Create account");
    public RegisterDialog(Frame owner, AuthenticationService service) { super(owner, "Create customer account", true); this.service = service; setSize(420, 430); setLocationRelativeTo(owner); buildUi(); }
    private void buildUi() { JPanel panel = new JPanel(new GridBagLayout()); panel.setBackground(UiTheme.SURFACE); panel.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32)); GridBagConstraints c = new GridBagConstraints(); c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1; c.insets = new Insets(6, 0, 6, 0);
        add(panel, new JLabel("Create your account"), c, 0, true); addField(panel, c, 1, "Full name", name); addField(panel, c, 2, "Email", email); addField(panel, c, 3, "Phone", phone); addField(panel, c, 4, "Password (8+ characters)", password); message.setForeground(new Color(185,28,28)); add(panel, message, c, 5, false); UiTheme.primaryButton(submit); submit.addActionListener(e -> register()); add(panel, submit, c, 6, false); setContentPane(panel); getRootPane().setDefaultButton(submit); }
    private void addField(JPanel p, GridBagConstraints base, int row, String title, JComponent field) { add(p, new JLabel(title), base, row * 2 - 1, true); field.setFont(UiTheme.font(14, Font.PLAIN)); field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UiTheme.BORDER), BorderFactory.createEmptyBorder(8, 10, 8, 10))); add(p, field, base, row * 2, false); }
    private void add(JPanel p, Component component, GridBagConstraints base, int row, boolean heading) { GridBagConstraints c = (GridBagConstraints) base.clone(); c.gridy = row; if (heading) component.setFont(UiTheme.font(heading ? 22 : 13, heading ? Font.BOLD : Font.PLAIN)); p.add(component, c); }
    private void register() { submit.setEnabled(false); message.setText("Creating account..."); new SwingWorker<Void,Void>() { protected Void doInBackground() throws Exception { service.register(name.getText(), email.getText(), new String(password.getPassword()), phone.getText()); return null; } protected void done() { try { get(); JOptionPane.showMessageDialog(RegisterDialog.this, "Account created. You can now sign in.", "Account ready", JOptionPane.INFORMATION_MESSAGE); dispose(); } catch (Exception e) { Throwable cause = e.getCause(); message.setText(cause instanceof AppException ? cause.getMessage() : "Registration failed."); submit.setEnabled(true); } } }.execute(); }
}
