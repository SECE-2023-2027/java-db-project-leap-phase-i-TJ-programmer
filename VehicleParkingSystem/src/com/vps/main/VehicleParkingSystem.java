package com.vps.main;

import java.math.BigDecimal;

import java.util.List;
import java.util.Scanner;
import com.vps.jdbc.Connect;
import com.vps.jdbc.operation;
import com.vps.jdbc.ParkingRecord;
import com.vps.user.User;
import com.vps.user.UserLogin;
import com.vps.admin.Adminmain;
import com.vps.vehicle.Vehicle;
import com.vps.vehicle.VehicleFactory;
import com.vps.parking.ParkingService;
import com.vps.parking.ParkingServiceImpl;

public class VehicleParkingSystem {
    private static int currentUserId = -1;
    private static User currentUser = null;
    private static ParkingService parkingService;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Connect dbConnect = new Connect();
        dbConnect.connect();
        
        parkingService = new ParkingServiceImpl(dbConnect.getConnection());
        operation op = new operation(dbConnect.getConnection());
        
        int choice;
        do {
            System.out.println("\nWelcome to Vehicle Parking System");
            if (currentUserId == -1) {
                displayLoginMenu();
            } else {
                displayMainMenu();
            }
            
            System.out.print("Enter your choice: ");
            while (!sc.hasNextInt()) {
                System.out.println("Invalid input. Please enter a valid number.");
                sc.next();
            }
            choice = sc.nextInt();

            if (currentUserId == -1) {
                choice = handleLoginMenuChoice(choice, sc, dbConnect, op);
            } else {
                choice = handleMainMenuChoice(choice, sc, op);
            }
        } while (choice != (currentUserId == -1 ? 4 : 7));

        dbConnect.closeConnection();
        sc.close();
    }

    private static void displayLoginMenu() {
        System.out.println("1. User Login");
        System.out.println("2. User Registration");
        System.out.println("3. Admin Portal");
        System.out.println("4. Exit");
    }

    private static void displayMainMenu() {
        System.out.println("1. Park vehicle");
        System.out.println("2. Retrieve vehicle");
        System.out.println("3. Calculate fees");
        System.out.println("4. Make payment");
        System.out.println("5. View my parking history");
        System.out.println("6. Logout");
        System.out.println("7. Exit");
    }

    private static int handleLoginMenuChoice(int choice, Scanner sc, Connect dbConnect, operation op) {
        switch (choice) {
            case 1:
                UserLogin userLogin = new UserLogin(dbConnect.getConnection());
                int userId = userLogin.login();
                if (userId > 0) {
                    currentUserId = userId;
                    currentUser = op.getUserDetails(userId);
                    System.out.println("Welcome, " + currentUser.getFullName() + "!");
                } else {
                    System.out.println("Login failed. Please try again.");
                }
                break;
            case 2:
                UserLogin userReg = new UserLogin(dbConnect.getConnection());
                if (userReg.register() > 0) {
                    System.out.println("Registration successful! Please login.");
                } else {
                    System.out.println("Registration failed. Please try again.");
                }
                break;
            case 3:
                System.out.println("\nLaunching Admin Panel...");
                Adminmain.main(new String[]{});
                break;
            case 4:
                System.out.println("Exiting the Vehicle Parking System.");
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
                break;
        }
        return choice;
    }

    private static int handleMainMenuChoice(int choice, Scanner sc, operation op) {
        switch (choice) {
            case 1:
                parkVehicle(sc, op);
                break;
            case 2:
                retrieveVehicle(sc, op);
                break;
            case 3:
                calculateFees(sc, op);
                break;
            case 4:
                makePayment(sc, op);
                break;
            case 5:
                viewParkingHistory(op);
                break;
            case 6:
                logout();
                break;
            case 7:
                System.out.println("Exiting the Vehicle Parking System.");
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
                break;
        }
        return choice;
    }

    private static void logout() {
        currentUserId = -1;
        currentUser = null;
        System.out.println("Logged out successfully.");
    }

    private static void parkVehicle(Scanner sc, operation op) {
        try {
            if (currentUser == null) {
                System.out.println("Please login first.");
                return;
            }

            System.out.println("\nEnter vehicle details:");

            System.out.println("\nEnter vehicle ID:");
            while (!sc.hasNextInt()) {
                System.out.println("Invalid input. Please enter a valid number.");
                sc.next();
            }
            int vehicleId = sc.nextInt();
            sc.nextLine();

            System.out.println("Enter license plate number:");
            String licensePlate = sc.nextLine();

            System.out.println("Enter vehicle type (Car/Bike/Truck):");
            String vehicleType = sc.nextLine();


            String ownerName = currentUser.getFullName();
            String ownerContact = currentUser.getPhone();

            System.out.println("\nConfirming owner details:");
            System.out.println("Owner Name: " + ownerName);
            System.out.println("Contact: " + ownerContact);

            
            System.out.println("\nEnter expected parking duration (in hours):");
            while (!sc.hasNextInt()) {
                System.out.println("Invalid input. Please enter a valid number of hours.");
                sc.next();
            }
            int expectedHours = sc.nextInt();
            sc.nextLine();

            Vehicle vehicle = VehicleFactory.createVehicle(vehicleType, vehicleId, licensePlate, ownerName, ownerContact);
            
            
            BigDecimal hourlyRate = vehicle.calculateHourlyRate();
            BigDecimal estimatedCost = hourlyRate.multiply(BigDecimal.valueOf(expectedHours));
            
            System.out.println("\nParking Summary:");
            System.out.println("Expected Duration: " + expectedHours + " hours");
            System.out.println("Hourly Rate: $" + hourlyRate);
            System.out.println("Estimated Cost: $" + estimatedCost);
            
            System.out.println("\nConfirm parking? (Y/N)");
            String confirm = sc.nextLine();
            
            if (confirm.equalsIgnoreCase("Y")) {
                
                boolean vehicleRegistered = op.insertVehicle(vehicleId, licensePlate, vehicleType, ownerName, ownerContact);
                
                if (vehicleRegistered) {
                    int spotId = parkingService.parkVehicle(vehicleId);
                    if (spotId != -1) {
                        System.out.println("Vehicle parked successfully at spot ID: " + spotId);
                        
                        ((ParkingServiceImpl) parkingService).updateExpectedDuration(vehicleId, expectedHours);
                    } else {
                        System.out.println("Failed to park vehicle. No available spots.");
                    }
                } else {
                    System.out.println("Failed to register vehicle.");
                }
            } else {
                System.out.println("Parking cancelled.");
            }
        } catch (Exception e) {
            System.out.println("Error while parking vehicle: " + e.getMessage());
        }
    }

    private static void retrieveVehicle(Scanner sc, operation op) {
        try {
            if (currentUser == null) {
                System.out.println("Please login first.");
                return;
            }

            System.out.println("\nEnter vehicle ID to retrieve:");
            while (!sc.hasNextInt()) {
                System.out.println("Invalid input. Please enter a valid number.");
                sc.next();
            }
            int vehicleId = sc.nextInt();
            sc.nextLine(); 

            BigDecimal fees = parkingService.calculateParkingFees(vehicleId);
            if (fees == null) {
                System.out.println("Vehicle not found or already retrieved.");
                return;
            }
            System.out.println("\nEnter the actual hours stayed:");
            int actualHours = sc.nextInt();
            sc.nextLine(); 

            
            op.updateActualDuration(vehicleId, actualHours);
            
            fees = parkingService.calculateParkingFees(vehicleId);
            System.out.println("\nParking Fee Summary:");
            System.out.println("Amount Due: $" + fees);

            

            System.out.println("\nProceed with payment? (Y/N)");
            String proceed = sc.nextLine();

            if (proceed.equalsIgnoreCase("Y")) {
                makePayment(sc, op);

              
                boolean retrieved = op.retrieveVehicle(vehicleId);
                if (retrieved) {
                    System.out.println("Vehicle retrieved successfully.");
                } else {
                    System.out.println("Error retrieving vehicle after payment.");
                }
            } else {
                System.out.println("Retrieval cancelled. Please pay parking fees before retrieving the vehicle.");
            }
        } catch (Exception e) {
            System.out.println("Error while retrieving vehicle: " + e.getMessage());
        }
    }




    private static void calculateFees(Scanner sc, operation op) {
        try {
            if (currentUser == null) {
                System.out.println("Please login first.");
                return;
            }

            System.out.println("\nEnter vehicle ID to calculate fees:");
            while (!sc.hasNextInt()) {
                System.out.println("Invalid input. Please enter a valid number.");
                sc.next();
            }
            int vehicleId = sc.nextInt();

            BigDecimal fees = parkingService.calculateParkingFees(vehicleId);
            if (fees != null) {
                int actualHours = ((ParkingServiceImpl) parkingService).getActualDuration(vehicleId);
                System.out.println("\nExpected Parking Fee Summary:");
                System.out.println("Duration: " + actualHours + " hours");
                System.out.println("Amount Due: $" + fees);
            } else {
                System.out.println("Failed to calculate parking fees. Vehicle might not be parked or already retrieved.");
            }
        } catch (Exception e) {
            System.out.println("Error while calculating fees: " + e.getMessage());
        }
    }

    private static void makePayment(Scanner sc, operation op) {
        try {
            if (currentUser == null) {
                System.out.println("Please login first.");
                return;
            }

            System.out.println("\nEnter vehicle ID for payment:");
            while (!sc.hasNextInt()) {
                System.out.println("Invalid input. Please enter a valid number.");
                sc.next();
            }
            int vehicleId = sc.nextInt();

            
            BigDecimal fees = parkingService.calculateParkingFees(vehicleId);
            if (fees == null) {
                System.out.println("No pending payment found for this vehicle.");
                return;
            }

            System.out.println("Pending amount: $" + fees);
            System.out.println("Enter payment amount:");
            while (!sc.hasNextDouble()) {
                System.out.println("Invalid input. Please enter a valid amount.");
                sc.next();
            }
            double amountDouble = sc.nextDouble();
            BigDecimal amount = BigDecimal.valueOf(amountDouble);

            if (amount.compareTo(fees) < 0) {
                System.out.println("Payment amount must be at least $" + fees);
                return;
            }

            if (parkingService.makePayment(vehicleId, amount)) {
                System.out.println("Payment of $" + amount + " successfully processed for vehicle ID " + vehicleId);
            } else {
                System.out.println("Failed to process payment. Please check if the vehicle ID is correct.");
            }
        } catch (Exception e) {
            System.out.println("Error while processing payment: " + e.getMessage());
        }
    }

    private static void viewParkingHistory(operation op) {
        if (currentUser == null) {
            System.out.println("Please login first.");
            return;
        }

        System.out.println("\nYour Parking History:");
        String ownerName = currentUser.getFullName();
        List<ParkingRecord> records = op.getUserParkingRecords(ownerName);
        
        if (records.isEmpty()) {
            System.out.println("No parking history found.");
            return;
        }

        System.out.printf("%-10s%-15s%-15s%-25s%-25s%-15s\n",
            "Record ID", "Vehicle ID", "Spot ID", "Parked Time", "Exit Time", "Fee");
        
        for (ParkingRecord record : records) {
            System.out.printf("%-10d%-15d%-15d%-25s%-25s$%-14.2f\n",
                record.getRecordId(),
                record.getVehicleId(),
                record.getSpotId(),
                record.getParkedTime(),
                record.getExitTime() != null ? record.getExitTime() : "Still Parked",
                record.getParkingFee() != null ? record.getParkingFee().doubleValue() : 0.00);
        }
    }
}