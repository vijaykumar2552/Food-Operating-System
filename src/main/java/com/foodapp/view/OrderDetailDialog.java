// Implements the desktop FoodOS order detail dialog screen using Swing components.
// Handles user interactions in the view while delegating business work.

package com.foodapp.view;

import com.foodapp.model.OrderDetail;
import com.foodapp.model.OrderStatus;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.*;

public class OrderDetailDialog extends JDialog {
    private static final NumberFormat MONEY = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public OrderDetailDialog(JFrame owner, OrderDetail detail) {
        super(owner, "Order #" + detail.id(), true);
        setSize(520, 580);
        setLocationRelativeTo(owner);
        buildUi(detail);
    }

    private void buildUi(OrderDetail detail) {
        JPanel panel = new JPanel(new BorderLayout(0, UiTheme.SPACING_MD));
        panel.setBackground(UiTheme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(UiTheme.SPACING_LG, UiTheme.SPACING_LG, UiTheme.SPACING_LG, UiTheme.SPACING_LG));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = Components.heading("Order #" + detail.id(), 22);
        header.add(title, BorderLayout.WEST);
        JLabel badge = Components.statusBadge(detail.status());
        header.add(badge, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(); body.setOpaque(false); body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JPanel info = Components.card();
        info.setLayout(new GridLayout(2, 2, UiTheme.SPACING_MD, UiTheme.SPACING_SM));
        info.add(infoLabel("Restaurant", detail.restaurantName()));
        info.add(infoLabel("Date", detail.orderDate().format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))));
        info.add(infoLabel("Status", detail.status().name().replace('_', ' ')));
        info.add(infoLabel("Payment", "Simulated — Success"));
        body.add(info);
        body.add(Box.createVerticalStrut(UiTheme.SPACING_MD));

        JPanel timeline = Components.card();
        timeline.setLayout(new BoxLayout(timeline, BoxLayout.Y_AXIS));
        timeline.add(Components.heading("Status Timeline", 14));
        timeline.add(Box.createVerticalStrut(UiTheme.SPACING_SM));
        OrderStatus[] flow = {OrderStatus.PLACED, OrderStatus.ACCEPTED, OrderStatus.PREPARING, OrderStatus.READY, OrderStatus.OUT_FOR_DELIVERY, OrderStatus.DELIVERED};
        for (OrderStatus s : flow) {
            boolean reached = detail.status().ordinal() >= s.ordinal();
            JLabel step = new JLabel((reached ? "●  " : "○  ") + s.name().replace('_', ' '));
            step.setFont(UiTheme.body(13)); step.setForeground(reached ? UiTheme.TEXT : UiTheme.MUTED_LIGHT);
            timeline.add(step); timeline.add(Box.createVerticalStrut(2));
        }
        body.add(timeline);
        body.add(Box.createVerticalStrut(UiTheme.SPACING_MD));

        JPanel items = Components.card();
        items.setLayout(new BoxLayout(items, BoxLayout.Y_AXIS));
        items.add(Components.heading("Items", 14));
        items.add(Box.createVerticalStrut(UiTheme.SPACING_SM));
        for (OrderDetail.OrderLine line : detail.items()) {
            JPanel row = new JPanel(new BorderLayout());
            row.setOpaque(false);
            JLabel name = new JLabel(line.name() + " × " + line.quantity()); name.setFont(UiTheme.body(13)); name.setForeground(UiTheme.TEXT);
            JLabel price = new JLabel(MONEY.format(line.lineTotal())); price.setFont(UiTheme.body(13)); price.setForeground(UiTheme.TEXT);
            row.add(name, BorderLayout.WEST); row.add(price, BorderLayout.EAST);
            items.add(row); items.add(Box.createVerticalStrut(4));
        }
        items.add(Box.createVerticalStrut(UiTheme.SPACING_SM));
        items.add(dividerRow("Subtotal", MONEY.format(detail.subtotal())));
        if (detail.discount().signum() > 0) items.add(dividerRow("Discount", "-" + MONEY.format(detail.discount())));
        if (detail.deliveryFee().signum() > 0) items.add(dividerRow("Delivery Fee", MONEY.format(detail.deliveryFee())));
        items.add(Box.createVerticalStrut(UiTheme.SPACING_SM));
        JLabel totalRow = new JLabel("Total: " + MONEY.format(detail.totalAmount()));
        totalRow.setFont(UiTheme.heading(16)); totalRow.setForeground(UiTheme.TEXT);
        items.add(totalRow);
        body.add(items);

        panel.add(body, BorderLayout.CENTER);

        JButton close = new JButton("Close"); UiTheme.secondaryButton(close);
        close.addActionListener(e -> dispose());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT)); bottom.setOpaque(false); bottom.add(close);
        panel.add(bottom, BorderLayout.SOUTH);

        setContentPane(panel);
    }

    private JPanel infoLabel(String label, String value) {
        JPanel p = new JPanel(); p.setOpaque(false); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel l = new JLabel(label); l.setFont(UiTheme.caption()); l.setForeground(UiTheme.MUTED);
        JLabel v = new JLabel(value); v.setFont(UiTheme.body(14)); v.setForeground(UiTheme.TEXT);
        p.add(l); p.add(Box.createVerticalStrut(2)); p.add(v);
        return p;
    }

    private JPanel dividerRow(String label, String value) {
        JPanel p = new JPanel(new BorderLayout()); p.setOpaque(false);
        JLabel l = new JLabel(label); l.setFont(UiTheme.body(13)); l.setForeground(UiTheme.MUTED);
        JLabel v = new JLabel(value); v.setFont(UiTheme.body(13)); v.setForeground(UiTheme.TEXT);
        p.add(l, BorderLayout.WEST); p.add(v, BorderLayout.EAST);
        return p;
    }
}
