/*
 * Copyright (c) 2019, CT-Main
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.idi.userlogin.utils;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author CT-Main
 */
public class CustomAlert extends Alert {

    Pane node;
    Region opaqueLayer = new Region();
    Text textWrap = new Text();

    public CustomAlert(AlertType alertType) {

        super(alertType);
        if (alertType.equals(AlertType.NONE)) {
            this.getButtonTypes().add(ButtonType.OK);
        }
        getDialogPane().setContent(textWrap);
        alertPaneSetup();
    }

    public CustomAlert(AlertType alertType, Pane node) {

        super(alertType);
        if (alertType.equals(AlertType.NONE)) {
            this.getButtonTypes().add(ButtonType.OK);
        }
        setupOpaqueLayer();
        alertPaneSetup();
        opaqueLayer.setMinSize(node.getMinWidth(), node.getMinHeight());
        this.node = node;
        this.node.setEffect(new BoxBlur(5, 5, 1));
        this.node.getChildren().add(opaqueLayer);
    }

    public CustomAlert(AlertType alertType, String contentText, Pane node, ButtonType... buttons) {
        super(alertType, contentText, buttons);
        setupOpaqueLayer();
        textWrap.setText(contentText);
        textWrap.setWrappingWidth(400);
        getDialogPane().setContent(textWrap);
        alertPaneSetup();
        opaqueLayer.setMinSize(node.getWidth(), node.getHeight());
        this.node = node;
        this.node.setEffect(new BoxBlur(5, 5, 1));
        this.node.getChildren().add(opaqueLayer);
    }

    public CustomAlert(AlertType alertType, String contentText, ButtonType... buttons) {
        super(alertType, contentText, buttons);
        textWrap.setText(contentText);
        textWrap.setWrappingWidth(400);
        getDialogPane().setContent(textWrap);
        alertPaneSetup();
    }

    private void setupOpaqueLayer() {
        opaqueLayer.setStyle("-fx-background-color:#050C3B;");
        opaqueLayer.setOpacity(.4);
        opaqueLayer.setVisible(false);
    }

    private void alertPaneSetup() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(""));
        DialogPane alertPane = this.getDialogPane();
        alertPane.getStylesheets().add(getClass().getResource("/css/Alerts.css").toExternalForm());
        alertPane.getStyleClass().add("myAlertPane");
        alertPane.setStyle("-fx-background-insets:8;");
        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        DropShadow ds = new DropShadow();
        ds.setOffsetY(1.0);
        ds.setOffsetX(1.0);
        ds.setColor(Color.web("#728282"));
        alertPane.getScene().getRoot().setEffect(ds);
        stage.toFront();
        stage.setAlwaysOnTop(true);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.getScene().setFill(Color.TRANSPARENT);
        stage.centerOnScreen();
        this.setGraphic(null);
        this.showingProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    opaqueLayer.setVisible(true);
                } else {
                    opaqueLayer.setVisible(false);
                    if (node != null) {
                        node.setEffect(new BoxBlur(0, 0, 0));
                    }
                }
            }
        });
    }

    public void setTextContent(String content) {
        textWrap.setText(content);
    }

}
