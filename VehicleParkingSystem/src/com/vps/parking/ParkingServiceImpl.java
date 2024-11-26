
package com.vps.parking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class ParkingServiceImpl implements ParkingService {
    private final Connection connection;
    public ParkingServiceImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public int parkVehicle(int vehicleId) {
        try {
            
            String findSpotSQL = "SELECT spot_id FROM parkingspots WHERE is_occupied = false LIMIT 1";
            try (PreparedStatement pstmt = connection.prepareStatement(findSpotSQL)) {
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    int spotId = rs.getInt("spot_id");
                    
                  
                    String parkSQL = "INSERT INTO parkingrecords (vehicle_id, spot_id, parked_time) VALUES (?, ?, ?)";
                    try (PreparedStatement parkStmt = connection.prepareStatement(parkSQL)) {
                        parkStmt.setInt(1, vehicleId);  
                        parkStmt.setInt(2, spotId);
                        parkStmt.setObject(3, LocalDateTime.now());
                        parkStmt.executeUpdate();
                        
                        String updateSpotSQL = "UPDATE parkingspots SET is_occupied = true WHERE spot_id = ?";
                        try (PreparedStatement updateStmt = connection.prepareStatement(updateSpotSQL)) {
                            updateStmt.setInt(1, spotId);
                            updateStmt.executeUpdate();
                        }
                        
                        return spotId;
                    }
                }
            }
            return -1; 
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public boolean retrieveVehicle(int vehicleId) {
        try {
           
            String sql = "UPDATE parkingspots SET is_occupied = false WHERE spot_id = " +
                        "(SELECT spot_id FROM parkingrecords WHERE vehicle_id = ? AND exit_time IS NULL)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, vehicleId);
                
               
                String exitSQL = "UPDATE parkingrecords SET exit_time = ? " +
                               "WHERE vehicle_id = ? AND exit_time IS NULL";
                try (PreparedStatement exitStmt = connection.prepareStatement(exitSQL)) {
                    exitStmt.setObject(1, LocalDateTime.now());
                    exitStmt.setInt(2, vehicleId);
                    exitStmt.executeUpdate();
                }
                
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public BigDecimal calculateParkingFees(int vehicleId) {
        try {
            String selectSql = "SELECT pr.expected_duration, v.vehicle_type " +
                               "FROM parkingrecords pr " +
                               "JOIN vehicles v ON pr.vehicle_id = v.vehicle_id " +
                               "WHERE pr.vehicle_id = ? AND pr.exit_time IS NULL";
            
            try (PreparedStatement selectPstmt = connection.prepareStatement(selectSql)) {
                selectPstmt.setInt(1, vehicleId);
                ResultSet rs = selectPstmt.executeQuery();
                
                if (rs.next()) {
                    int expectedHours = rs.getInt("expected_duration");
                    String vehicleType = rs.getString("vehicle_type");
                    
                    BigDecimal hourlyRate = getHourlyRate(vehicleType);
                    BigDecimal parkingFees = hourlyRate.multiply(BigDecimal.valueOf(expectedHours));
                    
                    String updateSql = "UPDATE parkingrecords SET parking_fee = ? WHERE vehicle_id = ? AND exit_time IS NULL";
                    try (PreparedStatement updatePstmt = connection.prepareStatement(updateSql)) {
                        updatePstmt.setBigDecimal(1, parkingFees);
                        updatePstmt.setInt(2, vehicleId);
                        updatePstmt.executeUpdate();
                    }
                    
                    return parkingFees;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    private BigDecimal getHourlyRate(String vehicleType) {
        return switch (vehicleType.toLowerCase()) {
            case "car" -> new BigDecimal("5.0");
            case "bike" -> new BigDecimal("2.0");
            case "truck" -> new BigDecimal("10.0");
            default -> new BigDecimal("5.0"); // Default rate
        };
    }

    @Override
    public boolean makePayment(int vehicleId, BigDecimal amount) {
        try {
            String sql = "UPDATE parkingrecords SET parking_fee = ? " +
                        "WHERE vehicle_id = ? AND exit_time IS NULL";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setBigDecimal(1, amount);
                pstmt.setInt(2, vehicleId);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void updateExpectedDuration(int vehicleId, int expectedHours) {
        try {
            String sql = "UPDATE parkingrecords SET expected_duration = ? " +
                        "WHERE vehicle_id = ? AND exit_time IS NULL";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, expectedHours);
                pstmt.setInt(2, vehicleId);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getActualDuration(int vehicleId) {
        try {
            String sql = "SELECT parked_time, COALESCE(exit_time, CURRENT_TIMESTAMP) as end_time " +
                        "FROM parkingrecords WHERE vehicle_id = ? AND exit_time IS NULL";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, vehicleId);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    LocalDateTime parkedTime = rs.getObject("parked_time", LocalDateTime.class);
                    LocalDateTime endTime = rs.getObject("end_time", LocalDateTime.class);
                    
                    long minutes = ChronoUnit.MINUTES.between(parkedTime, endTime);
                    return (int) Math.ceil(minutes / 60.0);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}