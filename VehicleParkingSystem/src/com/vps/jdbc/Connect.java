package com.vps.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connect {
    private String url = "jdbc:mysql://localhost:3306/VPS";
    private String user = "root";
    private String password = "tarun2006";
    private Connection con;

    
    public void connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(url, user, password);
            System.out.println("Connection successful!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    
    public Connection getConnection() {
        return con;
    }

    
    public void closeConnection() {
        try {
            if (con != null && !con.isClosed()) {
                con.close();
                System.out.println("Connection closed.");
            }
        } catch (SQLException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }
}
