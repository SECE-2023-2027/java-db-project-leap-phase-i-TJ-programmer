package com.vps.admin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import com.vps.jdbc.Connect;
import com.vps.jdbc.operation;
import com.vps.main.VehicleParkingSystem;  

public class Adminmain {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Connect dbConnect = new Connect();
        dbConnect.connect();

        Login login = new Login(dbConnect.getConnection());
        operation op = new operation(dbConnect.getConnection());
        int choice;

        do {
            System.out.println("Welcome to Admin Portal");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");
            
            while (!sc.hasNextInt()) {
                System.out.println("Invalid input. Please enter a valid number.");
                sc.next(); 
            }
            choice = sc.nextInt();

            switch(choice) {
                case 1:
                    if (login.log() == 1) {
                        System.out.println("Login Successful");
                        adminOperation(sc, op);
                    } else {
                        System.out.println("Login Failed");
                    }
                    break;
                case 2:
                    if(login.signup() == 1) {
                        System.out.println("Registration Successful");
                    } else {
                        System.out.println("Registration Failed");
                    }
                    break;
                case 3:
                    System.out.println("Exiting the Admin Portal.");
                    System.out.println("Returning to the Vehicle Parking System.");
                    VehicleParkingSystem.main(new String[]{});  
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        } while (choice != 3);
        
        sc.close();
        dbConnect.closeConnection();
    }

    private static void adminOperation(Scanner sc, operation op) {
        int choice;

        do {
            System.out.println("Welcome to Admin Operation");
            System.out.println("1. Create a parking spot");
            System.out.println("2. Remove a parking spot");
            System.out.println("3. Generate parking report");
            System.out.println("4. Back");
            System.out.print("Enter your choice: ");
            
            while (!sc.hasNextInt()) {
                System.out.println("Invalid input. Please enter a valid number.");
                sc.next(); 
            }
            choice = sc.nextInt();
            
            switch(choice) {
                case 1:
                    createParkingSpot(sc, op);
                    break;
                case 2:
                    removeParkingSpot(sc, op);
                    break;
                case 3:
                    generateParkingReport(op);
                    break;
                case 4:
                    System.out.println("Returning to the Vehicle Parking System.");
                    VehicleParkingSystem.main(new String[]{});  
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        } while (choice != 4);
    }

    private static void createParkingSpot(Scanner sc, operation op) {
        System.out.println("Enter parking spot type (e.g., Compact, Standard, VIP):");
        String spotType = sc.next();
        
        boolean isOccupied = false;
        boolean validInput = false;
        while (!validInput) {
            System.out.println("Is the spot occupied? (true/false):");
            if (sc.hasNextBoolean()) {
                isOccupied = sc.nextBoolean();
                validInput = true;
            } else {
                System.out.println("Invalid input. Please enter 'true' or 'false'.");
                sc.next(); 
            }
        }
        
        if (op.createParkingSpot(spotType, isOccupied)) {
            System.out.println("Parking spot created successfully.");
        } else {
            System.out.println("Failed to create parking spot.");
        }
    }

    private static void removeParkingSpot(Scanner sc, operation op) {
        int spotId = -1;
        boolean validInput = false;
        
        while (!validInput) {
            System.out.println("Enter parking spot ID to remove:");
            if (sc.hasNextInt()) {
                spotId = sc.nextInt();
                validInput = true;
            } else {
                System.out.println("Invalid input. Please enter a valid parking spot ID.");
                sc.next(); 
            }
        }
        
        if (op.removeParkingSpot(spotId)) {
            System.out.println("Parking spot removed successfully.");
        } else {
            System.out.println("Failed to remove parking spot.");
        }
    }



    private static void generateParkingReport(operation op) {
        System.out.println("Generating parking report...");

        var records = op.getParkingRecords();

        if (records.isEmpty()) {
            System.out.println("No parking records found.");
        } else {
            System.out.printf("%-10s%-15s%-15s%-25s%-25s%-15s\n",
                "Record ID", "Vehicle ID", "Spot ID", "Parked Time", "Exit Time", "Parking Fee");
            StringBuilder report = new StringBuilder();
            report.append(String.format("%-10s%-15s%-15s%-25s%-25s%-15s\n",
                "Record ID", "Vehicle ID", "Spot ID", "Parked Time", "Exit Time", "Parking Fee"));

            for (var record : records) {
                String line = String.format("%-10d%-15d%-15d%-25s%-25s%-15.2f\n",
                    record.getRecordId(), record.getVehicleId(), record.getSpotId(),
                    record.getParkedTime(), record.getExitTime(), record.getParkingFee());
                System.out.print(line);
                report.append(line);
            }
            
            System.out.println("Do you want to download the report as a .txt file? (yes/no):");
            Scanner sc = new Scanner(System.in);
            String choice = sc.next().trim().toLowerCase();

            if (choice.equals("yes")) {
                String defaultPath = "C:\\reports\\parking_report.txt";
                File reportFile = new File(defaultPath);
                reportFile.getParentFile().mkdirs();
                
                try (FileWriter writer = new FileWriter(reportFile)) {
                    writer.write(report.toString());
                    System.out.println("Report saved successfully at: " + defaultPath);
                } catch (IOException e) {
                    System.out.println("Failed to save the report. Error: " + e.getMessage());
                }
            } else {
                System.out.println("Report generation complete. No file saved.");
            }
            sc.close();
        }
       
    }




}
