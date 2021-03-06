package com.idi.userlogin.JavaBeans;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.commons.dbutils.DbUtils;
import com.idi.userlogin.Controllers.ConnectionHandler;
import com.idi.userlogin.Main;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.idi.userlogin.Controllers.ControllerHandler.updateSelected;

public abstract class Item<K> extends RecursiveTreeObject<K> {

    public static final Image folderIcon = new Image(Item.class.getResourceAsStream("/images/folder.png"));
    public static final Image fileIcon = new Image(Item.class.getResourceAsStream("/images/file.png"));
    public SimpleIntegerProperty id;
    public SimpleStringProperty started_On;
    public SimpleStringProperty completed_On;
    public SimpleBooleanProperty completed_prop;
    public Label delete;
    public Label name;
    public Label type;
    public CheckBox completed;
    public Label details;
    public SimpleStringProperty comments;
    public SimpleListProperty<String> conditions;
    public List<String> scanners;
    public Collection collection;
    public Group group;
    public SimpleIntegerProperty nonFeeder;
    public SimpleIntegerProperty total;
    public Path location;
    public List<Image> previews;
    public SimpleMapProperty<String,String> projColumns;

    public SimpleIntegerProperty countProperty() {
        if (total == null) {
            total = new SimpleIntegerProperty(this, "count");
        }
        return total;
    }

    public static void removeItem(Item item) {
        Connection connection = null;
        ResultSet set = null;
        PreparedStatement ps = null;
        int key = 0;
        try {
            connection = ConnectionHandler.createDBConnection();
            ps = connection.prepareStatement("DELETE FROM `" + Main.jsonHandler.getSelJobID() + "` WHERE id=" + item.getId() + "");
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(ps);
            DbUtils.closeQuietly(connection);
        }
    }

    public Item(int id, Collection collection, Group group, String name, int total, int non_feeder, String type, boolean completed, String comments, String startedOn, String completedOn) {
        this();

        this.id.set(id);
        this.group = group;
        this.collection = collection;
        this.name.setText(name);
        this.total.set(total);
        this.nonFeeder.set(non_feeder);
        this.type.setText(type);
        setupType(type);
        this.completed.setSelected(completed);
        this.completed_prop = new SimpleBooleanProperty();
        this.completed_prop.bindBidirectional(this.completed.selectedProperty());
        this.comments.set(comments);
        this.started_On.set(startedOn);
        this.completed_On.set(completedOn);
        this.completed.selectedProperty().addListener(e -> {
            this.completed.setSelected(this.completed.isSelected());
            if (this.completed.isSelected()) {
                this.completed_On.set(LocalDateTime.now().toString());
            } else {
                this.completed_On.set(null);
            }

            updateSelected(this);
        });
    }


    public void setupType(String type) {
        final ImageView view = new ImageView();
        switch (type) {
            case "Folder":
            case "Root":
                view.setImage(folderIcon);
                break;
            case "Multi-Paged":
                view.setImage(fileIcon);
                break;
        }
        this.type.setGraphic(view);
        view.setFitWidth(20);
        view.setFitHeight(20);
        this.type.setGraphicTextGap(16);
        this.type.setGraphic(view);
        this.type.setTooltip(new Tooltip(type));
        this.type.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    public Item() {

        this.details = new Label("Details");
        this.details.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        this.details.setGraphic(new ImageView(getClass().getResource("/images/info.png").toExternalForm()));
        this.details.setTranslateX(0);
        this.details.setTooltip(new Tooltip("Details"));
        this.details.getGraphic().maxHeight(8);
        this.details.getGraphic().maxWidth(8);
        this.details.getStyleClass().add("detailBtn");
        this.id = new SimpleIntegerProperty(0);
        this.name = new Label();
        this.nonFeeder = new SimpleIntegerProperty(0);
        this.completed = new CheckBox();
        this.comments = new SimpleStringProperty("");
        this.type = new Label();
        this.delete = new Label("Remove");
        this.delete.getStyleClass().add("detailBtn");
        this.delete.setStyle("-fx-text-fill: red; -fx-opacity: .8;");
        this.completed.setPadding(new Insets(24, 24, 24, 24));
        this.total = new SimpleIntegerProperty(0);
        this.conditions = new SimpleListProperty<>();
        this.conditions.set(FXCollections.observableArrayList());
        this.projColumns = new SimpleMapProperty<>();
        this.projColumns.set(FXCollections.observableHashMap());
        this.started_On = new SimpleStringProperty();
        this.completed_On = new SimpleStringProperty();
        this.previews = new ArrayList<>();
    }

    public static Image getFolderIcon() {
        return folderIcon;
    }

    public static Image getFileIcon() {
        return fileIcon;
    }

    public int getId() {
        return id.get();
    }

    public SimpleIntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public Label getDelete() {
        return delete;
    }

    public void setDelete(Label delete) {
        this.delete = delete;
    }

    public Label getName() {
        return name;
    }

    public void setName(Label name) {
        this.name = name;
    }

    public Path getLocation() {
        return location;
    }

    public void setLocation(Path location) {
        this.location = location;
    }

    public Label getType() {
        return type;
    }

    public void setType(Label type) {
        this.type = type;
    }

    public CheckBox getCompleted() {
        return completed;
    }

    public void setCompleted(CheckBox completed) {
        this.completed = completed;
    }

    public Label getDetails() {
        return details;
    }

    public void setDetails(Label details) {
        this.details = details;
    }

    public String getComments() {
        return comments.get();
    }

    public SimpleStringProperty commentsProperty() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments.set(comments);
    }

    public void setCompleted_prop(boolean completed_prop) {
        this.completed_prop.set(completed_prop);
    }

    public ObservableList<String> getConditions() {
        return conditions.get();
    }

    public SimpleListProperty<String> conditionsProperty() {
        return conditions;
    }

    public void setConditions(ObservableList<String> conditions) {
        this.conditions.set(conditions);
    }

    public void setCollection(Collection collection) {
        this.collection = collection;
    }

    public List<String> getScanners() {
        return scanners;
    }

    public void setScanners(List<String> scanners) {
        this.scanners = scanners;
    }

    public Collection getCollection() {
        return collection;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String getStarted_On() {
        return started_On.get();
    }

    public SimpleStringProperty started_OnProperty() {
        return started_On;
    }

    public void setStarted_On(String started_On) {
        this.started_On.set(started_On);
    }

    public String getCompleted_On() {
        return completed_On.get();
    }

    public SimpleStringProperty completed_OnProperty() {
        return completed_On;
    }

    public void setCompleted_On(String completed_On) {
        this.completed_On.set(completed_On);
    }

    public int getNonFeeder() {
        return nonFeeder.get();
    }

    public SimpleIntegerProperty nonFeederProperty() {
        return nonFeeder;
    }

    public void setNonFeeder(int nonFeeder) {
        this.nonFeeder.set(nonFeeder);
    }

    public int getTotal() {
        return total.get();
    }

    public SimpleIntegerProperty totalProperty() {
        return total;
    }

    public void setTotal(int total) {
        this.total.set(total);
    }

    public List<Image> getPreviews() {
        return previews;
    }

    public void setPreviews(List<Image> previews) {
        this.previews = previews;
    }

    public boolean isCompleted_prop() {
        return completed_prop.get();
    }

    public SimpleBooleanProperty completed_propProperty() {
        return completed_prop;
    }

}