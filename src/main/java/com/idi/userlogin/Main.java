package com.idi.userlogin;

import com.dustinredmond.fxtrayicon.FXTrayIcon;
import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
//import sun.util.logging.PlatformLogger;
import java.io.*;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Main extends Application {
    public static JsonHandler jsonHandler;
    public static ObservableList<String> devices = FXCollections.observableArrayList();
    public static FXTrayIcon fxTrayIcon;
    public static FileHandler logHandler;
    public final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private static SimpleFormatter formatter;

//    public static void fadeIn(Node node) {
//        DoubleProperty opacity = node.opacityProperty();
//        Timeline fadeIn = new Timeline(
//                new KeyFrame(Duration.ZERO, new KeyValue(opacity, 0.0)),
//                new KeyFrame(new Duration(300), new KeyValue(opacity, 1.0))
//        );
//        fadeIn.play();
//    }

    public static void fadeIn(Node node, Duration duration) {
        FadeTransition ft = new FadeTransition();
        ft.setNode(node);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.setRate(1.0);
        ft.setDuration(duration);
        SequentialTransition s = new SequentialTransition(ft);
        s.play();
    }

    public static void fadeOut(Node node, Duration duration) {
        FadeTransition ft = new FadeTransition();
        ft.setNode(node);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setRate(1.0);
        ft.setDuration(duration);
        SequentialTransition s = new SequentialTransition(ft);
        s.play();
    }

//    public static void fadeOut(Node node) {
//        DoubleProperty opacity = node.opacityProperty();
//        Timeline fadeIn = new Timeline(
//                new KeyFrame(Duration.ZERO, new KeyValue(opacity, 1.0)),
//                new KeyFrame(new Duration(300), new KeyValue(opacity, 0.0))
//        );
//        fadeIn.play();
//    }

    @Override
    public void start(Stage primaryStage) throws Exception {
//        com.sun.javafx.util.Logging.getCSSLogger().setLevel(sun.util.logging.PlatformLogger.Level.OFF);
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/MainMenu.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setMaximized(true);
        Platform.setImplicitExit(true);
        primaryStage.setOnCloseRequest(e -> System.exit(0));

        //Logs
        File dir = new File(JsonHandler.userDir + "\\Logs");
        if (!dir.exists()) {
            Files.createDirectories(dir.toPath());
        }

        Main.logHandler = new FileHandler(dir + "\\" + LocalDate.now().toString() + " Error Log.txt", true);
        formatter = new SimpleFormatter();
        Main.logHandler.setFormatter(formatter);
        Main.LOGGER.addHandler(logHandler);
        LOGGER.setLevel(Level.SEVERE);

        fxTrayIcon = new FXTrayIcon((Stage) root.getScene().getWindow(), Main.class.getResource("/images/check.png"));
        fxTrayIcon.show();
        fxTrayIcon.setApplicationTitle("User Login");

        //Init & Read Json Properties File
        jsonHandler = new JsonHandler();

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
                devices.addAll(task.get());
            } catch (InterruptedException | ExecutionException ex) {
                ex.printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
