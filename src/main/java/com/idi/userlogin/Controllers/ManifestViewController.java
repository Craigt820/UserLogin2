package com.idi.userlogin.Controllers;

import com.idi.userlogin.Handlers.ConnectionHandler;
import com.idi.userlogin.Handlers.ControllerHandler;
import com.idi.userlogin.Handlers.JsonHandler;
import com.idi.userlogin.JavaBeans.Collection;
import com.idi.userlogin.JavaBeans.Group;
import com.idi.userlogin.JavaBeans.Item;
import com.idi.userlogin.Main;
import com.idi.userlogin.utils.DailyLog;
import com.idi.userlogin.utils.ImgFactory;
import com.idi.userlogin.utils.Utils;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.AnchorPane;
import javafx.util.StringConverter;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.SearchableComboBox;
import org.controlsfx.control.textfield.CustomTextField;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static com.idi.userlogin.Handlers.JsonHandler.COMP_NAME;
import static com.idi.userlogin.Main.fxTrayIcon;
import static com.idi.userlogin.Main.jsonHandler;
import static com.idi.userlogin.utils.DailyLog.scanLogID;
import static com.idi.userlogin.utils.ImgFactory.IMGS.CHECKMARK;

public class ManifestViewController extends BaseEntryController<BaseEntryController.EntryItem> implements Initializable {

    private final EntryItem treeRoot = new EntryItem();
    private final RecursiveTreeItem rootItem = new RecursiveTreeItem<>(treeRoot, RecursiveTreeObject::getChildren);
    private String uid;

    @FXML
    private SearchableComboBox<Group> groupCombo;

    @FXML
    private SearchableComboBox<EntryItem> itemCombo;

    @Override
    @FXML
    public void insert() throws IOException {

        final EntryItem item = itemCombo.getSelectionModel().getSelectedItem();
        final String type = typeCombo.getSelectionModel().getSelectedItem().getText();
        final List<String> conditions = conditCombo.getCheckModel().getCheckedItems();
        final String comments = commentsField.getText();
        final Group group = groupCombo.getSelectionModel().getSelectedItem();
        if (item != null) {
            final TreeItem newItem = new TreeItem<>(item);
            item.setWorkstation(COMP_NAME);
            item.setStarted_On(LocalDateTime.now().toString());
            item.setGroup(group);
            item.getType().setText(type);
            item.setupType(type);
            item.setComments(commentsField.getText());
            item.getConditions().setAll(conditions);
            insertHelper(item);
            tree.getRoot().getChildren().add(newItem);
            typeCombo.getSelectionModel().selectFirst();
            conditCombo.getCheckModel().clearChecks();
            errorLbl.setVisible(false);
            final ObservableList items = group.getItemList();
            items.add(item);
            fxTrayIcon.showInfoMessage("Item: " + item.getName() + " Inserted");
            createFoldersFromStruct(item);
            resetFields();
        }
    }

    @Override
    public int insertHelper(Item<? extends Item> item) {
        Connection connection = null;
        ResultSet set = null;
        PreparedStatement ps = null;
        int key = 0;
        try {
            connection = ConnectionHandler.createDBConnection();
            ps = connection.prepareStatement("UPDATE `" + Main.jsonHandler.getSelJobID() + "` SET employee_id=(SELECT id FROM employees WHERE employees.name= '" + Main.jsonHandler.getName() + "'), type_id=(SELECT id FROM item_types WHERE name = '" + item.getType().getText() + "'),started_On=?,collection_id=?,group_id=?,comments=?,workstation=? WHERE id=?");
            Date now = formatDateTime(item.getStarted_On());
            ps.setTimestamp(1, new Timestamp(now.toInstant().toEpochMilli()));
            ps.setInt(2, item.getCollection().getID());
            ps.setInt(3, item.getGroup().getID());
            ps.setString(4, item.getComments());
            ps.setString(5, COMP_NAME);
            ps.setInt(6, item.getId());
            ps.executeUpdate();

        } catch (SQLException | ParseException e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(set);
            DbUtils.closeQuietly(ps);
            DbUtils.closeQuietly(connection);
            updateItemProps(item);
        }
        return key;
    }

    @Override
    public void updateItem(String paramString1, String paramString2, Item paramItem) {

    }

    @Override
    public void updateName(String paramString1, String paramString2, Item paramItem) {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        maniViewController = this;
        tree.setRoot(rootItem);
        afterInitialize();
    }

    @PostConstruct
    @Override
    public void afterInitialize() {
        super.afterInitialize();
        groupCombo.getSelectionModel().selectedItemProperty().addListener((ob, ov, nv) -> {
            groupSelectTask(nv, tree);
        });

        itemCombo.setCellFactory(e -> {
            ListCell<EntryItem> col = new ListCell<EntryItem>() {
                @Override
                protected void updateItem(EntryItem item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null && !item.getName().isEmpty()) {
                        setText(item.getName());
                    } else {
                        setText(null);
                        setGraphic(null);
                    }
                }
            };
            return col;
        });

        itemCombo.setButtonCell(new ListCell<EntryItem>() {
            @Override
            protected void updateItem(EntryItem item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !item.getName().isEmpty()) {
                    setText(item.getName());
                } else {
                    setText(null);
                    setGraphic(null);
                }
            }
        });

        itemCombo.setConverter(new StringConverter<EntryItem>() {
            @Override
            public String toString(EntryItem object) {
                return object.getName();
            }

            @Override
            public EntryItem fromString(String string) {
                return itemCombo.getItems().stream().filter(e -> e.getName().equals(string)).findAny().orElse(null);
            }
        });

    }

    @Override
    public void updateTotal() {

    }

    @Override
    public void groupSelectTask(Group nv, JFXTreeTableView<? extends Item> tree) {
        boolean sameVal = false;
        if (nv != null && !nv.getName().isEmpty()) {
            if (!nv.getName().equals(addGroupLabel)) {
                if (ControllerHandler.selGroup != null && nv == ControllerHandler.selGroup) {
                    sameVal = true;
                }
                if (!sameVal) {
                    //Daily Log already initialized
                    if (scanLogID != 0) {
                        DailyLog.endDailyLog();
                    }
                    ControllerHandler.selGroup = nv;
                    groupCountProp.set(0);
                    tree.getRoot().getChildren().clear();
                    selGroup.setText(nv.getName());
                    tree.setPlaceholder(indicator);
                    tree.getPlaceholder().autosize();
                    CompletableFuture.runAsync(() -> {
                        final ObservableList<EntryItem> comboItems = getItemsForCombo(ControllerHandler.selGroup);
                        FXCollections.sort(comboItems, new Comparator<Item>() {
                            @Override
                            public int compare(Item o1, Item o2) {
                                return o1.getName().compareTo(o2.getName());
                            }
                        });
                        itemCombo.getItems().addAll(comboItems);
                    }).supplyAsync(() -> {
                        final ObservableList<?> entryItems = getGroupItems(ControllerHandler.selGroup);
                        return entryItems;
                    }).thenApplyAsync(entryItems -> {
                        final ObservableList group = ControllerHandler.selGroup.getItemList();
                        group.setAll(entryItems);
                        final List itemList = entryItems.stream().map(TreeItem::new).collect(Collectors.toList());
                        return itemList;
                    }).thenApplyAsync(itemList -> {
                        tree.getRoot().getChildren().addAll(itemList);
                        return itemList;
                    }).thenRunAsync(() -> {
                        Platform.runLater(() -> {
                            groupCountProp.set(countGroupTotal());
                            tree.refresh();
                        });
                        //Start a new log entry
                        DailyLog.insertNewDailyLog(nv.getID());
                    });
                }
                insertBtn.setDisable(false);
            }
        }
    }

    public ObservableList<EntryItem> getItemsForCombo(Group group) {
        Connection connection = null;
        ResultSet set = null;
        PreparedStatement ps = null;
        ObservableList<EntryItem> group_items = FXCollections.observableArrayList();
        try {
            connection = ConnectionHandler.createDBConnection();
            ps = connection.prepareStatement("SELECT m.id," + uid + " as item FROM `" + Main.jsonHandler.getSelJobID() + "` m WHERE employee_id IS NULL");
            set = ps.executeQuery();
            while (set.next()) {
                final EntryItem item = new EntryItem(set.getInt("m.id"), group.getCollection(), group, set.getString("item"), 0, 0, "", false, "", "", "", "", false);
                group_items.add(item);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Main.LOGGER.log(Level.SEVERE, "There was an error getting the groups from the db!", e);

        } finally {
            DbUtils.closeQuietly(set);
            DbUtils.closeQuietly(ps);
            DbUtils.closeQuietly(connection);
        }

        return group_items;
    }

    @Override
    public ObservableList<? extends Item> getGroupItems(Group group) {
        Connection connection = null;
        ResultSet set = null;
        PreparedStatement ps = null;
        ObservableList<EntryItem> group_items = FXCollections.observableArrayList();
        AtomicInteger progress = new AtomicInteger(0);
        try {
            connection = ConnectionHandler.createDBConnection();
            ps = connection.prepareStatement("SELECT m.workstation,m.overridden,m.id,g.id as group_id, g.name as group_name, m." + uid + " as item,m.non_feeder, m.completed, e.name as employee, c.name as collection, m.total, t.name as type,m.conditions,m.comments,m.started_On,m.completed_On FROM `" + Main.jsonHandler.getSelJobID() + "` m INNER JOIN employees e ON m.employee_id = e.id INNER JOIN sc_groups g ON m.group_id = g.id INNER JOIN item_types t ON m.type_id = t.id INNER JOIN sc_collections c ON m.collection_id = c.id WHERE m.group_id=" + group.getID() + " AND employee_id=(SELECT id FROM employees WHERE name='" + jsonHandler.getName() + "')");
            set = ps.executeQuery();
            while (set.next()) {
                final EntryItem item = new EntryItem(set.getInt("m.id"), group.getCollection(), group, set.getString("item"), set.getInt("m.total"), set.getInt("m.non_feeder"), set.getString("type"), set.getInt("m.completed") == 1, set.getString("m.comments"), set.getString("m.started_On"), set.getString("m.completed_On"), set.getString("m.workstation"), Utils.intToBoolean(set.getInt("m.overridden")));
                final String condition = set.getString("m.conditions");
                if (condition != null && !condition.isEmpty()) {
                    final String[] splitConditions = condition.split(", ");
                    item.getConditions().setAll(Arrays.asList(splitConditions));
                }
                group_items.add(item);
                Platform.runLater(() -> {
                    indicator.setProgress(progress.get());
                    progress.incrementAndGet();
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Main.LOGGER.log(Level.SEVERE, "There was an error getting the groups from the db!", e);

        } finally {
            DbUtils.closeQuietly(set);
            DbUtils.closeQuietly(ps);
            DbUtils.closeQuietly(connection);
        }

        return group_items;
    }

    @Override
    public void resetFields() {

    }


    @Override
    public void legalTextTest(boolean isLegal, CustomTextField node) {

    }

    public AnchorPane getRoot() {
        return root;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
