// Implements the desktop FoodOS checkout dialog screen using Swing components.
// Handles user interactions in the view while delegating business work.

package com.foodapp.view;

import com.foodapp.exception.AppException;
import com.foodapp.model.OrderDetail;
import com.foodapp.model.OrderSummary;
import com.foodapp.model.PaymentMethod;
import com.foodapp.service.OrderService;
import com.foodapp.util.SessionContext;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.*;

public class CheckoutDialog extends JDialog {
    private final OrderService orderService;
    private boolean success = false;
    private final JTextField addressField = Components.styledTextField();
    private final JTextField cityField = Components.styledTextField();
    private final JTextField postalField = Components.styledTextField();
    private final JComboBox<PaymentMethod> methodCombo = new JComboBox<>(PaymentMethod.values());
    private final JLabel statusLabel = new JLabel(" ");
    private static final NumberFormat MONEY = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public CheckoutDialog(JFrame owner, OrderService orderService) {
        super(owner, "Checkout", true);
        this.orderService = orderService;
        setSize(480, 420);
        setLocationRelativeTo(owner);
        buildUi();
    }

    private void buildUi() {
        JPanel panel = new JPanel(new BorderLayout(0, UiTheme.SPACING_MD));
        panel.setBackground(UiTheme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(UiTheme.SPACING_LG, UiTheme.SPACING_LG, UiTheme.SPACING_LG, UiTheme.SPACING_LG));

        JLabel title = Components.heading("Checkout", 22);
        panel.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UiTheme.SURFACE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER, 1),
                BorderFactory.createEmptyBorder(UiTheme.SPACING_LG, UiTheme.SPACING_LG, UiTheme.SPACING_LG, UiTheme.SPACING_LG)));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1; c.insets = new Insets(4, 0, 4, 0);
        addRow(form, "Delivery Address", addressField, c, 0);
        addRow(form, "City", cityField, c, 1);
        addRow(form, "Postal Code", postalField, c, 2);
        addRow(form, "Payment Method", methodCombo, c, 3);
        panel.add(form, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        statusLabel.setFont(UiTheme.body(13)); statusLabel.setForeground(UiTheme.ERROR);
        bottom.add(statusLabel, BorderLayout.WEST);
        JButton placeOrder = new JButton("Place Order"); UiTheme.primaryButton(placeOrder);
        placeOrder.addActionListener(e -> doCheckout(placeOrder));
        JButton cancel = new JButton("Cancel"); UiTheme.secondaryButton(cancel);
        cancel.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, UiTheme.SPACING_SM, 0));
        buttons.setOpaque(false); buttons.add(cancel); buttons.add(placeOrder);
        bottom.add(buttons, BorderLayout.EAST);
        panel.add(bottom, BorderLayout.SOUTH);

        setContentPane(panel);
    }

    private void addRow(JPanel panel, String label, Component field, GridBagConstraints base, int row) {
        GridBagConstraints c = (GridBagConstraints) base.clone(); c.gridy = row * 2;
        JLabel l = new JLabel(label); l.setFont(UiTheme.label()); l.setForeground(UiTheme.TEXT);
        panel.add(l, c);
        c.gridy = row * 2 + 1; c.insets = new Insets(2, 0, 8, 0);
        panel.add(field, c);
    }

    private void doCheckout(JButton button) {
        button.setEnabled(false);
        statusLabel.setText("Processing...");
        new SwingWorker<Long, Void>() {
            protected Long doInBackground() throws Exception {
                return orderService.checkout(SessionContext.currentUser().getId(), addressField.getText(), cityField.getText(), postalField.getText(), (PaymentMethod) methodCombo.getSelectedItem());
            }
            protected void done() {
                try {
                    long orderId = get();
                    success = true;
                    JOptionPane.showMessageDialog(CheckoutDialog.this,
                            "<html><b>Order placed successfully!</b><br><br>Order #" + orderId + "<br>Status: PLACED<br><br>You can track this order in My Orders.</html>",
                            "Order Confirmed", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } catch (Exception e) {
                    Throwable cause = e.getCause();
                    statusLabel.setText(cause instanceof AppException ? cause.getMessage() : "Checkout failed. No changes were saved.");
                    button.setEnabled(true);
                }
            }
        }.execute();
    }

    public boolean succeeded() { return success; }
}
