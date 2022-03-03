package com.idi.userlogin.utils;

import com.idi.userlogin.Handlers.ConnectionHandler;
import com.idi.userlogin.Handlers.ControllerHandler;
import com.idi.userlogin.JavaBeans.Collection;
import com.idi.userlogin.JavaBeans.Group;
import com.idi.userlogin.JavaBeans.Item;
import com.idi.userlogin.Main;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import static com.idi.userlogin.Main.jsonHandler;

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
            ps = connection.prepareStatement("SELECT id,name FROM " + DBUtils.DBTable.C.getTable());
            set = ps.executeQuery();
            while (set.next()) {
                Collection collection = new Collection(set.getInt("id"), set.getString("name"));
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

    public static List<String> getHeadersInfo() {
        List<String> headers = new ArrayList<>();
        Connection connection = null;
        ResultSet set = null;
        PreparedStatement ps = null;
        try {
            connection = ConnectionHandler.createDBConnection();
            ps = connection.prepareStatement("SELECT name FROM `" + DBUtils.DBTable.H.getTable() + "`");
            set = ps.executeQuery();

            while (set.next()) {
                headers.add(set.getString("name"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Main.LOGGER.log(Level.SEVERE, "There was an error getting the groups from the db!", e);

        } finally {
            DbUtils.closeQuietly(set);
            DbUtils.closeQuietly(ps);
            DbUtils.closeQuietly(connection);
        }

        return headers;
    }

    public static ObservableList<TreeItem<String>> getItemInfo(String query) {
        Connection connection = null;
        ResultSet set = null;
        PreparedStatement ps = null;
        ObservableList<TreeItem<String>> treeItems = FXCollections.observableArrayList();
        try {
            connection = ConnectionHandler.createDBConnection();
            ps = connection.prepareStatement("SELECT * FROM `" + DBUtils.DBTable.M.getTable() + "` m WHERE " + query + " LIMIT 1");
            set = ps.executeQuery();
            List<String> headers = getHeadersInfo();

            while (set.next()) {
                for (int i = 0; i < headers.size(); i++) {
                    TreeItem col = new TreeItem(headers.get(i));
                    col.setExpanded(true);
                    try {
                        String data = set.getString(headers.get(i));
                        TreeItem item_ = new TreeItem<String>(data);
                        col.getChildren().add(item_);
                        treeItems.add(col);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Main.LOGGER.log(Level.SEVERE, "There was an error getting the groups from the db!", e);

        } finally {
            DbUtils.closeQuietly(set);
            DbUtils.closeQuietly(ps);
            DbUtils.closeQuietly(connection);
        }

        return treeItems;

    }

    public TreeItem<Group> getSubGroup(TreeItem<Group> group, StringBuilder sbH) {
        final TreeItem<Group> parent = group.getParent();
        if (parent.getValue().getName() != null) {
            System.out.println(parent.getValue().getName());
            sbH.append(parent.getValue().getName() + "\\");
            return getSubGroup(group.getParent(), sbH);
        }
        return parent;
    }


    public static ObservableList<Group> getGroups(Collection collection) {
        Connection connection = null;
        ResultSet set = null;
        PreparedStatement ps = null;
        ObservableList<Group> groups = FXCollections.observableArrayList();

        try {
            connection = ConnectionHandler.createDBConnection();
            ps = connection.prepareStatement("SELECT g.id, g.name as groupName,g.employee,c.id as Collection_id,c.name as colName,g.scanned as completed,g.started_on,g.completed_on FROM `" + DBUtils.DBTable.G.getTable() + "` g INNER JOIN `" + DBUtils.DBTable.C.getTable() + "` c ON g.collection_id = c.id  WHERE c.name='" + collection.getName() + "' AND g.employee=" + ConnectionHandler.user.getId() + " OR c.name='" + collection.getName() + "' AND g.employee IS NULL");
            set = ps.executeQuery();
            while (set.next()) {
                final String started_On = (set.getString("g.started_on")) != null ? set.getString("g.started_on") : "";
                final String completed_On = (set.getString("g.completed_on")) != null ? set.getString("g.completed_on") : "";
                final Group group = new Group(set.getInt("g.id"), 0, collection, set.getString("groupName"), intToBoolean(set.getInt("completed")), started_On.replace(" ", "T"), completed_On.replace(" ", "T"));
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

    //Used for sub groups (User Entry - For now)
    public static ObservableList<Group> getGroups_(Collection collection) {
        Connection connection = null;
        ResultSet set = null;
        PreparedStatement ps = null;
        ObservableList<Group> groups = FXCollections.observableArrayList();

        try {
            connection = ConnectionHandler.createDBConnection();
            ps = connection.prepareStatement("SELECT g.id, g.name as groupName,g.s_groups as subGroups,g.employee,c.id as Collection_id,c.name as colName,g.scanned as completed,g.started_on,g.completed_on FROM `" + DBUtils.DBTable.G.getTable() + "` g INNER JOIN `" + DBUtils.DBTable.C.getTable() + "` c ON g.collection_id = c.id  WHERE c.name='" + collection.getName() + "' AND g.employee=" + ConnectionHandler.user.getId() + " OR c.name='" + collection.getName() + "' AND g.employee IS NULL");
            set = ps.executeQuery();
            while (set.next()) {
                final String started_On = (set.getString("g.started_on")) != null ? set.getString("g.started_on") : "";
                final String completed_On = (set.getString("g.completed_on")) != null ? set.getString("g.completed_on") : "";
                Group mainGroup = new Group(set.getInt("g.id"), 0, collection, set.getString("groupName").trim(), intToBoolean(set.getInt("completed")), started_On.replace(" ", "T"), completed_On.replace(" ", "T"));
                mainGroup.setTotal(ControllerHandler.getGroupTotal(mainGroup));
                String sub = set.getString("subGroups");
                if (!set.wasNull()) {
                    String[] sGroups = sub.split(";");
                    for (int i = 0; i < sGroups.length; i++) {
                        Group subGroup = new Group(mainGroup, sGroups[i].trim());
                        subGroup.setID(mainGroup.getID());
                        mainGroup.getSubGroup().add(subGroup);
                    }
                }
                groups.add(mainGroup);
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

}