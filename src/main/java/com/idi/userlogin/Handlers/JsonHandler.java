package com.idi.userlogin.Handlers;

import com.idi.userlogin.JavaBeans.Job;
import com.idi.userlogin.Main;
import com.idi.userlogin.utils.DBUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

public abstract class JsonHandler {

    public static final String USER_DIR = System.getProperty("user.dir");
    public static final String USER_HOME = System.getProperty("user.home");
    public static String HOST_NAME;

    static {
        try {
            HOST_NAME = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public static Job selJob;
    public static String trackPath;
    public static String hostName;
    public static String user;
    public static String pass;
    public static JSONObject property;
    public static boolean exists = false;

    //Initializes properties
    static {
        final File file = new File(USER_DIR + "\\properties.json");
        if (!file.exists()) {
            property = new JSONObject();
            property.put("User", "User");
            property.put("Pass", "idi8@tangos88admin");
            property.put("TrackPath", "F:\\projects");
            property.put("HostName", "192.168.1.147");
            writeJson();
        }
        property = readJson();
        user = property.get("User").toString();
        pass = property.get("Pass").toString();
        trackPath = property.get("TrackPath").toString();
        hostName = property.get("HostName").toString();

        //Check if main project folder exists -- if not, create it
        initFolderStruct();
    }

    private static void initFolderStruct() {
        Path trackPath = null;
        trackPath = Paths.get(Paths.get(USER_HOME).getRoot().toString() + "\\Projects");
        property.put("TrackPath", trackPath);

        if (!trackPath.toFile().exists()) {
            try {
                Files.createDirectory(trackPath);
            } catch (IOException e) {
                e.printStackTrace();
                Main.LOGGER.log(Level.SEVERE, "There was an error creating the main track root!", e);
            }
        }
    }

    public static JSONObject readJson() {

        Object obj = null;
        try {
            obj = new JSONParser().parse(new FileReader(USER_DIR + "\\properties.json"));
        } catch (ParseException | IOException e) {
            e.printStackTrace();
            Main.LOGGER.log(Level.SEVERE, "There was an error reading the properties file!", e);

        }

        return (JSONObject) obj;
    }

    public static void writeJson() {
        try {
            PrintWriter pw = new PrintWriter(USER_DIR + "\\properties.json");
            pw.write(property.toJSONString());
            pw.flush();
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Main.LOGGER.log(Level.WARNING, "There was an error writing to the properties file!", e);

        }
    }

    public static Object getValue(String key) throws FileNotFoundException {
        return property.get(key);
    }

    public static String getTrackPath() {
        return trackPath;
    }

    public static void setTrackPath(String trackPath) {
        JsonHandler.trackPath = trackPath;
    }

    public static Job getSelJob() {
        return selJob;
    }

    public static void setSelJob(Job selJob) {
        JsonHandler.selJob = selJob;
    }
}
