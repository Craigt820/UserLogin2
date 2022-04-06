package com.idi.userlogin.utils;

import com.idi.userlogin.Handlers.ConnectionHandler;
import com.idi.userlogin.Handlers.ControllerHandler;
import com.idi.userlogin.Handlers.JsonHandler;
import com.idi.userlogin.JavaBeans.Group;
import com.idi.userlogin.Main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.logging.Level;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.commons.dbutils.DbUtils;

//Tracks the status of a Group and how long it takes to scan (Start-End Time & Totals)
public abstract class DailyLog {
    public static int scanLogID = 0;
    public static IntegerProperty dailyTotal;

    public static void insertNewDailyLog(int group_id) {
        Connection connection = null;
        ResultSet set = null;
        PreparedStatement ps = null;
        try {
            connection = ConnectionHandler.createDBConnection();
            ps = connection.prepareStatement("INSERT INTO `ul_scan2` (u_id,job_id,start_time,total,rescan,group_id) VALUES((SELECT id FROM employees WHERE employees.name='" + ConnectionHandler.user.getName() + "'),(SELECT id FROM projects WHERE projects.job_id='" + JsonHandler.getSelJob().getJob_id() + "'),?,?,?,?)", 1);
            Date now = ControllerHandler.formatDateTime(LocalDateTime.now().toString());
            ps.setTimestamp(1, new Timestamp(now.toInstant().toEpochMilli()));
            ps.setInt(2, 0);
            ps.setInt(3, 0);
            ps.setInt(4, group_id);
            ps.executeUpdate();
            set = ps.getGeneratedKeys();
        } catch (SQLException | java.text.ParseException e) {
            e.printStackTrace();
            Main.LOGGER.log(Level.SEVERE, "There was an error inserting the scan log!", e);
        } finally {
            try {
                set = ps.getGeneratedKeys();
                if (set.next())
                    scanLogID = set.getInt(1);
            } catch (SQLException e) {
                Main.LOGGER.log(Level.SEVERE, "There was an error trying to generate a key!", e);
            }
            DbUtils.closeQuietly(set);
            DbUtils.closeQuietly(ps);
            DbUtils.closeQuietly(connection);
            dailyTotal = new SimpleIntegerProperty(0);
        }
    }

    public static int updateLog(Group group) {
        int total = 0;
        if (scanLogID != 0) {
            Connection connection = null;
            ResultSet set = null;
            PreparedStatement ps = null;
            try {
                connection = ConnectionHandler.createDBConnection();
                ps = connection.prepareStatement("UPDATE `ul_scan2` SET total=? WHERE group_id=? AND u_id=" + ConnectionHandler.user.getId() + " AND start_time LIKE '%" + LocalDate.now().toString() + "%' OR total IS NOT NULL AND u_id=" + ConnectionHandler.user.getId() + " AND end_time LIKE '%" + LocalDate.now().toString() + "%' AND id=" + scanLogID);
                ps.setInt(1, group.getTotal());
                ps.setInt(2, group.getID());
                ps.executeUpdate();
                ps = connection.prepareStatement("SELECT total FROM `ul_scan2` WHERE id=" + scanLogID);
                set = ps.executeQuery();
                if (set.next())
                    total = set.getInt("total");
            } catch (SQLException e) {
                e.printStackTrace();
                Main.LOGGER.log(Level.SEVERE, "There was an error updating the scan log", e);
            } finally {
                DbUtils.closeQuietly(set);
                DbUtils.closeQuietly(ps);
                DbUtils.closeQuietly(connection);
            }
            ControllerHandler.loggedInController.getJob1Total().setText(String.valueOf(total));
        }

        return total;
    }

    public static void endDailyLog() {
        if (scanLogID != 0) {
            Connection connection = null;
            ResultSet set = null;
            PreparedStatement ps = null;
            try {
                connection = ConnectionHandler.createDBConnection();
                ps = connection.prepareStatement("UPDATE `ul_scan2` SET end_time=? WHERE id=" + scanLogID, 1);
                Date now = ControllerHandler.formatDateTime(LocalDateTime.now().toString());
                ps.setTimestamp(1, new Timestamp(now.toInstant().toEpochMilli()));
                ps.executeUpdate();
                set = ps.getGeneratedKeys();
            } catch (SQLException | java.text.ParseException e) {
                e.printStackTrace();
                Main.LOGGER.log(Level.SEVERE, "There was an error updating the scan log", e);
            } finally {
                DbUtils.closeQuietly(set);
                DbUtils.closeQuietly(ps);
                DbUtils.closeQuietly(connection);
            }
        }
    }
}
