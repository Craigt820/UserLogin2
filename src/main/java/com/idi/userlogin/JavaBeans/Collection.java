package com.idi.userlogin.JavaBeans;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.ListCell;

import java.util.ArrayList;
import java.util.List;

public class Collection extends ListCell<Collection> {
    private int id;
    private StringProperty name;
    private List<Group> groupList;

    public Collection(int id, String name) {
        this.id = id;
        this.name = new SimpleStringProperty(name);
        this.groupList = new ArrayList<>();
    }

    public int getID() {
        return this.id;
    }

    public void setID(int id) {
        this.id = id;
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public List<Group> getGroupList() {
        return groupList;
    }

    public void setGroupList(List<Group> groupList) {
        this.groupList = groupList;
    }
}
