package com.idi.userlogin.utils;

import com.jfoenix.controls.cells.editors.base.EditorNodeBuilder;
import com.jfoenix.controls.cells.editors.base.GenericEditableTableCell;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class WrappingTextfieldTableCell<K, T> extends GenericEditableTableCell<K, T> {

    private final Text cellText;

    public WrappingTextfieldTableCell(EditorNodeBuilder builder) {
        super(builder);
        this.wrapTextProperty().setValue(true);
        this.cellText = createText();
    }

    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
        this.setPadding(new Insets(8, 8, 8, 8));
        if (item != null) {
            setGraphic(cellText);
        } else {
            setGraphic(null);
            setText("");
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit(); //To change body of generated methods, choose Tools | Templates.
    }

    private Text createText() {
        Text text = new Text();
        text.wrappingWidthProperty().bind(widthProperty());
        text.textProperty().bind((ObservableValue<? extends String>) itemProperty());
        text.setTextAlignment(TextAlignment.CENTER);
        return text;
    }
}