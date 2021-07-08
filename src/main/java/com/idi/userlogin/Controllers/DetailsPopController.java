package com.idi.userlogin.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
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
    public Label workstation, location, startedOn, compOn;
    @FXML
    public TextArea commentsField;
    @FXML
    public TreeView<String> itemInfo;
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
        itemInfo.setShowRoot(false);
        itemInfo.setCellFactory(e -> {
            return new TreeCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        //Parent
                        if (!getTreeItem().isLeaf()) {
                            setAlignment(Pos.CENTER_LEFT);
                            setStyle("-fx-font-weight:bold;-fx-padding:8;-fx-font-size: 1.1em;");
                        } else {
                            setAlignment(Pos.CENTER_LEFT);
                            setStyle("-fx-font-weight:none;-fx-padding:4 24 4 0;-fx-font-size: 1.1em;");
                        }
                        Label label = new Label(item);
                        label.setWrapText(true);
                        label.setMaxWidth(1000);
                        label.textOverrunProperty().set(OverrunStyle.CLIP);
                        setGraphic(label);
                        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    } else {
                        setGraphic(null);
                        setText(null);
                    }
                }
            };
        });
        conditCombo.getItems().addAll(CONDITION_LIST);
        Tooltip.install(browseInfo, new Tooltip("Browse"));

    }

}
