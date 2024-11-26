
package com.vps.user;

import java.sql.Connection;
import java.util.Scanner;
import com.vps.jdbc.operation;

public class UserLogin {
    private Scanner sc = new Scanner(System.in);
    private operation op;

    public UserLogin(Connection con) {
        op = new operation(con);
    }

    public int login() {
        System.out.println("Enter Username:");
        String username = sc.next();
        System.out.println("Enter Password:");
        String password = sc.next();
        
        return op.checkUserLogin(username, password);
    }

    public int register() {
        System.out.println("Enter Username:");
        String username = sc.next();
        System.out.println("Enter Password:");
        String password = sc.next();
        sc.nextLine();
        System.out.println("Enter Full Name:");
        String fullName = sc.nextLine();
        System.out.println("Enter Email:");
        String email = sc.next();
        System.out.println("Enter Phone:");
        String phone = sc.next();
        
        return op.registerUser(username, password, fullName, email, phone);
    }
}