// Implements the desktop FoodOS inventory panel screen using Swing components.
// Handles user interactions in the view while delegating business work.

package com.foodapp.view;

import com.foodapp.util.Database;
import java.awt.*;
import java.sql.*;
import javax.swing.*;

public class InventoryPanel extends JPanel {
    private final Database database;
    private final DefaultListModel<String> invModel = new DefaultListModel<>();
    private final JList<String> invList = new JList<>(invModel);
    private final JLabel status = new JLabel(" ");

    public InventoryPanel(Database database, AppFrame app) {
        this.database = database;
        setBackground(UiTheme.BACKGROUND);
        setLayout(new BorderLayout(0, UiTheme.SPACING_MD));
        setBorder(BorderFactory.createEmptyBorder(UiTheme.SPACING_LG, UiTheme.SPACING_XL, UiTheme.SPACING_LG, UiTheme.SPACING_XL));
        buildUi();
    }

    private void buildUi() {
        add(Components.pageHeader("Inventory", "Track and update stock levels"), BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(invList);
        scroll.setBorder(null); scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        status.setFont(UiTheme.body(13)); status.setForeground(UiTheme.MUTED);
        bottom.add(status, BorderLayout.WEST);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, UiTheme.SPACING_SM, 0));
        actions.setOpaque(false);
        JButton updateBtn = new JButton("Update Stock"); UiTheme.primaryButton(updateBtn); updateBtn.addActionListener(e -> updateStock());
        JButton availBtn = new JButton("Mark Available"); UiTheme.secondaryButton(availBtn); availBtn.addActionListener(e -> setAvailability(true));
        JButton unavailBtn = new JButton("Mark Unavailable"); UiTheme.secondaryButton(unavailBtn); unavailBtn.addActionListener(e -> setAvailability(false));
        actions.add(unavailBtn); actions.add(availBtn); actions.add(updateBtn);
        bottom.add(actions, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);
    }

    public void refresh() {
        status.setText("Loading inventory...");
        new SwingWorker<Void, Void>() {
            protected Void doInBackground() throws Exception {
                invModel.clear();
                try (Connection conn = database.openConnection()) {
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "SELECT mi.item_id, mi.name, i.quantity_available, i.reorder_level, mi.availability FROM inventory i JOIN menu_items mi ON mi.item_id = i.item_id ORDER BY i.quantity_available ASC")) {
                        try (ResultSet rs = stmt.executeQuery()) {
                            while (rs.next()) {
                                String flag = rs.getInt(3) <= rs.getInt(4) ? " [LOW]" : "";
                                String avail = "AVAILABLE".equals(rs.getString(5)) ? "" : " [UNAVAILABLE]";
                                invModel.addElement(rs.getString(2) + " — Stock: " + rs.getInt(3) + flag + avail + " [ID:" + rs.getLong(1) + "]");
                            }
                        }
                    }
                }
                return null;
            }
            protected void done() {
                try { get(); status.setText(invModel.isEmpty() ? "No inventory records" : invModel.size() + " items in inventory"); }
                catch (Exception e) { status.setText("Unable to load inventory"); }
            }
        }.execute();
    }

    private void updateStock() {
        String selected = invList.getSelectedValue();
        if (selected == null) { status.setText("Select an item first"); return; }
        String val = JOptionPane.showInputDialog(this, "New stock quantity:", "Update Stock", JOptionPane.QUESTION_MESSAGE);
        if (val == null) return;
        try {
            int qty = Integer.parseInt(val);
            if (qty < 0) throw new NumberFormatException();
            long itemId = extractId(selected);
            new SwingWorker<Void, Void>() {
                protected Void doInBackground() throws Exception {
                    try (Connection conn = database.openConnection()) {
                        try (PreparedStatement stmt = conn.prepareStatement("UPDATE inventory SET quantity_available = ? WHERE item_id = ?")) {
                            stmt.setInt(1, qty); stmt.setLong(2, itemId); stmt.executeUpdate();
                        }
                    }
                    return null;
                }
                protected void done() { refresh(); }
            }.execute();
        } catch (NumberFormatException e) { JOptionPane.showMessageDialog(this, "Enter a valid non-negative quantity", "Invalid Stock", JOptionPane.WARNING_MESSAGE); }
    }

    private void setAvailability(boolean available) {
        String selected = invList.getSelectedValue();
        if (selected == null) { status.setText("Select an item first"); return; }
        long itemId = extractId(selected);
        new SwingWorker<Void, Void>() {
            protected Void doInBackground() throws Exception {
                try (Connection conn = database.openConnection()) {
                    try (PreparedStatement stmt = conn.prepareStatement("UPDATE menu_items SET availability = ? WHERE item_id = ?")) {
                        stmt.setString(1, available ? "AVAILABLE" : "SOLD_OUT"); stmt.setLong(2, itemId); stmt.executeUpdate();
                    }
                }
                return null;
            }
            protected void done() { refresh(); }
        }.execute();
    }

    private long extractId(String entry) { return Long.parseLong(entry.substring(entry.lastIndexOf("[ID:") + 4, entry.lastIndexOf("]"))); }
}
