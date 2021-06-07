package com.idi.userlogin.utils;

import com.idi.userlogin.Handlers.ConnectionHandler;
import com.idi.userlogin.JavaBeans.Collection;
import com.idi.userlogin.JavaBeans.Group;
import com.idi.userlogin.Handlers.JsonHandler;
import com.idi.userlogin.Main;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.*;
import java.time.format.DateTimeFormatter;

public class Utils {

    public final static DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a");

    public static int booleanToInt(boolean bool) {
        return bool ? 1 : 0;
    }


    public static boolean intToBoolean(int value) {
        return value == 1;
    }

    public static boolean legalText(String newValue) {
        return !StringUtils.containsAny(newValue, "\\/\\:\\*\\?\\<\\>\\|\\") && !newValue.isEmpty();
    }


    public static ObservableList<Collection> getCollections() {
        Connection connection = null;
        ResultSet set = null;
        PreparedStatement ps = null;
        ObservableList<Collection> collections = FXCollections.observableArrayList();

        try {
            connection = ConnectionHandler.createDBConnection();
            ps = connection.prepareStatement("SELECT c.id,c.name,p.job_id FROM sc_collections c INNER JOIN tracking.projects p ON c.job_id = p.id WHERE p.job_id ='" + Main.jsonHandler.getSelJobID() + "'");
            set = ps.executeQuery();
            while (set.next()) {
                Collection collection = new Collection(set.getInt("c.id"), set.getString("c.name"));
                collections.add(collection);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(set);
            DbUtils.closeQuietly(ps);
            DbUtils.closeQuietly(connection);
        }
        return collections;
    }

    public static ObservableList<Group> getGroups(Collection collection) {
        Connection connection = null;
        ResultSet set = null;
        PreparedStatement ps = null;
        ObservableList<Group> groups = FXCollections.observableArrayList();

        try {
            connection = ConnectionHandler.createDBConnection();
            ps = connection.prepareStatement("SELECT g.id,p.job_id, g.name as groupName,c.id as Collection_id,c.name as CollectionName,g.scanned as Completed,g.started_on,g.completed_on FROM tracking.sc_groups g INNER JOIN tracking.sc_collections c ON g.collection_id = c.id INNER JOIN tracking.projects p ON g.job_id = p.id WHERE p.job_id ='" + Main.jsonHandler.getSelJobID() + "' AND c.name='" + collection.getName() + "' AND employees LIKE '%" +  ConnectionHandler.user.getName() + "%' OR p.job_id ='" + Main.jsonHandler.getSelJobID() + "' AND c.name='" + collection.getName() + "' AND employees IS NULL");
            set = ps.executeQuery();
            while (set.next()) {
                final String started_On = (set.getString("g.started_on")) != null ? set.getString("g.started_on") : "";
                final String completed_On = (set.getString("g.completed_on")) != null ? set.getString("g.completed_on") : "";
                final Group group = new Group(set.getInt("g.id"), collection, set.getString("groupName"), intToBoolean(set.getInt("Completed")), started_On.replace(" ", "T"), completed_On.replace(" ", "T"));
                groups.add(group);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(set);
            DbUtils.closeQuietly(ps);
            DbUtils.closeQuietly(connection);
        }
        return groups;
    }

    ;


}