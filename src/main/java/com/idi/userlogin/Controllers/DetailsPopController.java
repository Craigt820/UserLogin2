package com.idi.userlogin.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.controlsfx.control.CheckComboBox;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static com.idi.userlogin.Controllers.UserEntryViewController.CONDITION_LIST;

public class DetailsPopController implements Initializable {

    @FXML
    public Label browseInfo;
    @FXML
    public Label workstation,location, startedOn, compOn;
    @FXML
    public TextArea commentsField;
    @FXML
    public CheckComboBox<String> conditCombo, scannerCombo;
    @FXML
    public Label overridden;

    @FXML
    private void browseLoc() throws IOException {
        if (location.getText() != null && !location.getText().isEmpty()) {
            File file = new File(location.getText());
            Desktop.getDesktop().open(new File(file.getParent()));
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        conditCombo.getItems().addAll(CONDITION_LIST);
        Tooltip.install(browseInfo, new Tooltip("Browse"));

    }

}
