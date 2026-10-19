// Implements the desktop FoodOS staff dashboard panel screen using Swing components.
// Handles user interactions in the view while delegating business work.

package com.foodapp.view;

import com.foodapp.util.Database;
import com.foodapp.util.SessionContext;
import java.awt.*;
import java.sql.*;
import javax.swing.*;

public class StaffDashboardPanel extends JPanel {
    private final Database database;
    private final AppFrame app;
    private final JLabel pendingValue = new JLabel("—");
    private final JLabel preparingValue = new JLabel("—");
    private final JLabel completedValue = new JLabel("—");
    private final JLabel revenueValue = new JLabel("—");
    private final DefaultListModel<String> lowStockModel = new DefaultListModel<>();
    private final JList<String> lowStockList = new JList<>(lowStockModel);
    private final JLabel status = new JLabel("Loading...");

    public StaffDashboardPanel(Database database, AppFrame app) {
        this.database = database; this.app = app;
        setBackground(UiTheme.BACKGROUND);
        setLayout(new BorderLayout(0, UiTheme.SPACING_LG));
        setBorder(BorderFactory.createEmptyBorder(UiTheme.SPACING_LG, UiTheme.SPACING_XL, UiTheme.SPACING_LG, UiTheme.SPACING_XL));
        buildUi();
    }

    private void buildUi() {
        add(Components.pageHeader("Staff Dashboard", "Today's operations at a glance"), BorderLayout.NORTH);

        JPanel center = new JPanel(); center.setOpaque(false); center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JPanel stats = new JPanel(new GridLayout(1, 4, UiTheme.SPACING_MD, 0));
        stats.setOpaque(false);
        stats.add(Components.statCard("Pending Orders", "—", "Awaiting acceptance"));
        stats.add(Components.statCard("Preparing", "—", "In the kitchen"));
        stats.add(Components.statCard("Completed Today", "—", "Delivered"));
        stats.add(Components.statCard("Revenue Today", "—", "From completed orders"));
        center.add(stats);
        center.add(Box.createVerticalStrut(UiTheme.SPACING_LG));

        JPanel lowStock = Components.card();
        lowStock.setLayout(new BoxLayout(lowStock, BoxLayout.Y_AXIS));
        JLabel title = Components.heading("Low Stock Alerts", 16); title.setAlignmentX(Component.LEFT_ALIGNMENT);
        lowStock.add(title); lowStock.add(Box.createVerticalStrut(UiTheme.SPACING_SM));
        lowStockList.setFixedCellHeight(28);
        lowStockList.setBorder(null);
        JScrollPane scroll = new JScrollPane(lowStockList); scroll.setBorder(null); scroll.setPreferredSize(new Dimension(0, 120));
        lowStock.add(scroll);
        center.add(lowStock);

        add(center, BorderLayout.CENTER);
        status.setFont(UiTheme.body(13)); status.setForeground(UiTheme.MUTED);
        add(status, BorderLayout.SOUTH);
    }

    public void refresh() {
        new SwingWorker<Void, Void>() {
            protected Void doInBackground() throws Exception {
                try (Connection conn = database.openConnection()) {
                    pendingValue.setText(String.valueOf(scalar(conn, "SELECT COUNT(*) FROM orders WHERE status = 'PLACED'")));
                    preparingValue.setText(String.valueOf(scalar(conn, "SELECT COUNT(*) FROM orders WHERE status IN ('ACCEPTED','PREPARING')")));
                    completedValue.setText(String.valueOf(scalar(conn, "SELECT COUNT(*) FROM orders WHERE status = 'DELIVERED' AND DATE(order_date) = CURRENT_DATE")));
                    var rev = scalar(conn, "SELECT COALESCE(SUM(total_amount),0) FROM orders WHERE status = 'DELIVERED' AND DATE(order_date) = CURRENT_DATE");
                    revenueValue.setText("₹" + rev);
                    loadLowStock(conn);
                }
                return null;
            }
            protected void done() {
                try { get(); updateStats(); status.setText("Dashboard refreshed"); status.setForeground(UiTheme.SUCCESS); }
                catch (Exception e) { status.setText("Unable to load dashboard data"); }
            }
        }.execute();
    }

    private void updateStats() {
        JPanel center = (JPanel) getComponent(1);
        JPanel stats = (JPanel) center.getComponent(0);
        stats.removeAll();
        stats.add(Components.statCard("Pending Orders", pendingValue.getText(), "Awaiting acceptance"));
        stats.add(Components.statCard("Preparing", preparingValue.getText(), "In the kitchen"));
        stats.add(Components.statCard("Completed Today", completedValue.getText(), "Delivered"));
        stats.add(Components.statCard("Revenue Today", revenueValue.getText(), "From completed orders"));
        stats.revalidate(); stats.repaint();
    }

    private void loadLowStock(Connection conn) throws SQLException {
        lowStockModel.clear();
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT mi.name, i.quantity_available, i.reorder_level FROM inventory i JOIN menu_items mi ON mi.item_id = i.item_id WHERE i.quantity_available <= i.reorder_level ORDER BY i.quantity_available ASC LIMIT 10")) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int qty = rs.getInt(2);
                    String marker = qty == 0 ? "OUT" : "LOW";
                    lowStockModel.addElement(rs.getString(1) + " — " + qty + " units (" + marker + ")");
                }
                if (lowStockModel.isEmpty()) lowStockModel.addElement("All stock levels are healthy");
            }
        }
    }

    private long scalar(Connection conn, String sql) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0;
        }
    }
}
