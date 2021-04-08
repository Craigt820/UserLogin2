package com.idi.userlogin.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import com.idi.userlogin.Main;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ForgotPwdController implements Initializable {

    @FXML
    private VBox root;

    @FXML
    private TextField email;

    @FXML
    private Label sentLbl;

    @FXML
    void resetPwd() {
        if (!email.getText().isEmpty()) {
            Main.fadeIn(sentLbl, Duration.seconds(1));
        }
    }

    @FXML
    void toMain() throws IOException {
        ControllerHandler.sceneTransition(ControllerHandler.mainMenuController.root, getClass().getResource("/fxml/MainMenu.fxml"), false);
        sentLbl.setOpacity(0);
    }

    @FXML
    void toSettings() throws IOException {
        ControllerHandler.sceneTransition(ControllerHandler.mainMenuController.root, getClass().getResource("/fxml/Settings.fxml"), false);
        sentLbl.setOpacity(0);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
