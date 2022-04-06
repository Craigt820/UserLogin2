package com.idi.userlogin.JavaBeans;

import com.idi.userlogin.Handlers.JsonHandler;
import com.idi.userlogin.utils.DBUtils;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.commons.dbutils.DbUtils;
import com.idi.userlogin.Handlers.ConnectionHandler;
import com.idi.userlogin.Main;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.idi.userlogin.Handlers.ControllerHandler.updateSelected;

public abstract class Item<K> extends RecursiveTreeObject<K> {

    public static final Image folderIcon = new Image(Item.class.getResourceAsStream("/images/folder.png"));
    public static final Image fileIcon = new Image(Item.class.getResourceAsStream("/images/file.png"));
    public BooleanProperty exists; //File/Folder
    public SimpleIntegerProperty id;
    public SimpleStringProperty started_On;
    public SimpleStringProperty completed_On;
    public Label delete;
    public SimpleStringProperty name;
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
    public SimpleMapProperty<String, String> projColumns;
    public SimpleStringProperty workstation;

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
        try {
            connection = ConnectionHandler.createDBConnection();
            ps = connection.prepareStatement("DELETE FROM `" + JsonHandler.getSelJob().getJob_id() + "" + DBUtils.DBTable.D.getTable() + "` WHERE id=" + item.getId() + "");
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(ps);
            DbUtils.closeQuietly(connection);
        }
    }

    public Item(int id, Collection collection, Group group, String name, int total, int non_feeder, String type, boolean completed, String comments, String startedOn, String completedOn,String workstation) {
        this();
        this.exists = new SimpleBooleanProperty(false);
        this.id.set(id);
        this.group = group;
        this.collection = collection;
        this.name.set(name);
        this.total.set(total);
        this.nonFeeder.set(Integer.parseInt(String.valueOf(non_feeder)));
        this.type.setText(type);
        setupType(type);
        this.completed.setSelected(completed);
        this.comments.set(comments);
        this.started_On.set(startedOn);
        this.completed_On.set(completedOn);
        this.workstation.set(workstation);
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
        view.setFitWidth(24);
        view.setFitHeight(24);
        this.type.setGraphicTextGap(16);
        this.type.setGraphic(view);
        this.type.setTooltip(new Tooltip(type));
        this.type.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    public Item() {
        this.details = new Label("Details");
        this.details.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        ImageView details = new ImageView(getClass().getResource("/images/info.png").toExternalForm());
        details.setFitWidth(24);
        details.setFitHeight(24);
        this.details.setGraphic(details);
        this.details.setTranslateX(0);
        this.details.setLayoutY(-2);
        this.details.setTooltip(new Tooltip("Details"));
        this.details.getGraphic().maxHeight(8);
        this.details.getGraphic().maxWidth(8);
        this.details.getStyleClass().add("detailBtn");
        this.id = new SimpleIntegerProperty(0);
        this.name = new SimpleStringProperty();
        this.nonFeeder = new SimpleIntegerProperty(0);
        this.completed = new CheckBox();
        this.completed.setContextMenu(new ContextMenu());
        this.comments = new SimpleStringProperty("");
        this.type = new Label();
        this.delete = new Label("Remove");
        this.delete.getStyleClass().add("detailBtn");
        this.delete.setStyle("-fx-font-size:1em;-fx-text-fill: red; -fx-opacity: .8;");
        this.completed.setPadding(new Insets(24, 24, 24, 24));
        this.total = new SimpleIntegerProperty(0);
        this.conditions = new SimpleListProperty<>();
        this.conditions.set(FXCollections.observableArrayList());
        this.projColumns = new SimpleMapProperty<>();
        this.projColumns.set(FXCollections.observableHashMap());
        this.started_On = new SimpleStringProperty();
        this.completed_On = new SimpleStringProperty();
        this.workstation = new SimpleStringProperty();
        this.previews = new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item<?> item = (Item<?>) o;
        return Objects.equals(exists, item.exists) && Objects.equals(id, item.id) && Objects.equals(started_On, item.started_On) && Objects.equals(completed_On, item.completed_On) && Objects.equals(delete, item.delete) && Objects.equals(name, item.name) && Objects.equals(type, item.type) && Objects.equals(completed, item.completed) && Objects.equals(details, item.details) && Objects.equals(comments, item.comments) && Objects.equals(conditions, item.conditions) && Objects.equals(scanners, item.scanners) && Objects.equals(collection, item.collection) && Objects.equals(group, item.group) && Objects.equals(nonFeeder, item.nonFeeder) && Objects.equals(total, item.total) && Objects.equals(location, item.location) && Objects.equals(previews, item.previews) && Objects.equals(projColumns, item.projColumns) && Objects.equals(workstation, item.workstation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exists, id, started_On, completed_On, delete, name, type, completed, details, comments, conditions, scanners, collection, group, nonFeeder, total, location, previews, projColumns, workstation);
    }

    public boolean isExists() {
        return exists.get();
    }

    public BooleanProperty existsProperty() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists.set(exists);
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

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
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
        this.nonFeeder.set(Integer.parseInt(String.valueOf(nonFeeder)));
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

    public String getWorkstation() {
        return workstation.get();
    }

    public SimpleStringProperty workstationProperty() {
        return workstation;
    }

    public void setWorkstation(String workstation) {
        this.workstation.set(workstation);
    }

    public List<Image> getPreviews() {
        return previews;
    }

    public void setPreviews(List<Image> previews) {
        this.previews = previews;
    }

}