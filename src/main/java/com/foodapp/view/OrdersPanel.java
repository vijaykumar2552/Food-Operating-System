// Implements the desktop FoodOS orders panel screen using Swing components.
// Handles user interactions in the view while delegating business work.

package com.foodapp.view;

import com.foodapp.exception.AppException;
import com.foodapp.model.OrderDetail;
import com.foodapp.model.OrderSummary;
import com.foodapp.service.OrderService;
import com.foodapp.util.SessionContext;
import java.awt.*;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javax.swing.*;

public class OrdersPanel extends JPanel {
    private final OrderService orderService;
    private final AppFrame app;
    private final DefaultListModel<OrderSummary> orderModel = new DefaultListModel<>();
    private final JList<OrderSummary> orderList = new JList<>(orderModel);
    private final JLabel statusLabel = new JLabel(" ");
    private static final NumberFormat MONEY = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    public OrdersPanel(OrderService os, AppFrame app) {
        this.orderService = os; this.app = app;
        setBackground(UiTheme.BACKGROUND);
        setLayout(new BorderLayout(0, UiTheme.SPACING_MD));
        setBorder(BorderFactory.createEmptyBorder(UiTheme.SPACING_LG, UiTheme.SPACING_XL, UiTheme.SPACING_LG, UiTheme.SPACING_XL));
        buildUi();
    }

    private void buildUi() {
        add(Components.pageHeader("My Orders", "Track your orders and view details"), BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(orderList);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        orderList.setCellRenderer(new OrderRenderer());
        add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        statusLabel.setFont(UiTheme.body(13)); statusLabel.setForeground(UiTheme.MUTED);
        bottom.add(statusLabel, BorderLayout.WEST);
        JButton detailsBtn = new JButton("View Details"); UiTheme.primaryButton(detailsBtn);
        detailsBtn.addActionListener(e -> showDetails());
        bottom.add(detailsBtn, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);
    }

    public void refresh() {
        statusLabel.setText("Loading orders...");
        new SwingWorker<List<OrderSummary>, Void>() {
            protected List<OrderSummary> doInBackground() throws Exception { return orderService.history(SessionContext.currentUser().getId()); }
            protected void done() {
                try {
                    orderModel.clear();
                    for (OrderSummary o : get()) orderModel.addElement(o);
                    statusLabel.setText(orderModel.isEmpty() ? "No orders yet" : orderModel.size() + " order(s)");
                } catch (Exception e) { statusLabel.setText("Unable to load orders"); }
            }
        }.execute();
    }

    private void showDetails() {
        OrderSummary selected = orderList.getSelectedValue();
        if (selected == null) return;
        new SwingWorker<OrderDetail, Void>() {
            protected OrderDetail doInBackground() throws Exception { return orderService.detail(SessionContext.currentUser().getId(), selected.id()); }
            protected void done() {
                try {
                    OrderDetail detail = get();
                    new OrderDetailDialog((JFrame) SwingUtilities.getWindowAncestor(OrdersPanel.this), detail).setVisible(true);
                } catch (Exception e) { statusLabel.setText("Unable to load order details"); }
            }
        }.execute();
    }

    private static class OrderRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, selected, focus);
            OrderSummary order = (OrderSummary) value;
            label.setText("<html><b style='font-size:14px'>Order #" + order.id() + " · " + order.restaurantName() + "</b><br><font color='#6B7280' size='2'>" + order.orderDate().format(DATE_FMT) + " · " + MONEY.format(order.totalAmount()) + "</font></html>");
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, UiTheme.BORDER_SOFT),
                    BorderFactory.createEmptyBorder(12, 14, 12, 14)));
            if (selected) label.setBackground(UiTheme.PRIMARY_LIGHT);
            return label;
        }
    }
}
