// Tests the FoodOS role test behavior and guards its current contract.
// JUnit assertions make regressions visible without changing production data.

package com.foodapp.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RoleTest {
    @Test
    void roleNamesMatchDatabaseValues() {
        assertEquals("CUSTOMER", Role.CUSTOMER.name());
        assertEquals("RESTAURANT_STAFF", Role.RESTAURANT_STAFF.name());
        assertEquals("ADMIN", Role.ADMIN.name());
    }
}
