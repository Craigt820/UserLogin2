package com.idi.userlogin.JavaBeans;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;

public class Group extends ListCell<Group> {
    private IntegerProperty id;
    private Collection collection;
    private StringProperty name;
    private String started_On;
    private String completed_On;
    private BooleanProperty complete;
    private ObservableList itemList;

    public Group(String name) {
        this.name = new SimpleStringProperty(name);
    }

    public Group(int id, Collection collection, String name, boolean complete, String started_On, String completed_On) {

        this.id = new SimpleIntegerProperty(id);
        this.collection = collection;
        this.name = new SimpleStringProperty(name);
        this.complete = new SimpleBooleanProperty(complete);
        this.started_On = started_On;
        this.completed_On = completed_On;
        this.itemList = FXCollections.observableArrayList();
    }

    public int getID() {
        return id.get();
    }

    public IntegerProperty IDProperty() {
        return id;
    }

    public void setID(int id) {
        this.id.set(id);
    }

    public Collection getCollection() {
        return collection;
    }

    public void setCollection(Collection collection) {
        this.collection = collection;
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

    public String getStarted_On() {
        return started_On;
    }

    public void setStarted_On(String started_On) {
        this.started_On = started_On;
    }

    public String getCompleted_On() {
        return completed_On;
    }

    public void setCompleted_On(String completed_On) {
        this.completed_On = completed_On;
    }

    public boolean isComplete() {
        return complete.get();
    }

    public BooleanProperty completeProperty() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete.set(complete);
    }

    public ObservableList<? extends Item> getItemList() {
        return itemList;
    }

    public void setItemList(ObservableList<? extends Item> itemList) {
        this.itemList = itemList;
    }
}
