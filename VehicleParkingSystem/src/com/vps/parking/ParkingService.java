package com.vps.parking;

import java.math.BigDecimal;

public interface ParkingService {
    int parkVehicle(int vehicleId);
    boolean retrieveVehicle(int vehicleId);
    BigDecimal calculateParkingFees(int vehicleId);
    boolean makePayment(int vehicleId, BigDecimal amount);
    void updateExpectedDuration(int vehicleId, int expectedHours);
    int getActualDuration(int vehicleId);
}