package com.idi.userlogin.Controllers;

import com.idi.userlogin.Handlers.ControllerHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.apache.commons.dbutils.DbUtils;
import com.idi.userlogin.utils.FXUtils;
import com.idi.userlogin.Handlers.JsonHandler;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.Properties;
import java.util.ResourceBundle;

public class AccessCodeController implements Initializable {

    @FXML
    private VBox root;

    @FXML
    private TextField accessCode;

    @FXML
    private Label errorLbl;

    @FXML
    void accessInfo() {

    }

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
            connection = DriverManager.getConnection("jdbc:mysql://" + JsonHandler.hostName + "/Tracking", prop);
            String getLogin = "SELECT Name FROM employees WHERE ACode=? LIMIT 1";
            preparedStatement = connection.prepareStatement(getLogin); //Recommended for running SQL queries multiple times implements Statement interface
            preparedStatement.setString(1, accessCode.getText());
            set = preparedStatement.executeQuery(); //The term "result set" refers to the row and column data contained in a ResultSet object.
            if (set.next()) {
                success = true;
                set.getString("Name");
            }

        } catch (SQLException e) {
            switch (e.getErrorCode()) {
                case 0:
                    errorLbl.setText("Cannot Connect to the Server, Please Try Again Later.");
                    FXUtils.fadeOut(errorLbl, Duration.seconds(2));
                    break;
                case 1045:
                    errorLbl.setText("Your Credentials Are Incorrect, Please Try Again.");
                    FXUtils.fadeOut(errorLbl, Duration.seconds(2));
                    break;
            }

            e.printStackTrace();
        } finally {
            try {
                DbUtils.close(connection);
                DbUtils.close(set);
                DbUtils.close(preparedStatement);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

        }
        return success;
    }

    @FXML
    void login() {
        boolean isSuccess;
        isSuccess = signIn();

        if (isSuccess) {
            ControllerHandler.sceneTransition(root, getClass().getResource("/fxml/JobSelect.fxml"), false);
        } else {
            errorLbl.setText("Your Credentials Are Incorrect, Please Try Again.");
            FXUtils.fadeOut(errorLbl, Duration.seconds(2));
        }
    }

    @FXML
    void toMain() throws IOException {
        ControllerHandler.sceneTransition(ControllerHandler.mainMenuController.root, getClass().getResource("/fxml/MainMenu.fxml"), false);
    }

    @FXML
    void toSettings() throws IOException, InterruptedException {
        ControllerHandler.sceneTransition(ControllerHandler.mainMenuController.root, getClass().getResource("/fxml/Settings.fxml"), false);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
