package com.idi.userlogin.Controllers;

import com.idi.userlogin.JavaBeans.Group;
import com.idi.userlogin.JavaBeans.Item;
import com.jfoenix.controls.JFXTreeTableView;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import org.controlsfx.control.textfield.CustomTextField;

import java.net.URL;
import java.util.ResourceBundle;

public class BaseManifestController  extends ControllerHandler implements Initializable {

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @Override
    public int insertHelper(Item<? extends Item> item) {
        return 0;
    }

    @Override
    public void updateTotal() {

    }

    @Override
    public ObservableList<? extends Item> getGroupItems(Group group) {
        return null;
    }

    @Override
    public void resetFields() {

    }


    @Override
    public void legalTextTest(boolean isLegal, CustomTextField node) {

    }
}
