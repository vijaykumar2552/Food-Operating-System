// Implements the desktop FoodOS analytics panel screen using Swing components.
// Handles user interactions in the view while delegating business work.

package com.foodapp.view;

import com.foodapp.util.Database;
import java.awt.*;
import java.sql.*;
import javax.swing.*;

public class AnalyticsPanel extends JPanel {
    private final Database database;

    public AnalyticsPanel(Database database, AppFrame app) {
        this.database = database;
        setBackground(UiTheme.BACKGROUND);
        setLayout(new BorderLayout(0, UiTheme.SPACING_LG));
        setBorder(BorderFactory.createEmptyBorder(UiTheme.SPACING_LG, UiTheme.SPACING_XL, UiTheme.SPACING_LG, UiTheme.SPACING_XL));
        buildUi();
    }

    private void buildUi() {
        add(Components.pageHeader("Analytics", "Performance insights and trends"), BorderLayout.NORTH);

        JPanel center = new JPanel(); center.setOpaque(false); center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JPanel stats = new JPanel(new GridLayout(1, 4, UiTheme.SPACING_MD, 0));
        stats.setOpaque(false);
        stats.add(Components.statCard("Total Orders", "—", "All time"));
        stats.add(Components.statCard("Completed", "—", "Delivered orders"));
        stats.add(Components.statCard("Revenue", "—", "From delivered orders"));
        stats.add(Components.statCard("Avg Order Value", "—", "Per delivered order"));
        center.add(stats);
        center.add(Box.createVerticalStrut(UiTheme.SPACING_LG));

        JPanel statusDist = Components.card();
        statusDist.setLayout(new BoxLayout(statusDist, BoxLayout.Y_AXIS));
        statusDist.add(Components.heading("Order Status Distribution", 16));
        statusDist.add(Box.createVerticalStrut(UiTheme.SPACING_SM));
        JLabel distLabel = new JLabel(" "); distLabel.setFont(UiTheme.body(13)); distLabel.setForeground(UiTheme.MUTED);
        statusDist.add(distLabel);
        center.add(statusDist);

        center.add(Box.createVerticalStrut(UiTheme.SPACING_MD));

        JPanel topItems = Components.card();
        topItems.setLayout(new BoxLayout(topItems, BoxLayout.Y_AXIS));
        topItems.add(Components.heading("Top-Selling Items", 16));
        topItems.add(Box.createVerticalStrut(UiTheme.SPACING_SM));
        DefaultListModel<String> topModel = new DefaultListModel<>();
        JList<String> topList = new JList<>(topModel);
        topList.setFixedCellHeight(26); topList.setBorder(null);
        JScrollPane scroll = new JScrollPane(topList); scroll.setBorder(null); scroll.setPreferredSize(new Dimension(0, 140));
        topItems.add(scroll);
        center.add(topItems);

        add(center, BorderLayout.CENTER);

        putClientProperty("distLabel", distLabel);
        putClientProperty("topModel", topModel);
        putClientProperty("statsPanel", stats);
    }

    public void refresh() {
        new SwingWorker<Void, Void>() {
            protected Void doInBackground() throws Exception {
                try (Connection conn = database.openConnection()) {
                    long total = scalar(conn, "SELECT COUNT(*) FROM orders");
                    long completed = scalar(conn, "SELECT COUNT(*) FROM orders WHERE status = 'DELIVERED'");
                    long revenue = scalar(conn, "SELECT COALESCE(SUM(total_amount),0) FROM orders WHERE status = 'DELIVERED'");
                    long avg = completed > 0 ? revenue / completed : 0;
                    publishStats(total, completed, revenue, avg);

                    JLabel distLabel = (JLabel) getClientProperty("distLabel");
                    StringBuilder sb = new StringBuilder("<html>");
                    try (PreparedStatement stmt = conn.prepareStatement("SELECT status, COUNT(*) FROM orders GROUP BY status ORDER BY COUNT(*) DESC")) {
                        try (ResultSet rs = stmt.executeQuery()) {
                            while (rs.next()) sb.append(rs.getString(1).replace('_',' ')).append(": ").append(rs.getInt(2)).append("   ");
                        }
                    }
                    sb.append("</html>");
                    SwingUtilities.invokeLater(() -> distLabel.setText(sb.toString()));

                    DefaultListModel<String> topModel = (DefaultListModel<String>) getClientProperty("topModel");
                    topModel.clear();
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "SELECT mi.name, SUM(oi.quantity) AS total_qty FROM order_items oi JOIN menu_items mi ON mi.item_id = oi.item_id GROUP BY mi.item_id ORDER BY total_qty DESC LIMIT 10")) {
                        try (ResultSet rs = stmt.executeQuery()) {
                            while (rs.next()) topModel.addElement(rs.getString(1) + " — " + rs.getInt(2) + " units");
                        }
                    }
                }
                return null;
            }
            protected void done() { }
        }.execute();
    }

    private void publishStats(long total, long completed, long revenue, long avg) {
        SwingUtilities.invokeLater(() -> {
            JPanel stats = (JPanel) getClientProperty("statsPanel");
            stats.removeAll();
            stats.add(Components.statCard("Total Orders", String.valueOf(total), "All time"));
            stats.add(Components.statCard("Completed", String.valueOf(completed), "Delivered orders"));
            stats.add(Components.statCard("Revenue", "₹" + revenue, "From delivered orders"));
            stats.add(Components.statCard("Avg Order Value", "₹" + avg, "Per delivered order"));
            stats.revalidate(); stats.repaint();
        });
    }

    private long scalar(Connection conn, String sql) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0;
        }
    }
}
