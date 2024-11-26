package com.vps.vehicle;

import java.math.BigDecimal;

public class Bike extends Vehicle {
    private static final BigDecimal HOURLY_RATE = new BigDecimal("2.0");

    public Bike(int vehicleId, String licensePlate, String ownerName, String ownerContact) {
        super(vehicleId, licensePlate, ownerName, ownerContact);
    }

    @Override
    public BigDecimal calculateHourlyRate() {
        return HOURLY_RATE;
    }
}