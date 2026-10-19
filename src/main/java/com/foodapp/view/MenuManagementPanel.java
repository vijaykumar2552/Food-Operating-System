// Implements the desktop FoodOS menu management panel screen using Swing components.
// Handles user interactions in the view while delegating business work.

package com.foodapp.view;

import com.foodapp.util.Database;
import com.foodapp.util.SessionContext;
import java.awt.*;
import java.sql.*;
import javax.swing.*;

public class MenuManagementPanel extends JPanel {
    private final Database database;
    private final AppFrame app;
    private final DefaultListModel<String> menuModel = new DefaultListModel<>();
    private final JList<String> menuList = new JList<>(menuModel);
    private final JLabel status = new JLabel(" ");
    private final JTextField search = new JTextField();

    public MenuManagementPanel(Database database, AppFrame app) {
        this.database = database; this.app = app;
        setBackground(UiTheme.BACKGROUND);
        setLayout(new BorderLayout(0, UiTheme.SPACING_MD));
        setBorder(BorderFactory.createEmptyBorder(UiTheme.SPACING_LG, UiTheme.SPACING_XL, UiTheme.SPACING_LG, UiTheme.SPACING_XL));
        buildUi();
    }

    private void buildUi() {
        add(Components.pageHeader("Menu Management", "Manage your restaurant menu items"), BorderLayout.NORTH);

        JPanel topBar = new JPanel(new BorderLayout(UiTheme.SPACING_MD, 0));
        topBar.setOpaque(false);
        JButton searchBtn = new JButton("Search");
        topBar.add(Components.searchField(search, searchBtn), BorderLayout.CENTER);
        JButton refresh = new JButton("Refresh"); UiTheme.secondaryButton(refresh);
        topBar.add(refresh, BorderLayout.EAST);
        searchBtn.addActionListener(e -> refresh()); refresh.addActionListener(e -> refresh());
        add(topBar, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(menuList);
        scroll.setBorder(null); scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        status.setFont(UiTheme.body(13)); status.setForeground(UiTheme.MUTED);
        bottom.add(status, BorderLayout.WEST);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, UiTheme.SPACING_SM, 0));
        actions.setOpaque(false);
        JButton toggleAvail = new JButton("Toggle Availability"); UiTheme.secondaryButton(toggleAvail); toggleAvail.addActionListener(e -> toggleAvailability());
        JButton updatePrice = new JButton("Update Price"); UiTheme.secondaryButton(updatePrice); updatePrice.addActionListener(e -> updatePrice());
        actions.add(toggleAvail); actions.add(updatePrice);
        bottom.add(actions, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);
    }

    public void refresh() {
        status.setText("Loading menu items...");
        new SwingWorker<Void, Void>() {
            protected Void doInBackground() throws Exception {
                menuModel.clear();
                try (Connection conn = database.openConnection()) {
                    String sql = "SELECT mi.item_id, mi.name, mi.price, mi.availability, i.quantity_available, mc.name AS category FROM menu_items mi JOIN menu_categories mc ON mc.category_id = mi.category_id LEFT JOIN inventory i ON i.item_id = mi.item_id ORDER BY mc.name, mi.name";
                    try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            String avail = "AVAILABLE".equals(rs.getString(4)) ? "Available" : "Unavailable";
                            menuModel.addElement(rs.getString(2) + " — ₹" + rs.getBigDecimal(3) + " · " + rs.getString(6) + " · " + avail + " · Stock: " + rs.getInt(5) + " [ID:" + rs.getLong(1) + "]");
                        }
                    }
                }
                return null;
            }
            protected void done() {
                try { get(); status.setText(menuModel.isEmpty() ? "No menu items found" : menuModel.size() + " menu items"); }
                catch (Exception e) { status.setText("Unable to load menu"); }
            }
        }.execute();
    }

    private void toggleAvailability() {
        String selected = menuList.getSelectedValue();
        if (selected == null) { status.setText("Select an item first"); return; }
        long itemId = extractId(selected);
        new SwingWorker<Void, Void>() {
            protected Void doInBackground() throws Exception {
                try (Connection conn = database.openConnection()) {
                    try (PreparedStatement stmt = conn.prepareStatement("UPDATE menu_items SET availability = CASE WHEN availability = 'AVAILABLE' THEN 'SOLD_OUT' ELSE 'AVAILABLE' END WHERE item_id = ?")) {
                        stmt.setLong(1, itemId); stmt.executeUpdate();
                    }
                }
                return null;
            }
            protected void done() { refresh(); }
        }.execute();
    }

    private void updatePrice() {
        String selected = menuList.getSelectedValue();
        if (selected == null) { status.setText("Select an item first"); return; }
        String val = JOptionPane.showInputDialog(this, "New price:", "Update Price", JOptionPane.QUESTION_MESSAGE);
        if (val == null) return;
        try {
            double price = Double.parseDouble(val);
            if (price <= 0) throw new NumberFormatException();
            long itemId = extractId(selected);
            new SwingWorker<Void, Void>() {
                protected Void doInBackground() throws Exception {
                    try (Connection conn = database.openConnection()) {
                        try (PreparedStatement stmt = conn.prepareStatement("UPDATE menu_items SET price = ? WHERE item_id = ?")) {
                            stmt.setBigDecimal(1, java.math.BigDecimal.valueOf(price));
                            stmt.setLong(2, itemId); stmt.executeUpdate();
                        }
                    }
                    return null;
                }
                protected void done() { refresh(); }
            }.execute();
        } catch (NumberFormatException e) { JOptionPane.showMessageDialog(this, "Enter a valid positive price", "Invalid Price", JOptionPane.WARNING_MESSAGE); }
    }

    private long extractId(String entry) { return Long.parseLong(entry.substring(entry.lastIndexOf("[ID:") + 4, entry.lastIndexOf("]"))); }
}
