package com.idi.userlogin.Handlers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionHandler {

    public static final String CONN = "jdbc:mysql://" + JsonHandler.hostName + "/Tracking?useSSL=false";

    public static Connection createDBConnection() {
        try {
            return DriverManager.getConnection(CONN, JsonHandler.user, JsonHandler.pass);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
