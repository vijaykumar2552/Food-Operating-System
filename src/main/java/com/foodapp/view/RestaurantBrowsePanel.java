// Implements the desktop FoodOS restaurant browse panel screen using Swing components.
// Handles user interactions in the view while delegating business work.

package com.foodapp.view;

import com.foodapp.exception.AppException;
import com.foodapp.model.MenuItem;
import com.foodapp.model.Restaurant;
import com.foodapp.service.CartService;
import com.foodapp.service.MenuItemService;
import com.foodapp.service.RestaurantService;
import com.foodapp.util.SessionContext;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import javax.swing.*;

public class RestaurantBrowsePanel extends JPanel {
    private final RestaurantService restaurantService;
    private final MenuItemService menuService;
    private final CartService cartService;
    private final AppFrame app;
    private final JTextField searchField = new JTextField();
    private final DefaultListModel<Restaurant> restaurantModel = new DefaultListModel<>();
    private final JList<Restaurant> restaurantList = new JList<>(restaurantModel);
    private final DefaultListModel<MenuItem> menuModel = new DefaultListModel<>();
    private final JList<MenuItem> menuList = new JList<>(menuModel);
    private final JLabel statusLabel = new JLabel(" ");
    private Restaurant currentRestaurant;
    private static final NumberFormat MONEY = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public RestaurantBrowsePanel(RestaurantService rs, MenuItemService ms, CartService cs, AppFrame app) {
        this.restaurantService = rs; this.menuService = ms; this.cartService = cs; this.app = app;
        setBackground(UiTheme.BACKGROUND);
        setLayout(new BorderLayout(0, UiTheme.SPACING_MD));
        setBorder(BorderFactory.createEmptyBorder(UiTheme.SPACING_LG, UiTheme.SPACING_XL, UiTheme.SPACING_LG, UiTheme.SPACING_XL));
        buildUi();
    }

    private void buildUi() {
        JPanel header = Components.pageHeader("Restaurants", "Browse and order from active restaurants");
        add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, UiTheme.SPACING_MD));
        center.setOpaque(false);

        JPanel topBar = new JPanel(new BorderLayout(UiTheme.SPACING_MD, 0));
        topBar.setOpaque(false);
        JButton searchBtn = new JButton("Search");
        topBar.add(Components.searchField(searchField, searchBtn), BorderLayout.CENTER);
        searchBtn.addActionListener(e -> loadRestaurants(searchField.getText()));
        searchField.addActionListener(e -> loadRestaurants(searchField.getText()));
        center.add(topBar, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerLocation(320);
        split.setDividerSize(1);
        split.setBorder(null);
        split.setOpaque(false);

        restaurantList.setCellRenderer(new RestaurantRenderer());
        restaurantList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        restaurantList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && restaurantList.getSelectedValue() != null) {
                currentRestaurant = restaurantList.getSelectedValue();
                loadMenu(currentRestaurant);
            }
        });
        JScrollPane leftScroll = new JScrollPane(restaurantList);
        leftScroll.setBorder(null);
        leftScroll.getVerticalScrollBar().setUnitIncrement(16);
        split.setLeftComponent(leftScroll);

        menuList.setCellRenderer(new MenuRenderer());
        menuList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane rightScroll = new JScrollPane(menuList);
        rightScroll.setBorder(null);
        rightScroll.getVerticalScrollBar().setUnitIncrement(16);
        split.setRightComponent(rightScroll);

        center.add(split, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        statusLabel.setFont(UiTheme.body(13)); statusLabel.setForeground(UiTheme.MUTED);
        bottom.add(statusLabel, BorderLayout.WEST);
        JButton addBtn = new JButton("Add to Cart"); UiTheme.primaryButton(addBtn);
        addBtn.addActionListener(e -> addToCart());
        bottom.add(addBtn, BorderLayout.EAST);
        center.add(bottom, BorderLayout.SOUTH);

        add(center, BorderLayout.CENTER);
    }

    public void refresh() { loadRestaurants(""); }

    private void loadRestaurants(String search) {
        statusLabel.setText("Loading restaurants...");
        new SwingWorker<List<Restaurant>, Void>() {
            protected List<Restaurant> doInBackground() throws Exception { return restaurantService.search(search, "All cuisines"); }
            protected void done() {
                try {
                    restaurantModel.clear();
                    for (Restaurant r : get()) restaurantModel.addElement(r);
                    statusLabel.setText(restaurantModel.isEmpty() ? "No restaurants found" : restaurantModel.size() + " restaurants available");
                } catch (Exception e) { statusLabel.setText(errorMessage(e, "Unable to load restaurants")); }
            }
        }.execute();
    }

    private void loadMenu(Restaurant r) {
        statusLabel.setText("Loading menu for " + r.name() + "...");
        new SwingWorker<List<MenuItem>, Void>() {
            protected List<MenuItem> doInBackground() throws Exception { return menuService.findForRestaurant(r.id(), "", "All categories"); }
            protected void done() {
                try {
                    menuModel.clear();
                    for (MenuItem item : get()) menuModel.addElement(item);
                    statusLabel.setText(menuModel.isEmpty() ? "No menu items available" : "Select an item and add to cart");
                } catch (Exception e) { statusLabel.setText(errorMessage(e, "Unable to load menu")); }
            }
        }.execute();
    }

    private void addToCart() {
        MenuItem item = menuList.getSelectedValue();
        if (item == null) { statusLabel.setText("Select a menu item first"); return; }
        if (!item.available()) { statusLabel.setText("This item is currently unavailable"); return; }
        String qtyStr = JOptionPane.showInputDialog(this, "Quantity for " + item.name() + ":", "1");
        if (qtyStr == null) return;
        try {
            int qty = Integer.parseInt(qtyStr);
            statusLabel.setText("Adding to cart...");
            new SwingWorker<Void, Void>() {
                protected Void doInBackground() throws Exception { cartService.add(SessionContext.currentUser().getId(), item.id(), qty); return null; }
                protected void done() {
                    try { get(); statusLabel.setText("Added " + qty + " x " + item.name() + " to cart"); }
                    catch (Exception e) { statusLabel.setText(errorMessage(e, "Unable to add item")); }
                }
            }.execute();
        } catch (NumberFormatException e) { statusLabel.setText("Enter a whole number between 1 and 50"); }
    }

    private String errorMessage(Exception e, String fallback) {
        Throwable c = e.getCause(); return c instanceof AppException ? c.getMessage() : fallback;
    }

    private static class RestaurantRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, selected, focus);
            Restaurant r = (Restaurant) value;
            label.setText("<html><b style='font-size:14px'>" + r.name() + "</b><br><font color='#6B7280' size='2'>" + r.cuisine() + " · " + r.location() + " · Rating " + r.rating() + "</font></html>");
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, UiTheme.BORDER_SOFT),
                    BorderFactory.createEmptyBorder(14, 14, 14, 14)));
            if (selected) { label.setBackground(UiTheme.PRIMARY_LIGHT); label.setForeground(UiTheme.TEXT); }
            return label;
        }
    }

    private static class MenuRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, selected, focus);
            MenuItem item = (MenuItem) value;
            String avail = item.available() ? "<font color='#16A34A'><b>Available</b></font> · Stock: " + item.stock() : "<font color='#DC2626'><b>Unavailable</b></font>";
            label.setText("<html><b style='font-size:14px'>" + item.name() + "</b>  <font color='#2563EB' size='3'><b>" + MONEY.format(item.price()) + "</b></font><br><font color='#6B7280' size='2'>" + item.category() + " · " + avail + "<br>" + (item.description() != null ? item.description() : "") + "</font></html>");
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, UiTheme.BORDER_SOFT),
                    BorderFactory.createEmptyBorder(12, 14, 12, 14)));
            if (selected) { label.setBackground(UiTheme.PRIMARY_LIGHT); label.setForeground(UiTheme.TEXT); }
            return label;
        }
    }
}
