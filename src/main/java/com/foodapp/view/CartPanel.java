// Implements the desktop FoodOS cart panel screen using Swing components.
// Handles user interactions in the view while delegating business work.

package com.foodapp.view;

import com.foodapp.model.CartItem;
import com.foodapp.model.Cart;
import com.foodapp.model.PaymentMethod;
import com.foodapp.service.CartService;
import com.foodapp.service.OrderService;
import com.foodapp.util.SessionContext;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import javax.swing.*;

public class CartPanel extends JPanel {
    private final CartService cartService;
    private final OrderService orderService;
    private final AppFrame app;
    private final DefaultListModel<CartItem> cartModel = new DefaultListModel<>();
    private final JList<CartItem> cartList = new JList<>(cartModel);
    private final JLabel totalLabel = new JLabel("Total: ₹0.00");
    private final JLabel statusLabel = new JLabel(" ");
    private static final NumberFormat MONEY = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public CartPanel(CartService cs, OrderService os, AppFrame app) {
        this.cartService = cs; this.orderService = os; this.app = app;
        setBackground(UiTheme.BACKGROUND);
        setLayout(new BorderLayout(0, UiTheme.SPACING_MD));
        setBorder(BorderFactory.createEmptyBorder(UiTheme.SPACING_LG, UiTheme.SPACING_XL, UiTheme.SPACING_LG, UiTheme.SPACING_XL));
        buildUi();
    }

    private void buildUi() {
        add(Components.pageHeader("Your Cart", "Review items and proceed to checkout"), BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(cartList);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        cartList.setCellRenderer(new CartRenderer());
        add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(UiTheme.SPACING_LG, 0));
        bottom.setOpaque(false);

        JPanel left = new JPanel(); left.setOpaque(false); left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        totalLabel.setFont(UiTheme.heading(18)); totalLabel.setForeground(UiTheme.TEXT);
        statusLabel.setFont(UiTheme.body(13)); statusLabel.setForeground(UiTheme.MUTED);
        left.add(totalLabel); left.add(Box.createVerticalStrut(4)); left.add(statusLabel);
        bottom.add(left, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, UiTheme.SPACING_SM, 0));
        actions.setOpaque(false);
        JButton qtyBtn = new JButton("Change Qty"); UiTheme.secondaryButton(qtyBtn); qtyBtn.addActionListener(e -> changeQuantity());
        JButton removeBtn = new JButton("Remove"); UiTheme.secondaryButton(removeBtn); removeBtn.addActionListener(e -> removeItem());
        JButton clearBtn = new JButton("Clear"); UiTheme.secondaryButton(clearBtn); clearBtn.addActionListener(e -> clearCart());
        JButton checkoutBtn = new JButton("Checkout"); UiTheme.primaryButton(checkoutBtn); checkoutBtn.addActionListener(e -> checkout());
        actions.add(qtyBtn); actions.add(removeBtn); actions.add(clearBtn); actions.add(checkoutBtn);
        bottom.add(actions, BorderLayout.EAST);

        add(bottom, BorderLayout.SOUTH);
    }

    public void refresh() {
        statusLabel.setText("Loading cart...");
        new SwingWorker<Cart, Void>() {
            protected Cart doInBackground() throws Exception { return cartService.getCart(SessionContext.currentUser().getId()); }
            protected void done() {
                try {
                    Cart cart = get();
                    cartModel.clear();
                    for (CartItem item : cart.items()) cartModel.addElement(item);
                    totalLabel.setText("Total: " + MONEY.format(cart.subtotal()));
                    statusLabel.setText(cart.isEmpty() ? "Your cart is empty" : cart.totalQuantity() + " item(s)");
                } catch (Exception e) { statusLabel.setText("Unable to load cart"); }
            }
        }.execute();
    }

    private void changeQuantity() {
        CartItem item = cartList.getSelectedValue();
        if (item == null) return;
        String val = JOptionPane.showInputDialog(this, "Quantity for " + item.itemName() + ":", item.quantity());
        if (val == null) return;
        try {
            int qty = Integer.parseInt(val);
            new SwingWorker<Void,Void>() {
                protected Void doInBackground() throws Exception { cartService.changeQuantity(SessionContext.currentUser().getId(), item.itemId(), qty); return null; }
                protected void done() { refresh(); }
            }.execute();
        } catch (NumberFormatException e) { JOptionPane.showMessageDialog(this, "Enter a whole number between 0 and 50", "Cart", JOptionPane.WARNING_MESSAGE); }
    }

    private void removeItem() {
        CartItem item = cartList.getSelectedValue();
        if (item == null) return;
        new SwingWorker<Void,Void>() {
            protected Void doInBackground() throws Exception { cartService.remove(SessionContext.currentUser().getId(), item.itemId()); return null; }
            protected void done() { refresh(); }
        }.execute();
    }

    private void clearCart() {
        if (cartModel.isEmpty()) return;
        if (JOptionPane.showConfirmDialog(this, "Clear all items from your cart?", "Clear Cart", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        new SwingWorker<Void,Void>() {
            protected Void doInBackground() throws Exception { cartService.clear(SessionContext.currentUser().getId()); return null; }
            protected void done() { refresh(); }
        }.execute();
    }

    private void checkout() {
        if (cartModel.isEmpty()) { JOptionPane.showMessageDialog(this, "Your cart is empty.", "Checkout", JOptionPane.WARNING_MESSAGE); return; }
        CheckoutDialog dialog = new CheckoutDialog((JFrame) SwingUtilities.getWindowAncestor(this), orderService);
        dialog.setVisible(true);
        if (dialog.succeeded()) {
            refresh();
            app.openOrders();
        }
    }

    private static class CartRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, selected, focus);
            CartItem item = (CartItem) value;
            String avail = item.available() ? "" : " <font color='#DC2626'>(unavailable)</font>";
            label.setText("<html><b style='font-size:14px'>" + item.itemName() + "</b> × " + item.quantity() + "  <font color='#2563EB'><b>" + MONEY.format(item.subtotal()) + "</b></font>" + avail + "<br><font color='#6B7280' size='2'>" + item.restaurantName() + " · Unit " + MONEY.format(item.unitPrice()) + "</font></html>");
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, UiTheme.BORDER_SOFT),
                    BorderFactory.createEmptyBorder(12, 14, 12, 14)));
            if (selected) { label.setBackground(UiTheme.PRIMARY_LIGHT); }
            return label;
        }
    }
}
