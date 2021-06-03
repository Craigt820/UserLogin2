package com.idi.userlogin.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class GroupPopController implements Initializable {

    @FXML
    public Label browseInfo;
    @FXML
    public Label location, total, collection, startedOn, compOn;
    @FXML
    public TextArea commentsField;


    @FXML
    private void browseLoc() throws IOException {
        if (location.getText() != null && !location.getText().isEmpty()) {
            File file = new File(location.getText());
            Desktop.getDesktop().open(new File(file.getParent()));
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Tooltip.install(browseInfo, new Tooltip("Browse"));
    }

}
