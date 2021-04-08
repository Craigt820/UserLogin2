package com.idi.userlogin.Controllers;

import com.idi.userlogin.JsonHandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionHandler {

    public static final String CONN = "jdbc:mysql://" + JsonHandler.hostName + "/Tracking";

    public static Connection createDBConnection() {
        try {
            return DriverManager.getConnection(CONN, JsonHandler.user, JsonHandler.pass);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
