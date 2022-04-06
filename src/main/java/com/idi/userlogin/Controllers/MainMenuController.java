package com.idi.userlogin.Controllers;

import com.idi.userlogin.Handlers.ConnectionHandler;
import com.idi.userlogin.Handlers.ControllerHandler;
import com.idi.userlogin.Handlers.JsonHandler;
import com.idi.userlogin.Handlers.ProjectHandler;
import com.idi.userlogin.JavaBeans.User;
import com.idi.userlogin.Main;
import com.idi.userlogin.utils.DBUtils;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.apache.commons.dbutils.DbUtils;
import com.idi.userlogin.utils.FXUtils;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import static com.idi.userlogin.Handlers.JsonHandler.HOST_NAME;
import static com.idi.userlogin.Handlers.JsonHandler.trackPath;

public class MainMenuController implements Initializable {

    @FXML
    public VBox root;

    @FXML
    private TextField user;

    @FXML
    private PasswordField password;

    @FXML
    private Label statusLbl;

    private boolean signIn() {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet set = null;
        boolean success = false;
        try {
            Properties prop = new Properties();
            prop.put("connectTimeout", "2000");
            prop.put("user", JsonHandler.user);
            prop.put("password", JsonHandler.pass);
            connection = DriverManager.getConnection(ConnectionHandler.CONN, prop);
            String getLogin = "SELECT id,Name FROM employees WHERE Name=? AND Password=? LIMIT 1";
            preparedStatement = connection.prepareStatement(getLogin); //Recommended for running SQL queries multiple times implements Statement interface
            preparedStatement.setString(1, user.getText());
            preparedStatement.setString(2, password.getText());
            set = preparedStatement.executeQuery(); //The term "result set" refers to the row and column data contained in a ResultSet object.
            if (set.next()) {
                success = true;
                ConnectionHandler.user = new User(set.getString("Name"), set.getInt("id"));
            } else {
                Platform.runLater(() -> {
                    statusLbl.setStyle("-fx-text-fill:#d01515; -fx-font-weight: bold;");
                    statusLbl.setText("Your Credentials Are Incorrect, Please Try Again.");
                    FXUtils.fadeOut(statusLbl, Duration.seconds(2));
                });
            }

        } catch (SQLException e) {
            Platform.runLater(() -> {
                statusLbl.setStyle("-fx-text-fill:#d01515; -fx-font-weight: bold;");
                switch (e.getErrorCode()) {
                    case 0:
                        statusLbl.setText("Cannot Connect to the Server, Please Try Again Later.");
                        FXUtils.fadeOut(statusLbl, Duration.seconds(2));
                        break;
                    case 1045:
                        statusLbl.setText("Your Credentials Are Incorrect, Please Try Again.");
                        FXUtils.fadeOut(statusLbl, Duration.seconds(2));
                        break;
                }
            });

            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(connection);
            DbUtils.closeQuietly(set);
            DbUtils.closeQuietly(preparedStatement);
        }
        return success;
    }

    @FXML
    void login() throws ExecutionException, InterruptedException {
        FXUtils.fadeIn(statusLbl, Duration.seconds(.5));
        statusLbl.setStyle("-fx-text-fill:#3660a3; -fx-font-weight: bold;");
        statusLbl.setText("Logging In...");
        final Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return signIn();
            }
        };
        new Thread(task).start();

        task.setOnSucceeded(e -> {
            try {
                if (task.get()) {
                    Main.consumeStage = true;
                    FXUtils.fadeOut(statusLbl, Duration.seconds(2));
                    workstation();
                    ControllerHandler.sceneTransition(root, getClass().getResource("/fxml/JobSelect.fxml"), false);
                }
            } catch (InterruptedException | ExecutionException ex) {
                ex.printStackTrace();
            }
        });
    }

    private void workstation() {

        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = ConnectionHandler.createDBConnection();
            ps = connection.prepareStatement("Insert into workstation(`name`) SELECT * FROM (SELECT ? as ws) AS temp WHERE NOT EXISTS(SELECT ws FROM workstation WHERE name = ?) LIMIT 1");
            ps.setString(1, HOST_NAME);
            ps.setString(2, HOST_NAME);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            Main.LOGGER.log(Level.SEVERE, "There was an error updating a non-feeder field!", e);

        } finally {
            DbUtils.closeQuietly(ps);
            DbUtils.closeQuietly(connection);
        }
    }

    @FXML
    void toAccessCode() throws IOException {
        ControllerHandler.sceneTransition(root, getClass().getResource("/fxml/AccessCode.fxml"), false);
    }

    @FXML
    void toForgotPwd() throws IOException {
        ControllerHandler.sceneTransition(root, getClass().getResource("/fxml/ForgotPwd.fxml"), false);
    }

    @FXML
    void toSettings() throws IOException, InterruptedException {
        ControllerHandler.sceneTransition(root, getClass().getResource("/fxml/Settings.fxml"), false);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ControllerHandler.mainMenuController = this;
    }

    public VBox getRoot() {
        return root;
    }

}