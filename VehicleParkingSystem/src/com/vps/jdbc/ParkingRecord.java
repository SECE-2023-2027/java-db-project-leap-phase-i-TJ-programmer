package com.vps.jdbc;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class ParkingRecord {
    private int recordId;
    private int vehicleId;
    private int spotId;
    private Timestamp parkedTime;
    private Timestamp exitTime;
    private BigDecimal parkingFee;
    private String vehicleType;
    private String paymentStatus;
    private Timestamp createdAt; 

   

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public int getSpotId() {
        return spotId;
    }

    public void setSpotId(int spotId) {
        this.spotId = spotId;
    }

    public Timestamp getParkedTime() {
        return parkedTime;
    }

    public void setParkedTime(Timestamp parkedTime) {
        this.parkedTime = parkedTime;
    }

    public Timestamp getExitTime() {
        return exitTime;
    }

    public void setExitTime(Timestamp exitTime) {
        this.exitTime = exitTime;
    }

    public BigDecimal getParkingFee() {
        return parkingFee;
    }

    public void setParkingFee(BigDecimal parkingFee) {
        this.parkingFee = parkingFee;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getPaymentStatus() { 
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) { 
        this.paymentStatus = paymentStatus;
    }

    public Timestamp getCreatedAt() { 
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) { 
        this.createdAt = createdAt;
    }
}
