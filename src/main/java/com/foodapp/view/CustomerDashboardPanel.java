// Implements the desktop FoodOS customer dashboard panel screen using Swing components.
// Handles user interactions in the view while delegating business work.

package com.foodapp.view;

import com.foodapp.exception.AppException;
import com.foodapp.exception.DatabaseOperationException;
import com.foodapp.model.DashboardSummary;
import com.foodapp.service.DashboardService;
import com.foodapp.util.SessionContext;
import java.awt.*;
import javax.swing.*;

public class CustomerDashboardPanel extends JPanel {
    private final DashboardService dashboardService;
    private final AppFrame app;
    private final JLabel restaurantsValue = new JLabel("—");
    private final JLabel activeValue = new JLabel("—");
    private final JLabel recentValue = new JLabel("—");
    private final JPanel content = new JPanel(new BorderLayout(0, UiTheme.SPACING_LG));

    public CustomerDashboardPanel(DashboardService service, AppFrame app) {
        this.dashboardService = service;
        this.app = app;
        setBackground(UiTheme.BACKGROUND);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(UiTheme.SPACING_LG, UiTheme.SPACING_XL, UiTheme.SPACING_LG, UiTheme.SPACING_XL));
        buildUi();
    }

    private void buildUi() {
        JPanel header = Components.pageHeader("Welcome, " + SessionContext.currentUser().getFullName().split(" ")[0],
                "Your food operations at a glance");
        content.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(); center.setOpaque(false); center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JPanel stats = new JPanel(new GridLayout(1, 3, UiTheme.SPACING_MD, 0));
        stats.setOpaque(false);
        stats.add(Components.statCard("Active Restaurants", "—", "Available to browse"));
        stats.add(Components.statCard("Active Orders", "—", "In progress"));
        stats.add(Components.statCard("Recent Orders", "—", "Last 30 days"));
        center.add(stats);
        center.add(Box.createVerticalStrut(UiTheme.SPACING_LG));

        JPanel quickActions = Components.card();
        quickActions.setLayout(new BoxLayout(quickActions, BoxLayout.Y_AXIS));
        JLabel qaTitle = Components.heading("Quick actions", 16); qaTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        quickActions.add(qaTitle); quickActions.add(Box.createVerticalStrut(UiTheme.SPACING_SM));

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, UiTheme.SPACING_SM, 0));
        actionRow.setOpaque(false);
        JButton browse = new JButton("Browse Restaurants"); UiTheme.primaryButton(browse); browse.addActionListener(e -> app.openRestaurants());
        JButton orders = new JButton("My Orders"); UiTheme.secondaryButton(orders); orders.addActionListener(e -> app.openOrders());
        JButton cart = new JButton("View Cart"); UiTheme.secondaryButton(cart); cart.addActionListener(e -> app.openCart());
        actionRow.add(browse); actionRow.add(orders); actionRow.add(cart);
        quickActions.add(actionRow);
        center.add(quickActions);

        content.add(center, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
    }

    public void refresh() {
        new SwingWorker<DashboardSummary, Void>() {
            protected DashboardSummary doInBackground() throws Exception { return dashboardService.customerSummary(SessionContext.currentUser()); }
            protected void done() {
                try {
                    DashboardSummary s = get();
                    restaurantsValue.setText(String.valueOf(s.restaurantCount()));
                    activeValue.setText(String.valueOf(s.activeOrders()));
                    recentValue.setText(String.valueOf(s.recentOrders()));
                    updateStats(s);
                } catch (Exception e) {
                    Throwable cause = e.getCause();
                    updateStats(new DashboardSummary(0, 0, 0));
                }
            }
        }.execute();
    }

    private void updateStats(DashboardSummary s) {
        JPanel center = (JPanel) content.getComponent(1);
        JPanel stats = (JPanel) center.getComponent(0);
        stats.removeAll();
        stats.add(Components.statCard("Active Restaurants", String.valueOf(s.restaurantCount()), "Available to browse"));
        stats.add(Components.statCard("Active Orders", String.valueOf(s.activeOrders()), "In progress"));
        stats.add(Components.statCard("Recent Orders", String.valueOf(s.recentOrders()), "Last 30 days"));
        stats.revalidate(); stats.repaint();
    }
}
