package com.vps.admin;

import java.sql.Connection;
import java.util.Scanner;
import com.vps.jdbc.operation;

public class Login {
    private Scanner sc = new Scanner(System.in);
    private String uname, pass;
    private operation op;

    public Login(Connection con) {
        op = new operation(con);
    }

    int log() {
        System.out.println("Enter the Username:");
        uname = sc.next();
        System.out.println("Enter the Password:");
        pass = sc.next();
        
        if(op.checkLogin(uname, pass) == 1) {
            return 1;
        } else {
            return 0;
        }
    }
    int signup() {
    	System.out.println("Enter the new Username:");
        uname = sc.next();
        System.out.println("Enter the new Password:");
        pass = sc.next();
        if(op.register(uname, pass) == 1) {
            return 1;
        } else {
            
            return 0;
        }
    }
}
