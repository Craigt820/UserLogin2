package com.idi.userlogin;

import com.dustinredmond.fxtrayicon.FXTrayIcon;
import com.idi.userlogin.Handlers.JsonHandler;
import com.idi.userlogin.utils.ImgFactory;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
//import sun.util.logging.PlatformLogger;
import java.io.*;
import java.net.InetAddress;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Main extends Application {

    public final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    public final static ObservableList<String> DEVICE_LIST = FXCollections.observableArrayList();
    public static FXTrayIcon fxTrayIcon;
    public static boolean consumeStage; //Flag to block closing the window ONLY if on the main menu window
    public static FileHandler logHandler;
    private static SimpleFormatter formatter;

    @Override
    public void start(Stage primaryStage) throws Exception {
//        com.sun.javafx.util.Logging.getCSSLogger().setLevel(sun.util.logging.PlatformLogger.Level.OFF);
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/MainMenu.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setMaximized(true);
        primaryStage.setOnCloseRequest(e -> {
            if (consumeStage) {
                e.consume();
            }else{
                fxTrayIcon.hide();
                Platform.exit();
            }
        });

        //Logging
        File dir = new File(JsonHandler.USER_DIR + "\\Logs");
        if (!dir.exists()) {
            Files.createDirectories(dir.toPath());
        }

        Main.logHandler = new FileHandler(dir + "\\" + LocalDate.now().toString() + " Error Log.txt", true);
        formatter = new SimpleFormatter();
        Main.logHandler.setFormatter(formatter);
        Main.LOGGER.addHandler(logHandler);
        LOGGER.setLevel(Level.ALL);
        if (fxTrayIcon == null) {
            fxTrayIcon = new FXTrayIcon((Stage) root.getScene().getWindow(), new URL(ImgFactory.IMGS.CHECKMARK.getLoc()));
            fxTrayIcon.show();
            fxTrayIcon.setApplicationTitle("User Login");
        }


        //Get any connect USB Devices that may be scanners
        final Task<ArrayList<String>> task = new Task<ArrayList<String>>() {
            @Override
            protected ArrayList<String> call() throws Exception {
                ArrayList<String> devices = new ArrayList<>();
                File file = new File("scanners.txt");
                if (file.exists()) {
                    file.delete();
                }
                //Plug & Play Device
                Process processBuilder = new ProcessBuilder().command("powershell", "get-pnpdevice", "-Class USB", "-Status OK", "|", "select-object", "FriendlyName").redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.appendTo(file)).start();
                int errorCode = processBuilder.waitFor();
                if (errorCode == 0) {
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    //Skip Header - "FriendlyName"
                    br.readLine();
                    br.readLine();
                    br.readLine();
                    String line = null;
                    while ((line = br.readLine()) != null)
                        if (!devices.contains(line.trim()) && !line.isEmpty()) {
                            devices.add(line.trim());
                        }
                    br.close();
                }

                return devices;
            }
        };

        new Thread(task).start();
        task.setOnSucceeded(e -> {
            try {
                DEVICE_LIST.addAll(task.get());
                System.out.println(DEVICE_LIST.toString());
            } catch (InterruptedException | ExecutionException ex) {
                ex.printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
