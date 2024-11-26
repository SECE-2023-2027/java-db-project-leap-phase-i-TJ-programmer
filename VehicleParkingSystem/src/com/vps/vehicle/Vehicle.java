package com.vps.vehicle;

import java.math.BigDecimal;

public abstract class Vehicle {
    private int vehicleId;
    private String licensePlate;
    private String ownerName;
    private String ownerContact;

    public Vehicle(int vehicleId, String licensePlate, String ownerName, String ownerContact) {
        this.vehicleId = vehicleId;
        this.licensePlate = licensePlate;
        this.ownerName = ownerName;
        this.ownerContact = ownerContact;
    }

   
    public abstract BigDecimal calculateHourlyRate();

    
    public int getVehicleId() { return vehicleId; }
    public String getLicensePlate() { return licensePlate; }
    public String getOwnerName() { return ownerName; }
    public String getOwnerContact() { return ownerContact; }
}