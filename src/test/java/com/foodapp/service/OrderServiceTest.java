// Tests the FoodOS order service test behavior and guards its current contract.
// JUnit assertions make regressions visible without changing production data.

package com.foodapp.service;

import com.foodapp.exception.InvalidOrderException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderServiceTest {
    private final OrderService service = new OrderService(null, null, null, null, null);

    @Test
    void rejectsEmptyCheckoutBeforeOpeningDatabaseConnection() {
        assertThrows(InvalidOrderException.class, () ->
                service.checkout(1L, List.of(), "10 Main Street", "Bengaluru", "560001",
                        com.foodapp.model.PaymentMethod.CASH));
    }

    @Test
    void rejectsInvalidQuantitiesBeforeOpeningDatabaseConnection() {
        assertThrows(InvalidOrderException.class, () ->
                service.checkout(1L, List.of(new OrderService.CheckoutLine(10L, 0)),
                        "10 Main Street", "Bengaluru", "560001",
                        com.foodapp.model.PaymentMethod.CASH));
    }

    @Test
    void rejectsOversizedAddressBeforeOpeningDatabaseConnection() {
        assertThrows(InvalidOrderException.class, () ->
                service.checkout(1L, List.of(new OrderService.CheckoutLine(10L, 1)),
                        "x".repeat(256), "Bengaluru", "560001",
                        com.foodapp.model.PaymentMethod.CASH));
    }
}
