// Applies FoodOS cart service business rules for API and desktop workflows.
// Coordinates validation and persistence through the service layer.

package com.foodapp.service;

import com.foodapp.dao.CartDao;
import com.foodapp.dao.MenuItemDao;
import com.foodapp.exception.DatabaseOperationException;
import com.foodapp.exception.InvalidOrderException;
import com.foodapp.model.Cart;
import com.foodapp.model.CartItem;
import com.foodapp.model.MenuItem;
import com.foodapp.util.Database;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class CartService {
    private final Database database; private final CartDao cartDao; private final MenuItemDao menuDao;
    public CartService(Database database, CartDao cartDao, MenuItemDao menuDao) { this.database = database; this.cartDao = cartDao; this.menuDao = menuDao; }
    public Cart getCart(long customerId) throws DatabaseOperationException { try (Connection connection = database.openConnection()) { return new Cart(cartDao.findItems(connection, customerId)); } catch (SQLException exception) { throw new DatabaseOperationException("Unable to load your cart.", exception); } }
    public void add(long customerId, long itemId, int quantity) throws DatabaseOperationException, InvalidOrderException { validateQuantity(quantity); try (Connection connection = database.openConnection()) { MenuItem item = menuDao.findById(connection, itemId); if (item == null || !item.available()) throw new InvalidOrderException("This menu item is unavailable."); Cart current = new Cart(cartDao.findItems(connection, customerId)); if (!current.isEmpty() && current.restaurantId() != item.restaurantId()) throw new InvalidOrderException("Your cart can contain items from one restaurant at a time."); for (CartItem existing : current.items()) if (existing.itemId() == itemId && existing.quantity() + quantity > 50) throw new InvalidOrderException("Maximum quantity for one item is 50."); cartDao.addItem(connection, customerId, itemId, quantity); } catch (InvalidOrderException exception) { throw exception; } catch (SQLException exception) { throw new DatabaseOperationException("Unable to update your cart.", exception); } }
    public void remove(long customerId, long itemId) throws DatabaseOperationException { try (Connection connection = database.openConnection()) { cartDao.removeItem(connection, customerId, itemId); } catch (SQLException exception) { throw new DatabaseOperationException("Unable to update your cart.", exception); } }
    public void clear(long customerId) throws DatabaseOperationException { try (Connection connection = database.openConnection()) { cartDao.clear(connection, customerId); } catch (SQLException exception) { throw new DatabaseOperationException("Unable to clear your cart.", exception); } }
    public void changeQuantity(long customerId, long itemId, int quantity) throws DatabaseOperationException, InvalidOrderException { if (quantity < 0) throw new InvalidOrderException("Quantity cannot be negative."); if (quantity == 0) { remove(customerId, itemId); return; } validateQuantity(quantity); try (Connection connection = database.openConnection()) { cartDao.setQuantity(connection, customerId, itemId, quantity); } catch (SQLException exception) { throw new DatabaseOperationException("Unable to update quantity.", exception); } }
    private void validateQuantity(int quantity) throws InvalidOrderException { if (quantity < 1 || quantity > 50) throw new InvalidOrderException("Quantity must be between 1 and 50."); }
}
