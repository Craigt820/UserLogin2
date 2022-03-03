package com.idi.userlogin.Controllers;

import com.idi.userlogin.Handlers.ConnectionHandler;
import com.idi.userlogin.utils.DBUtils;
import com.idi.userlogin.utils.ImgFactory;
import com.idi.userlogin.utils.Utils;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.SearchableComboBox;
import org.controlsfx.control.textfield.CustomTextField;
import com.idi.userlogin.JavaBeans.Collection;
import com.idi.userlogin.JavaBeans.Group;
import com.idi.userlogin.JavaBeans.Item;
import com.idi.userlogin.Main;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.*;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import static com.idi.userlogin.Handlers.JsonHandler.trackPath;
import static com.idi.userlogin.JavaBeans.User.COMP_NAME;
import static com.idi.userlogin.Main.fxTrayIcon;
import static com.idi.userlogin.Main.jsonHandler;
import static com.idi.userlogin.utils.ImgFactory.IMGS.CHECKMARK;
import static com.idi.userlogin.utils.ImgFactory.IMGS.EXMARK;

public class UserEntryViewController extends BaseEntryController<BaseEntryController.EntryItem> {

    private final EntryItem treeRoot = new EntryItem();
    private final RecursiveTreeItem rootItem = new RecursiveTreeItem<>(treeRoot, RecursiveTreeObject::getChildren);
    private String groupCol;

    @Override
    public void legalTextTest(boolean isLegal, CustomTextField node) {
        if (!isLegal) {
            node.setRight(ImgFactory.createView(EXMARK));
            node.setTooltip(new Tooltip("May Contain a Special Character (?\\/:*\"<>|) or Is Empty!"));
            errorLbl.setText("Empty or Contains a Special Character (?\\/:*\"<>|) ");
            errorLbl.setVisible(true);
            insertBtn.setDisable(true);
        } else {
            node.setRight(ImgFactory.createView(CHECKMARK));
            node.setTooltip(new Tooltip("Legal Text"));
            errorLbl.setVisible(false);
            insertBtn.setDisable(false);
        }
        node.getRight().setStyle("-fx-translate-x:-8;");
    }

    @PostConstruct
    @Override
    public void afterInitialize() {
        super.afterInitialize();
        groupTree.getSelectionModel().selectedItemProperty().addListener((ob, ov, nv) -> {
            groupSelectTask(nv.getValue(), tree);
        });

        colCombo.setCellFactory(e -> {
            ListCell<com.idi.userlogin.JavaBeans.Collection> col = new ListCell<com.idi.userlogin.JavaBeans.Collection>() {
                @Override
                protected void updateItem(com.idi.userlogin.JavaBeans.Collection item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null && !item.getName().isEmpty()) {
                        setText(item.getName());
                    }
                }
            };
            return col;
        });
    }

    @Override
    public void updateItem(String paramString1, String paramString2, Item paramItem) {

    }

    @Override
    public void updateName(String paramString1, String paramString2, Item paramItem) {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        entryController = this;
        tree.setRoot(rootItem);
        afterInitialize();
    }

    @FXML
    private void iconify() {
//        Stage stage = FXUtils.getStageFromNode(mainPane);
//        stage.setIconified(true);
    }

    @FXML
    private void close() throws IOException {
//        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Close the application?", ButtonType.YES, ButtonType.NO);
//        Optional<ButtonType> wait = alert.showAndWait();
//        if (wait.get().equals(ButtonType.YES)) {
//            ImgUtils.deleteTempDir();
//            Main.getStage().close();
//
//            String[] split = ManagementFactory.getRuntimeMXBean().getName().split("@");
//            Runtime.getRuntime().exec("taskkill /F /PID " + split[0]);
//        }
    }

    @FXML
    private void restore() {
//        Stage stage = FXUtils.getStageFromNode(mainPane);
//        if (stage.isMaximized()) {
//            stage.setMaximized(false);
//        } else {
//            stage.setMaximized(true);
//        }
    }

    @Override
    @FXML
    public void insert() throws IOException {

        final String name = nameField.getText();
        final String type = typeCombo.getSelectionModel().getSelectedItem().getText();
        final List<String> conditions = conditCombo.getCheckModel().getCheckedItems();
        final String comments = commentsField.getText();
        final Group group = groupTree.getSelectionModel().getSelectedItem().getValue();
        boolean isPresent = tree.getRoot().getChildren().stream().anyMatch(e -> e.getValue().getType().getText().equals(type) && e.getValue().getName().toLowerCase().equals(name.toLowerCase()));
        if (!isPresent) {
            if (!name.isEmpty()) {
                nameField.setRight(ImgFactory.createView(CHECKMARK));
                final EntryItem item = new EntryItem(group.getCollection(), group.getG_Parent(), StringUtils.trim(name), type, comments);
                final TreeItem newItem = new TreeItem<>(item);
                item.setComments(commentsField.getText());
                item.getConditions().setAll(conditions);
                int id = insertHelper(item); //Insert Data into DB
                if (id > 0) { //if insert successful
                    item.setId(id);

                    //if group has no items, initialize the group and take ownership
                    if (tree.getRoot().getChildren().isEmpty()) {
                        initSelGroup(group);
                    }

                    item.setLocation(Paths.get(trackPath + "\\" + jsonHandler.getSelJobID() + "\\" + buildFolderStruct(item.id.get(), item) + "\\" + name.trim()));
                    tree.getRoot().getChildren().add(newItem);
                    nameField.clear();
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

        } else {
            errorLbl.setText("Name Already Exists!");
            errorLbl.setVisible(true);
        }
        nameField.getRight().setStyle("-fx-translate-x:-8;");
    }

    @Override
    public int insertHelper(Item<? extends Item> item) {
        Connection connection = null;
        ResultSet set = null;
        PreparedStatement ps = null;
        int key = 0;
        try {
            connection = ConnectionHandler.createDBConnection();
            ps = connection.prepareStatement("INSERT INTO `" + DBUtils.DBTable.D.getTable() + "` (name,group_id,started_on,employee_id,type_id,comments) VALUES(?,?,?,(SELECT id FROM employees WHERE employees.name= '" + ConnectionHandler.user.getName() + "'),(SELECT id FROM item_types WHERE item_types.name='" + item.getType().getText() + "'),?)", PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, item.getName());
            ps.setInt(2, item.getGroup().getID());
            Date now = formatDateTime(item.getStarted_On());
            ps.setTimestamp(3, new Timestamp(now.toInstant().toEpochMilli()));
            ps.setString(4, item.getComments());
            ps.executeUpdate();
            set = ps.getGeneratedKeys();

        } catch (SQLException | ParseException e) {
            e.printStackTrace();
        } finally {
            try {
                set = ps.getGeneratedKeys();
                if (set.next()) {
                    key = set.getInt(1);
                }
                item.setId(key);

            } catch (SQLException e) {
                e.printStackTrace();
                Main.LOGGER.log(Level.SEVERE, "There was an error inserting a new item!", e);
            }
            DbUtils.closeQuietly(set);
            DbUtils.closeQuietly(ps);
            DbUtils.closeQuietly(connection);
            updateItemProps(item);
        }
        return key;
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
            ps = connection.prepareStatement("SELECT d.workstation,d.id,d.id as group_id, d.name as group_name, d.name as item,d.non_feeder, d.completed, e.name as employee, d.total, t.name as type,d.conditions,d.comments,d.started_On,d.completed_On FROM `" +DBUtils.DBTable.D.getTable() + "` d INNER JOIN employees e ON m.employee_id = e.id INNER JOIN `" + DBUtils.DBTable.G.getTable() + "` g ON m.group_id = g.id INNER JOIN item_types t ON m.type_id = t.id WHERE group_id=" + group.getID() + "");
            set = ps.executeQuery();
            while (set.next()) {
                final EntryItem item = new EntryItem(set.getInt("m.id"),group.getCollection(), group, set.getString("item"), set.getInt("m.total"), set.getInt("m.non_feeder"), set.getString("type"), set.getInt("m.completed") == 1, set.getString("m.comments"), set.getString("m.started_On"), set.getString("m.completed_On"), set.getString("m.workstation"));
                String condition = set.getString("m.conditions");
                if (condition != null && !condition.isEmpty()) {
                    String[] splitConditions = condition.split(", ");
                    item.getConditions().setAll(Arrays.asList(splitConditions));
                }
                group_items.add(item);
                Platform.runLater(() -> {
                    indicator.setProgress(progress.get());
                });
                progress.incrementAndGet();
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
        nameField.setText("");
        conditCombo.getCheckModel().clearChecks();
        commentsField.setText("");
    }


    public AnchorPane getRoot() {
        return root;
    }

    public SearchableComboBox<Collection> getColCombo() {
        return colCombo;
    }


    public JFXTreeTableView<?> getTree() {
        return tree;
    }

    public String getGroupCol() {
        return groupCol;
    }

    public void setGroupCol(String groupCol) {
        this.groupCol = groupCol;
    }
}
