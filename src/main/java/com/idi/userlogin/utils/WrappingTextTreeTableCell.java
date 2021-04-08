/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.idi.userlogin.utils;

import com.jfoenix.controls.cells.editors.base.EditorNodeBuilder;
import com.jfoenix.controls.cells.editors.base.GenericEditableTreeTableCell;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.converter.DefaultStringConverter;

/**
 *
 * @author CT-Main
 */
public class WrappingTextTreeTableCell<S> extends TextFieldTreeTableCell<S, String> {

    private final Text cellText;

    public WrappingTextTreeTableCell() {
        super(new DefaultStringConverter());
        this.wrapTextProperty().setValue(true);
        this.cellText = createText();
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setGraphic(cellText);
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null && !isEmpty() && !isEditing()) {
            super.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue observable, Boolean oldValue, Boolean newValue) {
                    if (newValue == true) {
                        cellText.setStyle("-fx-fill:white;");
                    } else {
                        cellText.setStyle("-fx-fill:black;");
                    }
                }
            });

            super.getTreeTableView().focusedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (newValue == true) {
                        cellText.setStyle("-fx-fill:white;");
                    }
                }
            });

            super.getTreeTableRow().selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (newValue == true) {
                        cellText.setStyle("-fx-fill:black;");
                    } else {
                        cellText.setStyle("-fx-fill:white;");
                    }
                }
            });
            setGraphic(cellText);
        }
    }

    private Text createText() {
        Text text = new Text();
        text.wrappingWidthProperty().bind(widthProperty());
        text.textProperty().bind(itemProperty());
        text.setTextAlignment(TextAlignment.CENTER);
        return text;
    }
}