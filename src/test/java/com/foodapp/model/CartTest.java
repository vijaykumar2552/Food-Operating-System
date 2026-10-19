// Tests the FoodOS cart test behavior and guards its current contract.
// JUnit assertions make regressions visible without changing production data.

package com.foodapp.model;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CartTest {
    @Test
    void subtotalAndQuantityUseAuthoritativeLineValues() {
        Cart cart = new Cart(List.of(
                new CartItem(1, 3, "Blue Basil", "Paneer Bowl", new BigDecimal("249.00"), 2, true),
                new CartItem(2, 3, "Blue Basil", "Butter Chicken", new BigDecimal("289.00"), 1, true)));
        assertEquals(new BigDecimal("787.00"), cart.subtotal());
        assertEquals(3, cart.totalQuantity());
    }
}
