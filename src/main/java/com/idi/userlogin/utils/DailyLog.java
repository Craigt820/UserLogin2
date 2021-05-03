package com.idi.userlogin.utils;

import com.idi.userlogin.Controllers.ConnectionHandler;
import com.idi.userlogin.Controllers.ControllerHandler;
import com.idi.userlogin.Main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.logging.Level;

import org.apache.commons.dbutils.DbUtils;

public abstract class DailyLog {
    public static int scanLogID = 0;

    public static void insertNewDailyLog() {
        Connection connection = null;
        ResultSet set = null;
        PreparedStatement ps = null;
        try {
            connection = ConnectionHandler.createDBConnection();
            ps = connection.prepareStatement("INSERT INTO `ul_scan` (u_id,job_id,start_time,total,status,rescan,loc_id) VALUES((SELECT id FROM employees WHERE employees.name='" + Main.jsonHandler.getName() + "'),(SELECT id FROM projects WHERE projects.job_id='" + Main.jsonHandler.getSelJobID() + "'),?,?,(SELECT id FROM ul_status WHERE ul_status.name='Online'),?,(SELECT loc_id FROM employees WHERE employees.name='" + Main.jsonHandler.getName() + "'))", 1);
            Date now = ControllerHandler.formatDateTime(LocalDateTime.now().toString());
            ps.setTimestamp(1, new Timestamp(now.toInstant().toEpochMilli()));
            ps.setInt(2, 0);
            ps.setInt(3, 0);
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
                Main.LOGGER.log(Level.SEVERE, "There was an error trying to generating a key!", e);
            }
            DbUtils.closeQuietly(set);
            DbUtils.closeQuietly(ps);
            DbUtils.closeQuietly(connection);
        }
    }

    public static int updateJobTotal() {
        int total = 0;
        if (scanLogID != 0) {
            Connection connection = null;
            ResultSet set = null;
            PreparedStatement ps = null;
            try {
                connection = ConnectionHandler.createDBConnection();
                ps = connection.prepareStatement("UPDATE `ul_scan` SET total=(SELECT IF(ISNULL(total),0,SUM(total)) FROM `" + Main.jsonHandler.getSelJobID() + "` WHERE total IS NOT NULL AND employee_id=(SELECT id FROM employees WHERE name='" + Main.jsonHandler.getName() + "') AND started_on LIKE '%2021-04-23%' OR total IS NOT NULL AND employee_id=(SELECT id FROM employees WHERE name='" + Main.jsonHandler.getName() + "') AND completed_on LIKE '%2021-04-23%') WHERE id=" + scanLogID);
                ps.executeUpdate();
                ps = connection.prepareStatement("SELECT total FROM `ul_scan` WHERE id=" + scanLogID);
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

    public static void updateDailyStatus(String status) {
        if (scanLogID != 0) {
            Connection connection = null;
            ResultSet set = null;
            PreparedStatement ps = null;
            try {
                connection = ConnectionHandler.createDBConnection();
                ps = connection.prepareStatement("UPDATE `ul_scan` SET status=(SELECT id from ul_status WHERE name='" + status + "') WHERE id=" + scanLogID, 1);
                ps.executeUpdate();
                set = ps.getGeneratedKeys();
            } catch (SQLException e) {
                e.printStackTrace();
                Main.LOGGER.log(Level.SEVERE, "There was an error updating the scan log", e);
            } finally {
                DbUtils.closeQuietly(set);
                DbUtils.closeQuietly(ps);
                DbUtils.closeQuietly(connection);
            }
        }
    }

    public static void endDailyLog() {
        if (scanLogID != 0) {
            Connection connection = null;
            ResultSet set = null;
            PreparedStatement ps = null;
            try {
                connection = ConnectionHandler.createDBConnection();
                ps = connection.prepareStatement("UPDATE `ul_scan` SET end_time=? WHERE id=" + scanLogID, 1);
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
