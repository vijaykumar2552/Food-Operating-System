// Defines the typed FoodOS restaurant model used across application layers.
// Encapsulates domain data so services and DAOs share a consistent shape.

package com.foodapp.model;

import java.math.BigDecimal;

public record Restaurant(
        long id,
        long ownerId,
        String name,
        String description,
        String cuisine,
        String location,
        String contactPhone,
        String openingHours,
        BigDecimal rating,
        int deliveryTimeMinutes,
        BigDecimal deliveryDistanceKm,
        BigDecimal deliveryCharge,
        String status,
        String coverImagePath,
        String mapAddress,
        BigDecimal mapLat,
        BigDecimal mapLng,
        String mapLink
) {
    public boolean isActive() { return "ACTIVE".equals(status); }

    public static Restaurant simple(long id, String name, String cuisine, String location,
                                    BigDecimal rating, String status) {
        return new Restaurant(id, 0, name, null, cuisine, location, null, null,
                rating, 30, BigDecimal.ZERO, BigDecimal.ZERO, status, null, null, null, null, null);
    }
}
