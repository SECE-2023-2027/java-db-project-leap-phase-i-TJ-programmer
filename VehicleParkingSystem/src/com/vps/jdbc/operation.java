package com.vps.jdbc;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

import com.vps.user.User;
public class operation {
    private Connection con;

    public operation(Connection con) {
        this.con = con;
    }

    
    public boolean insertVehicle(int vehicleId, String licensePlate, String vehicleType, String ownerName, String ownerContact) {
        String query = "INSERT INTO Vehicles (vehicle_id, license_plate, vehicle_type, owner_name, owner_contact) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setInt(1, vehicleId);
            pstmt.setString(2, licensePlate);
            pstmt.setString(3, vehicleType);
            pstmt.setString(4, ownerName);
            pstmt.setString(5, ownerContact);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Error inserting vehicle: " + e.getMessage());
            return false;
        }
    }

    
    public int parkVehicle(int vehicleId) {
        
        String findSpotQuery = "SELECT spot_id FROM ParkingSpots WHERE is_occupied = FALSE AND is_active = TRUE LIMIT 1";
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(findSpotQuery)) {
            if (rs.next()) {
                int spotId = rs.getInt("spot_id");
                
                
                String insertQuery = "INSERT INTO ParkingRecords (vehicle_id, spot_id, parked_time) VALUES (?, ?, NOW())";
                try (PreparedStatement pstmt = con.prepareStatement(insertQuery)) {
                    pstmt.setInt(1, vehicleId);
                    pstmt.setInt(2, spotId);
                    pstmt.executeUpdate();
                }
                
                
                return spotId;
            }
        } catch (SQLException e) {
            System.out.println("Error parking vehicle: " + e.getMessage());
        }
        return -1;
    }

    
    public boolean retrieveVehicle(int vehicleId) {
        String updateExitTimeQuery = "UPDATE ParkingRecords SET exit_time = NOW() WHERE vehicle_id = ? AND exit_time IS NULL";
        String findSpotIdQuery = "SELECT spot_id FROM ParkingRecords WHERE vehicle_id = ? AND exit_time = NOW()";

        try (PreparedStatement pstmtUpdate = con.prepareStatement(updateExitTimeQuery);
             PreparedStatement pstmtFindSpot = con.prepareStatement(findSpotIdQuery)) {
            
            pstmtUpdate.setInt(1, vehicleId);
            int rowsAffected = pstmtUpdate.executeUpdate();

            if (rowsAffected > 0) {
                pstmtFindSpot.setInt(1, vehicleId);
                try (ResultSet rs = pstmtFindSpot.executeQuery()) {
                    if (rs.next()) {
                        int spotId = rs.getInt("spot_id");
                        String updateSpotStatusQuery = "UPDATE ParkingSpots SET is_occupied = FALSE WHERE spot_id = ?";
                        try (PreparedStatement pstmtUpdateSpot = con.prepareStatement(updateSpotStatusQuery)) {
                            pstmtUpdateSpot.setInt(1, spotId);
                            pstmtUpdateSpot.executeUpdate();
                        }
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving vehicle: " + e.getMessage());
        }
        return false;
    }


    
    public BigDecimal calculateParkingFees(int vehicleId) {
        String query = "{CALL CalculateParkingFee(?, ?)}";
        try (CallableStatement stmt = con.prepareCall(query)) {
            stmt.setInt(1, vehicleId);
            stmt.registerOutParameter(2, Types.DECIMAL);
            stmt.execute();
            return stmt.getBigDecimal(2);
        } catch (SQLException e) {
            System.out.println("Error calculating parking fees: " + e.getMessage());
            return null;
        }
    }

    
    public boolean makePayment(int vehicleId, BigDecimal amountPaid) {
        String query = "UPDATE ParkingRecords SET parking_fee = ?, payment_status = 'PAID' " +
                      "WHERE vehicle_id = ? AND exit_time IS NULL";
        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setBigDecimal(1, amountPaid);
            pstmt.setInt(2, vehicleId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error making payment: " + e.getMessage());
            return false;
        }
    }

   
    public boolean createParkingSpot(String spotType, boolean isOccupied) {
        String query = "INSERT INTO ParkingSpots (spot_type, is_occupied) VALUES (?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, spotType);
            stmt.setBoolean(2, isOccupied);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error creating parking spot: " + e.getMessage());
            return false;
        }
    }

    public boolean removeParkingSpot(int spotId) {
        String query = "UPDATE ParkingSpots SET is_active = FALSE WHERE spot_id = ?";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, spotId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error removing parking spot: " + e.getMessage());
            return false;
        }
    }

    public List<ParkingRecord> getParkingRecords() {
        List<ParkingRecord> records = new ArrayList<>();
        String query = "SELECT * FROM ParkingRecords"; 

        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                ParkingRecord record = new ParkingRecord();
                record.setRecordId(rs.getInt("record_id"));
                record.setVehicleId(rs.getInt("vehicle_id"));
                record.setSpotId(rs.getInt("spot_id"));
                record.setParkedTime(rs.getTimestamp("parked_time"));
                record.setExitTime(rs.getTimestamp("exit_time"));
                record.setParkingFee(rs.getBigDecimal("parking_fee"));
                record.setPaymentStatus(rs.getString("payment_status"));
                record.setCreatedAt(rs.getTimestamp("created_at"));
                records.add(record);
            }
        } catch (SQLException e) {
            System.out.println("Error generating parking report: " + e.getMessage());
        }
        return records;
    }


    public int checkLogin(String uname, String pass) {
        String query = "SELECT * FROM admindetails WHERE Username = ? AND Pass = ?";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, uname);
            stmt.setString(2, pass);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? 1 : 0;
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            return 0;
        }
    }

    public int register(String uname, String pass) {
        String query = "INSERT INTO admindetails (Username, Pass) VALUES (?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, uname);
            stmt.setString(2, pass);
            return stmt.executeUpdate() > 0 ? 1 : 0;
        } catch (SQLException e) {
            System.out.println("Error during registration: " + e.getMessage());
            return 0;
        }
    }
    public int checkUserLogin(String username, String password) {
        String query = "SELECT user_id FROM Users WHERE username = ? AND password = ? AND is_active = TRUE";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    
                    updateLastLogin(userId);
                    return userId;
                }
                return -1;
            }
        } catch (SQLException e) {
            System.out.println("Error during user login: " + e.getMessage());
            return -1;
        }
    }

    
    public int registerUser(String username, String password, String fullName, String email, String phone) {
        String query = "INSERT INTO Users (username, password, full_name, email, phone) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, fullName);
            stmt.setString(4, email);
            stmt.setString(5, phone);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1); 
                    }
                }
            }
            return -1;
        } catch (SQLException e) {
            System.out.println("Error during user registration: " + e.getMessage());
            return -1;
        }
    }

    
    private void updateLastLogin(int userId) {
        String query = "UPDATE Users SET last_login = CURRENT_TIMESTAMP WHERE user_id = ?";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating last login: " + e.getMessage());
        }
    }

    
    public User getUserDetails(int userId) {
        String query = "SELECT * FROM Users WHERE user_id = ?";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setFullName(rs.getString("full_name"));
                    user.setEmail(rs.getString("email"));
                    user.setPhone(rs.getString("phone"));
                    return user;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving user details: " + e.getMessage());
        }
        return null;
    }
    
    public List<ParkingRecord> getUserParkingRecords(String ownerName) {
        List<ParkingRecord> history = new ArrayList<>();
        String findVehicleQuery = "SELECT vehicle_id FROM Vehicles WHERE owner_name = ?";
        String findParkingRecordsQuery = "SELECT * FROM ParkingRecords WHERE vehicle_id = ? ORDER BY parked_time DESC";

        try (PreparedStatement vehicleStmt = con.prepareStatement(findVehicleQuery)) {
            vehicleStmt.setString(1, ownerName);
            ResultSet vehicleRs = vehicleStmt.executeQuery();

            if (vehicleRs.next()) {
                int vehicleId = vehicleRs.getInt("vehicle_id");

                try (PreparedStatement parkingStmt = con.prepareStatement(findParkingRecordsQuery)) {
                    parkingStmt.setInt(1, vehicleId);

                    try (ResultSet rs = parkingStmt.executeQuery()) {
                        while (rs.next()) {
                            ParkingRecord record = new ParkingRecord();
                            record.setRecordId(rs.getInt("record_id"));
                            record.setVehicleId(rs.getInt("vehicle_id"));
                            record.setSpotId(rs.getInt("spot_id"));
                            record.setParkedTime(rs.getTimestamp("parked_time"));
                            record.setExitTime(rs.getTimestamp("exit_time"));
                            record.setParkingFee(rs.getBigDecimal("parking_fee"));
                            history.add(record);
                        }
                    }
                }
            } else {
                System.out.println("No vehicle found for the specified user.");
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving parking history: " + e.getMessage());
        }
        return history;
    }


    public void updateActualDuration(int vehicleId, int actualHours) {
        String updateQuery = "UPDATE ParkingRecords SET expected_duration = ? WHERE vehicle_id = ? AND exit_time IS NULL";
        try (PreparedStatement pstmt = con.prepareStatement(updateQuery)) {
            pstmt.setInt(1, actualHours);
            pstmt.setInt(2, vehicleId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating actual duration: " + e.getMessage());
        }
    }



}