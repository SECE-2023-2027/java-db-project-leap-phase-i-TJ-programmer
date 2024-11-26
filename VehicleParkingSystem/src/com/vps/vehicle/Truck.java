package com.vps.vehicle;

import java.math.BigDecimal;

public class Truck extends Vehicle {
    private static final BigDecimal HOURLY_RATE = new BigDecimal("10.0");

    public Truck(int vehicleId, String licensePlate, String ownerName, String ownerContact) {
        super(vehicleId, licensePlate, ownerName, ownerContact);
    }

    @Override
    public BigDecimal calculateHourlyRate() {
        return HOURLY_RATE;
    }
}
