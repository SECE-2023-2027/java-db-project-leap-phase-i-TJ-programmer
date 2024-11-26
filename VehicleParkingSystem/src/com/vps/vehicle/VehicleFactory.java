package com.vps.vehicle;

public class VehicleFactory {
    public static Vehicle createVehicle(String vehicleType, int vehicleId, String licensePlate, String ownerName, String ownerContact) {
        return switch (vehicleType.toLowerCase()) {
            case "car" -> new Car(vehicleId, licensePlate, ownerName, ownerContact);
            case "bike" -> new Bike(vehicleId, licensePlate, ownerName, ownerContact);
            case "truck" -> new Truck(vehicleId, licensePlate, ownerName, ownerContact);
            default -> throw new IllegalArgumentException("Invalid vehicle type: " + vehicleType);
        };
    }
}