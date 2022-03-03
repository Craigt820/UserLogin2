package com.idi.userlogin.Handlers;

import com.idi.userlogin.JavaBeans.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionHandler {

    public static final String CONN = "jdbc:mysql://" + JsonHandler.hostName + "/Tracking?useSSL=false&allowPublicKeyRetrieval=true";
    public static User user;

    public static Connection createDBConnection() {
        try {
            return DriverManager.getConnection(CONN, JsonHandler.user, JsonHandler.pass);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
