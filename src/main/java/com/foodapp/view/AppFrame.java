// Implements the desktop FoodOS app frame screen using Swing components.
// Handles user interactions in the view while delegating business work.

package com.foodapp.view;

import com.foodapp.config.AppConfig;
import com.foodapp.dao.*;
import com.foodapp.exception.AppException;
import com.foodapp.model.DashboardSummary;
import com.foodapp.model.User;
import com.foodapp.service.*;
import com.foodapp.util.Database;
import com.foodapp.util.SessionContext;
import java.awt.*;
import javax.swing.*;

public class AppFrame extends JFrame implements Sidebar.NavigationListener {
    private final AuthenticationService auth;
    private final Database database = new Database(AppConfig.load());
    private final DashboardService dashboardService = new DashboardService(database);
    private final RestaurantService restaurantService = new RestaurantService(database, new JdbcRestaurantDao());
    private final MenuItemService menuService = new MenuItemService(database, new JdbcMenuItemDao());
    private final CartService cartService = new CartService(database, new JdbcCartDao(), new JdbcMenuItemDao());
    private final OrderService orderService = new OrderService(database, new JdbcCartDao(), new JdbcMenuItemDao(), new JdbcOrderDao(), new JdbcAddressDao());

    private final Sidebar sidebar;
    private final JPanel content = new JPanel(new CardLayout());
    private final JLabel headerTitle = new JLabel();
    private final JLabel headerSubtitle = new JLabel();

    private final CustomerDashboardPanel dashboardPanel;
    private final RestaurantBrowsePanel restaurantPanel;
    private final CartPanel cartPanel;
    private final OrdersPanel ordersPanel;
    private final StaffDashboardPanel staffPanel;
    private final MenuManagementPanel menuMgmtPanel;
    private final InventoryPanel inventoryPanel;
    private final AnalyticsPanel analyticsPanel;
    private final NotificationsPanel notificationsPanel;
    private final SettingsPanel settingsPanel;

    private final String[] customerPages = {"Dashboard", "Restaurants", "Cart", "Orders", "Settings"};
    private final String[] staffPages = {"Dashboard", "Menu", "Inventory", "Orders", "Analytics", "Notifications", "Settings"};

    public AppFrame(AuthenticationService auth) {
        this.auth = auth;
        User user = SessionContext.currentUser();
        boolean isStaff = user.getRole() == com.foodapp.model.Role.RESTAURANT_STAFF || user.getRole() == com.foodapp.model.Role.ADMIN;
        String[] pages = isStaff ? staffPages : customerPages;

        setTitle("FoodOS | " + user.getFullName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 760);
        setMinimumSize(new Dimension(960, 640));
        setLocationRelativeTo(null);

        dashboardPanel = new CustomerDashboardPanel(dashboardService, this);
        restaurantPanel = new RestaurantBrowsePanel(restaurantService, menuService, cartService, this);
        cartPanel = new CartPanel(cartService, orderService, this);
        ordersPanel = new OrdersPanel(orderService, this);
        staffPanel = new StaffDashboardPanel(database, this);
        menuMgmtPanel = new MenuManagementPanel(database, this);
        inventoryPanel = new InventoryPanel(database, this);
        analyticsPanel = new AnalyticsPanel(database, this);
        notificationsPanel = new NotificationsPanel();
        settingsPanel = new SettingsPanel(auth, this);

        sidebar = new Sidebar(this, pages);

        buildShell();
        navigate(isStaff ? "Dashboard" : "Dashboard");
    }

    private void buildShell() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UiTheme.BACKGROUND);

        sidebar.setBackground(UiTheme.SIDEBAR_BG);
        root.add(sidebar, BorderLayout.WEST);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UiTheme.BACKGROUND);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UiTheme.SURFACE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UiTheme.BORDER),
                BorderFactory.createEmptyBorder(18, 28, 18, 28)));
        JPanel headerLeft = new JPanel(); headerLeft.setOpaque(false); headerLeft.setLayout(new BoxLayout(headerLeft, BoxLayout.Y_AXIS));
        headerTitle.setFont(UiTheme.heading(20)); headerTitle.setForeground(UiTheme.TEXT);
        headerSubtitle.setFont(UiTheme.body(13)); headerSubtitle.setForeground(UiTheme.MUTED);
        headerSubtitle.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
        headerLeft.add(headerTitle); headerLeft.add(headerSubtitle);
        header.add(headerLeft, BorderLayout.WEST);
        main.add(header, BorderLayout.NORTH);

        content.setBackground(UiTheme.BACKGROUND);
        content.add(dashboardPanel, "Dashboard");
        content.add(restaurantPanel, "Restaurants");
        content.add(cartPanel, "Cart");
        content.add(ordersPanel, "Orders");
        content.add(staffPanel, "StaffDashboard");
        content.add(menuMgmtPanel, "Menu");
        content.add(inventoryPanel, "Inventory");
        content.add(analyticsPanel, "Analytics");
        content.add(notificationsPanel, "Notifications");
        content.add(settingsPanel, "Settings");
        main.add(content, BorderLayout.CENTER);

        root.add(main, BorderLayout.CENTER);
        setContentPane(root);
    }

    @Override
    public void navigate(String page) {
        CardLayout cl = (CardLayout) content.getLayout();
        headerTitle.setText(page);
        switch (page) {
            case "Dashboard" -> {
                headerSubtitle.setText("Overview and quick actions");
                boolean staff = SessionContext.currentUser().getRole() != com.foodapp.model.Role.CUSTOMER;
                if (staff) { cl.show(content, "StaffDashboard"); staffPanel.refresh(); }
                else { cl.show(content, "Dashboard"); dashboardPanel.refresh(); }
            }
            case "Restaurants" -> { headerSubtitle.setText("Browse and order from active restaurants"); cl.show(content, "Restaurants"); restaurantPanel.refresh(); }
            case "Cart" -> { headerSubtitle.setText("Review your items and checkout"); cl.show(content, "Cart"); cartPanel.refresh(); }
            case "Orders" -> { headerSubtitle.setText("Track your orders and view details"); cl.show(content, "Orders"); ordersPanel.refresh(); }
            case "Menu" -> { headerSubtitle.setText("Manage your restaurant menu"); cl.show(content, "Menu"); menuMgmtPanel.refresh(); }
            case "Inventory" -> { headerSubtitle.setText("Track and update stock levels"); cl.show(content, "Inventory"); inventoryPanel.refresh(); }
            case "Analytics" -> { headerSubtitle.setText("Performance insights and trends"); cl.show(content, "Analytics"); analyticsPanel.refresh(); }
            case "Notifications" -> { headerSubtitle.setText("Recent alerts and updates"); cl.show(content, "Notifications"); }
            case "Settings" -> { headerSubtitle.setText("Manage your profile and preferences"); cl.show(content, "Settings"); settingsPanel.refresh(); }
            default -> cl.show(content, page);
        }
        sidebar.setActive(page);
    }

    @Override
    public void logout() {
        SessionContext.signOut();
        dispose();
        new LoginFrame(auth).setVisible(true);
    }

    public void openRestaurants() { navigate("Restaurants"); }
    public void openCart() { navigate("Cart"); }
    public void openOrders() { navigate("Orders"); }
}
