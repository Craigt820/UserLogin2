package com.idi.userlogin.Controllers;

import com.idi.userlogin.Handlers.ControllerHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;
import com.idi.userlogin.Handlers.JsonHandler;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {

    @FXML
    private VBox root;

    @FXML
    private TextField trackPath;

    @FXML
    private TextField hostName;

    @FXML
    private TextField user;

    @FXML
    private PasswordField password;

    @FXML
    private Label savedLbl;

    @FXML
    void save() {

        if (!trackPath.getText().isEmpty() && !hostName.getText().isEmpty()) {
            JsonHandler.property.put("HostName", hostName.getText());
            JsonHandler.property.put("TrackPath", trackPath.getText());
            JsonHandler.property.put("User", user.getText());
            JsonHandler.property.put("Pass", password.getText());
            JsonHandler.writeJson();
            JsonHandler.readJson();
            JsonHandler.trackPath = JsonHandler.property.get("TrackPath").toString();
            JsonHandler.hostName = JsonHandler.property.get("HostName").toString();
            JsonHandler.user = JsonHandler.property.get("User").toString();
            JsonHandler.pass = JsonHandler.property.get("Pass").toString();
            ControllerHandler.fadeIn(savedLbl, Duration.seconds(1));
        }
    }

    @FXML
    void browseTrack() {
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        final File folder = directoryChooser.showDialog(root.getScene().getWindow());
        if (folder != null && !folder.toString().isEmpty()) {
            trackPath.setText(folder.toString());
        }
    }

    @FXML
    void toMain() throws IOException {
        savedLbl.setOpacity(0);
        ControllerHandler.sceneTransition(ControllerHandler.mainMenuController.root, getClass().getResource("/fxml/MainMenu.fxml"), false);
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        trackPath.setText(JsonHandler.trackPath);
        hostName.setText(JsonHandler.hostName);
        user.setText(JsonHandler.user);
        password.setText(JsonHandler.pass);
    }
}
