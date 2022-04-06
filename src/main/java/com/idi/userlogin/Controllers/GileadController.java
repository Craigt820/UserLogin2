//package com.idi.userlogin.Controllers;
//
//import com.idi.userlogin.Handlers.ConnectionHandler;
//import com.idi.userlogin.Handlers.ControllerHandler;
//import com.idi.userlogin.JavaBeans.Collection;
//import com.idi.userlogin.JavaBeans.Group;
//import com.idi.userlogin.JavaBeans.Item;
//import com.idi.userlogin.Main;
//import com.idi.userlogin.utils.DailyLog;
//import com.idi.userlogin.utils.Utils;
//import com.jfoenix.controls.JFXTreeTableColumn;
//import com.jfoenix.controls.JFXTreeTableView;
//import com.jfoenix.controls.RecursiveTreeItem;
//import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
//import javafx.application.Platform;
//import javafx.beans.property.SimpleStringProperty;
//import javafx.beans.property.StringProperty;
//import javafx.beans.value.ChangeListener;
//import javafx.beans.value.ObservableValue;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.fxml.FXML;
//import javafx.fxml.Initializable;
//import javafx.scene.control.*;
//import javafx.scene.control.cell.ComboBoxTreeTableCell;
//import javafx.scene.control.cell.TreeItemPropertyValueFactory;
//import javafx.scene.layout.AnchorPane;
//import javafx.util.Duration;
//import org.apache.commons.dbutils.DbUtils;
//import org.controlsfx.control.SearchableComboBox;
//import org.controlsfx.control.textfield.CustomTextField;
//
//import javax.annotation.PostConstruct;
//import java.io.IOException;
//import java.net.URL;
//import java.nio.file.Paths;
//import java.sql.*;
//import java.text.ParseException;
//import java.time.LocalDateTime;
//import java.util.Date;
//import java.util.*;
//import java.util.concurrent.CompletableFuture;
//import java.util.logging.Level;
//import java.util.stream.Collectors;
//
//import static com.idi.userlogin.Handlers.JsonHandler.trackPath;
//import static com.idi.userlogin.JavaBeans.User.COMP_NAME;
//import static com.idi.userlogin.Main.fxTrayIcon;
//import static com.idi.userlogin.Main.JsonHandler;
//import static com.idi.userlogin.utils.DailyLog.scanLogID;
//
//public class GileadController extends BaseEntryController<GileadController.GileadItem> implements Initializable {
//    private final ArrayList<String> STATUS = new ArrayList<>(Arrays.asList("BLINDED", "UNBLINDED"));
//    private String uid;
//    private String groupCol;
//    private final String addDocLabel = "Add New Doc Type";
//    private final ObservableList<DocType> DOC_TYPES = FXCollections.observableArrayList();
//    private final GileadItem treeRoot = new GileadItem();
//    private final RecursiveTreeItem<GileadItem> rootItem = new RecursiveTreeItem<>(treeRoot, RecursiveTreeObject::getChildren);
//
//    @FXML
//    protected JFXTreeTableView<GileadItem> tree;
//    @FXML
//    protected JFXTreeTableColumn<GileadItem, DocType> dtColumn;
//    @FXML
//    protected JFXTreeTableColumn<GileadItem, String> statusColumn;
//    @FXML
//    protected JFXTreeTableColumn<GileadItem, String> mcnColumn;
//    @FXML
//    protected JFXTreeTableColumn<GileadItem, String> pcColumn;
//    @FXML
//    private CustomTextField pcField, mcnField;
//    @FXML
//    protected SearchableComboBox<DocType> dtCombo; //Doc Type
//    @FXML
//    private SearchableComboBox<String> statusCombo, folderCombo;
//    @FXML
//    private PasswordField ss;
//    @FXML
//    private Label editDocListBtn;
//
//    @Override
//    public ObservableList<? extends Item> getGroupItems(Group group) {
//        Connection connection = null;
//        ResultSet set = null;
//        PreparedStatement ps = null;
//        ObservableList<GileadItem> group_items = FXCollections.observableArrayList();
//
//        try {
//            connection = ConnectionHandler.createDBConnection();
//            ps = connection.prepareStatement("SELECT g_dt.id,g_dt.name,g_dt.mcn,m.workstation,m.overridden,m.mcn,m.pc,m.doc_type,m.name,m.status, m.id,g.id as group_id, g.name as group_name, m.completed, e.name as employee, c.name as collection, m.total,t.name as type,m.conditions,m.started_On,m.completed_On,m.comments FROM `" + Main.JsonHandler.getSelJob().getName() + "` m INNER JOIN employees e ON m.employee_id = e.id INNER JOIN `" + JsonHandler.getSelJob().getName() + "_g` g ON m.group_id = g.id INNER JOIN item_types t ON m.type_id = t.id INNER JOIN sc_collections c ON m.collection_id = c.id INNER JOIN gilead_dt g_dt ON m.doc_type = g_dt.name WHERE group_id=" + group.getID() + "");
//            set = ps.executeQuery();
//            while (set.next()) {
//                final GileadItem item = new GileadItem(set.getInt("m.id"), group.getCollection(), group, set.getString("m.name"), set.getString("pc"), set.getString("mcn"), new DocType(set.getInt("g_dt.id"), set.getString("g_dt.name"), Utils.intToBoolean(set.getInt("g_dt.mcn"))), set.getString("status"), set.getInt("m.total"), set.getInt("m.completed") == 1, "Multi-Paged", null, set.getString("m.comments"), set.getString("m.started_On"), set.getString("m.completed_On"), set.getString("m.workstation"), Utils.intToBoolean(set.getInt("m.overridden")));
//                String condition = set.getString("m.conditions");
//                if (condition != null && !condition.isEmpty()) {
//                    String[] splitConditions = condition.split(", ");
//                    item.getConditions().setAll(Arrays.asList(splitConditions));
//                }
//                group_items.add(item);
//            }
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//            Main.LOGGER.log(Level.SEVERE, "There was an error getting the groups from the db!", e);
//
//        } finally {
//            DbUtils.closeQuietly(set);
//            DbUtils.closeQuietly(ps);
//            DbUtils.closeQuietly(connection);
//        }
//
//        return group_items;
//    }
//
//    @PostConstruct
//    @Override
//    public void afterInitialize() {
//        gileadController = this;
//        tree.setRoot(rootItem);
//        super.afterInitialize();
//    }
//
//    @Override
//    public void updateItem(String column, String newValue, Item item) {
//        Connection connection = null;
//        PreparedStatement ps = null;
//        try {
//            connection = ConnectionHandler.createDBConnection();
//            ps = connection.prepareStatement("Update `" + Main.JsonHandler.getSelJob().getName() + "` SET `" + column + "`=? WHERE employee_id=" + ConnectionHandler.user.getId() + " AND id=?");
//            ps.setString(1, newValue);
//            ps.setInt(2, item.getId());
//            ps.executeUpdate();
//        } catch (SQLException e) {
//            e.printStackTrace();
//            Main.LOGGER.log(Level.SEVERE, "There was an error updating a field!", e);
//        } finally {
//            DbUtils.closeQuietly(ps);
//            DbUtils.closeQuietly(connection);
//        }
//    }
//
//    @Override
//    public void updateName(String oldValue, String newValue, Item item) {
//        Connection connection = null;
//        PreparedStatement ps = null;
//        try {
//            connection = ConnectionHandler.createDBConnection();
//            ps = connection.prepareStatement("Update `" + Main.JsonHandler.getSelJob().getName() + "` SET `name`=? WHERE employee_id=" + ConnectionHandler.user.getId() + " AND full_name=?");
//            ps.setString(1, newValue);
//            ps.setString(2, oldValue);
//            ps.executeUpdate();
//        } catch (SQLException e) {
//            e.printStackTrace();
//            Main.LOGGER.log(Level.SEVERE, "There was an error updating a field!", e);
//        } finally {
//            DbUtils.closeQuietly(ps);
//            DbUtils.closeQuietly(connection);
//        }
//        this.tree.getRoot().getChildren().stream().filter(e -> (e.getValue()).name.get().equals(oldValue)).forEach(e -> ((GileadItem) e.getValue()).name.set(newValue));
//    }
//
//    public void docTypeHelper(String oldtype, String newType) {
//
//        Connection connection = null;
//        ResultSet set = null;
//        PreparedStatement ps = null;
//        try {
//            connection = ConnectionHandler.createDBConnection();
//            ps = connection.prepareStatement("UPDATE `gilead_dt` SET name=? WHERE name=?");
//            ps.setString(1, newType);
//            ps.setString(2, oldtype);
//            ps.executeUpdate();
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//            Main.LOGGER.log(Level.SEVERE, "There was an error editing a doc type!", e);
//
//        } finally {
//            DbUtils.closeQuietly(set);
//            DbUtils.closeQuietly(ps);
//            DbUtils.closeQuietly(connection);
//        }
//    }
//
//    public static class DocType {
//        private int id;
//        private String name;
//        private Boolean mcn;
//
//        public DocType(int id, String name, Boolean mcn) {
//            this.id = id;
//            this.name = name;
//            this.mcn = mcn;
//        }
//
//        public int getId() {
//            return id;
//        }
//
//        public void setId(int id) {
//            this.id = id;
//        }
//
//        public String getName() {
//            return name;
//        }
//
//        public void setName(String name) {
//            this.name = name;
//        }
//
//        public Boolean getMcn() {
//            return mcn;
//        }
//
//        public void setMcn(Boolean mcn) {
//            this.mcn = mcn;
//        }
//
//        @Override
//        public boolean equals(Object o) {
//            if (this == o) return true;
//            if (o == null || getClass() != o.getClass()) return false;
//            DocType docType = (DocType) o;
//            return name.equals(docType.name);
//        }
//
//        @Override
//        public int hashCode() {
//            return Objects.hash(name);
//        }
//    }
//
//
//    @Override
//    public void initialize(URL location, ResourceBundle bundle) {
//        //We add a listener to when one of the name fields are changed, we then clear the SS field. (New Patient)
//        afterInitialize();
//        groupTree.getSelectionModel().selectedItemProperty().addListener((ob, ov, nv) -> {
//            groupSelectTask(nv.getValue(), tree);
//        });
//
//        DOC_TYPES.addAll(getDocTypes());
//        dtCombo.getItems().addAll(DOC_TYPES);
//        dtCombo.setCellFactory(e -> {
//            ListCell<DocType> cell = new ListCell<DocType>() {
//                @Override
//                protected void updateItem(DocType item, boolean empty) {
//                    super.updateItem(item, empty);
//                    if (item != null) {
//                        setText(item.name);
//                        setContentDisplay(ContentDisplay.TEXT_ONLY);
////                        if (!item.equals(addDocLabel)) {
////                            StackPane stackPane = new StackPane();
////                            Label text = new Label(item.name);
////                            HBox left = new HBox(text);
////                            left.setAlignment(Pos.CENTER_LEFT);
////                            Label edit = new Label("Edit");
////                            edit.getStyleClass().add("labels");
////                            edit.setStyle("-fx-text-fill: #07255E;");
////                            ChangeListener listViewShowListener = new ChangeListener<Boolean>() {
////                                @Override
////                                public void changed(ObservableValue observable, Boolean oldValue, Boolean newValue) {
////                                    if (!newValue) {
////                                        dtCombo.show();
////                                    }
////                                }
////                            };
//////
//////                            edit.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
//////                                dtCombo.showingProperty().addListener(listViewShowListener);
//////                                TextField textField = new TextField(item.name);
//////                                StackPane pane2 = new StackPane();
//////                                HBox root = new HBox();
//////                                root.setSpacing(8);
//////                                HBox left2 = new HBox();
//////                                HBox right2 = new HBox();
//////                                root.getChildren().addAll(left2, right2);
//////                                left2.setAlignment(Pos.CENTER_LEFT);
//////                                right2.setAlignment(Pos.CENTER_RIGHT);
//////                                HBox.setHgrow(left2, Priority.ALWAYS);
//////                                HBox.setHgrow(right2, Priority.ALWAYS);
//////                                Label cancelEdit = new Label("X");
//////                                cancelEdit.setMaxWidth(40);
//////                                cancelEdit.getStyleClass().add("labels");
//////                                cancelEdit.setStyle("-fx-text-fill: #07255E;");
//////                                left2.getChildren().add(textField);
//////                                right2.getChildren().add(cancelEdit);
//////                                pane2.getChildren().addAll(root);
//////
//////                                cancelEdit.addEventFilter(MouseEvent.MOUSE_PRESSED, e2 -> {
//////                                    setGraphic(stackPane);
//////                                    dtCombo.showingProperty().removeListener(listViewShowListener);
//////                                });
//////
//////                                textField.addEventFilter(KeyEvent.KEY_PRESSED, e2 -> {
//////                                    if (e2.getCode().equals(KeyCode.ENTER)) {
//////                                        System.out.println(textField.getText());
//////                                        docTypeHelper(text.getText(), textField.getText());
//////                                        fxTrayIcon.showInfoMessage("Doc Type: `" + text.getText() + "` Renamed To `" + textField.getText() + "`");
//////                                        text.setText(textField.getText());
//////                                        setText(textField.getText());
//////                                        dtCombo.getItems().set(getIndex(), textField.getText());
//////                                        setGraphic(stackPane);
//////                                        dtCombo.showingProperty().removeListener(listViewShowListener);
//////                                    }
//////                                });
//////
//////                                setGraphic(pane2);
//////                            });
////                            HBox right = new HBox(edit);
////                            right.setAlignment(Pos.CENTER_RIGHT);
////                            stackPane.getChildren().addAll(left, right);
////                            setGraphic(stackPane);
////                            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
////                        }
//                    } else {
//                        setText(null);
//                        setGraphic(null);
//                    }
//                }
//            };
//
////            //ADD NEW DOC TYPE was selected
////            cell.addEventFilter(MouseEvent.MOUSE_PRESSED, evt -> {
////
////                if (!cell.getItem().isEmpty() && !cell.isEmpty()) {
////
////                    if (cell.getItem().equals(addDocLabel)) {
////                        TextInputDialog dialog = new TextInputDialog();
////                        dialog.setContentText("Enter Type");
////
////                        dialog.showAndWait().ifPresent(text -> {
////                            String textUpper = text.toUpperCase();
////                            boolean noMatch = dtCombo.getItems().stream().noneMatch(e3 -> e3.toUpperCase().equals(textUpper));
////                            if (noMatch) {
////                                newDocType(textUpper);
////                                Platform.runLater(() -> {
////                                    DOC_TYPES.add(textUpper);
////                                    dtCombo.getItems().set(dtCombo.getItems().size() - 2, textUpper);
////                                    sortDTList();
////                                    final int index = dtCombo.getItems().indexOf(textUpper);
////                                    dtCombo.getSelectionModel().select(index);
////                                });
////
////                                fxTrayIcon.showInfoMessage("DocType '" + text + "' Has Been Created");
////                            } else {
////                                fxTrayIcon.showErrorMessage("DocType '" + text + "' Exists Already!");
////                            }
////                        });
////                    }
////                }
////            });
//            return cell;
//        });
//        dtCombo.setButtonCell(new ListCell<DocType>() {
//            @Override
//            protected void updateItem(DocType item, boolean empty) {
//                super.updateItem(item, empty);
//                if (item != null) {
//                    setText(item.getName());
//                } else {
//                    setGraphic(null);
//                    setText("");
//                }
//            }
//        });
//
//        nameColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("name"));
//        pcColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("pc"));
//        mcnColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("mcn"));
//        dtColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("docType"));
//        dtColumn.setCellFactory(e -> new ComboBoxTreeTableCell<GileadItem, DocType>(FXCollections.observableArrayList(DOC_TYPES)) {
//
//            @Override
//            public void updateItem(DocType item, boolean empty) {
//                super.updateItem(item, empty);
//            }
//
//            @Override
//            public void startEdit() {
//                super.startEdit();
//            }
//
//
//            @Override
//            public void cancelEdit() {
//                super.cancelEdit();
//            }
//
//            @Override
//            public void commitEdit(DocType newValue) {
//                GileadItem item = getTreeTableRow().getTreeItem().getValue();
//                item.setDocType(newValue);
//                ControllerHandler.updateItem(item, "UPDATE `" + JsonHandler.getSelJob().getName() + "` SET doc_type='" + item.getDocType() + "' WHERE id=" + item.id.get() + "");
//                super.commitEdit(newValue);
//            }
//        });
//        dtCombo.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<DocType>() {
//            @Override
//            public void changed(ObservableValue<? extends DocType> observable, DocType oldValue, DocType newValue) {
//                if (newValue != null) {
//                    mcnField.setDisable(!newValue.mcn);
//                }
//            }
//        });
//        statusColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("status"));
//        statusCombo.getItems().addAll(STATUS);
//    }
//
//    private void sortDTList() {
//        FXCollections.sort(dtCombo.getItems(), new Comparator<DocType>() {
//            @Override
//            public int compare(DocType o1, DocType o2) {
//                //Keep the "add new doc type" cell at the last index
//                if (o1.equals(addDocLabel) || o2.equals(addDocLabel)) {
//                    return 0;
//                }
//                return o1.name.compareTo(o2.name);
//            }
//        });
//    }
//
//
//    private ObservableList<DocType> getDocTypes() {
//        Connection connection = null;
//        ResultSet set = null;
//        PreparedStatement ps = null;
//        ObservableList<DocType> dts = FXCollections.observableArrayList();
//        try {
//            connection = ConnectionHandler.createDBConnection();
//            ps = connection.prepareStatement("SELECT id,name,mcn FROM `gilead_dt`");
//            set = ps.executeQuery();
//            while (set.next()) {
//                dts.add(new DocType(set.getInt("id"), set.getString("name"), Utils.intToBoolean(set.getInt("mcn"))));
//            }
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//            Main.LOGGER.log(Level.SEVERE, "There was an error getting the data of a column from the db!", e);
//
//        } finally {
//            dts.setAll(dts.stream().sorted(Comparator.comparing(DocType::getName)).collect(Collectors.toList()));
//            DbUtils.closeQuietly(set);
//            DbUtils.closeQuietly(ps);
//            DbUtils.closeQuietly(connection);
//        }
//        return dts;
//    }
//
//    private ObservableList<String> getFolders(Group group) {
//        Connection connection = null;
//        ResultSet set = null;
//        PreparedStatement ps = null;
//        ObservableList<String> folders = FXCollections.observableArrayList();
//        try {
//            connection = ConnectionHandler.createDBConnection();
//            ps = connection.prepareStatement("SELECT `folder name` as folder FROM `" + selJob.getName() + "` WHERE `" + groupCol + "`=?");
//            ps.setString(1, group.getName());
//            set = ps.executeQuery();
//            while (set.next()) {
//                folders.add(set.getString("folder"));
//            }
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//            Main.LOGGER.log(Level.SEVERE, "There was an error getting the data of a column from the db!", e);
//
//        } finally {
//            DbUtils.closeQuietly(set);
//            DbUtils.closeQuietly(ps);
//            DbUtils.closeQuietly(connection);
//        }
//        return folders;
//    }
//
//    public class GileadItem extends Item<GileadItem> {
//
//        private StringProperty name;
//        private StringProperty mcn;
//        private DocType docType;
//        private StringProperty status;
//        private StringProperty pc;
//
//        public GileadItem(String name) {
//            super.name.set(name);
//        }
//
//        public GileadItem() {
//        }
//
//        public GileadItem(int id, Collection collection, Group group, String name, String pc, String mcn, DocType dt, String status, int total, boolean completed, String type, List<String> condition, String comments, String started_On, String completed_On, String workstation, boolean overridden) {
//            super(id, collection, group, name, total, 0, type, completed, comments, started_On, completed_On, workstation, overridden);
//            super.type.setText(type);
//            this.pc = new SimpleStringProperty(pc);
//            this.mcn = new SimpleStringProperty(mcn);
//            this.docType = dt;
//            this.status = new SimpleStringProperty(status);
//            if (condition != null) {
//                super.conditions.addAll(condition);
//            }
//
//            this.details.setOnMousePressed(e -> {
//                setupDetailsPop(this);
//            });
//
//            this.delete.setOnMousePressed(e2 -> {
//                setupDelete(this, tree);
//            });
//
//
//            super.location = Paths.get(trackPath + "\\" + JsonHandler.getSelJob().getName() + "\\" + buildFolderStruct(super.id.get(), this) + "\\" + super.id.get());
//
//            if (super.id.get() > 0) {
//                super.location = Paths.get(trackPath + "\\" + JsonHandler.getSelJob().getName() + "\\" + buildFolderStruct(super.id.get(), this) + "\\" + super.id.get());
//            }
//            //This is required for newly inserted items -- The id is updated to the new row id once the new item is inserted into the db. For this project, the file name
//            // is the id. The id would be "0" if it's not updated after inserting.
//            super.idProperty().addListener((ob, ov, nv) -> {
//                if (!ov.equals(nv)) {
//                    super.location = Paths.get(trackPath + "\\" + JsonHandler.getSelJob().getName() + "\\" + buildFolderStruct(super.id.get(), this) + "\\" + super.id.get());
//                }
//            });
//        }
//
//        public String getMcn() {
//            return mcn.get();
//        }
//
//        public StringProperty mcnProperty() {
//            return mcn;
//        }
//
//        public void setMcn(String mcn) {
//            this.mcn.set(mcn);
//        }
//
//        public DocType getDocType() {
//            return docType;
//        }
//
//        public void setDocType(DocType docType) {
//            this.docType = docType;
//        }
//
//        public String getStatus() {
//            return status.get();
//        }
//
//        public StringProperty statusProperty() {
//            return status;
//        }
//
//        public void setStatus(String status) {
//            this.status.set(status);
//        }
//
//        public String getPc() {
//            return pc.get();
//        }
//
//        public StringProperty pcProperty() {
//            return pc;
//        }
//
//        public void setPc(String pc) {
//            this.pc.set(pc);
//        }
//    }
//
//
//    @FXML
//    private void iconify() {
////        Stage stage = FXUtils.getStageFromNode(mainPane);
////        stage.setIconified(true);
//    }
//
//    @FXML
//    private void close() throws IOException {
////        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Close the application?", ButtonType.YES, ButtonType.NO);
////        Optional<ButtonType> wait = alert.showAndWait();
////        if (wait.get().equals(ButtonType.YES)) {
////            ImgUtils.deleteTempDir();
////            Main.getStage().close();
////
////            String[] split = ManagementFactory.getRuntimeMXBean().getName().split("@");
////            Runtime.getRuntime().exec("taskkill /F /PID " + split[0]);
////        }
//    }
//
//    @FXML
//    private void restore() {
////        Stage stage = FXUtils.getStageFromNode(mainPane);
////        if (stage.isMaximized()) {
////            stage.setMaximized(false);
////        } else {
////            stage.setMaximized(true);
////        }
//    }
//
//
//    @Override
//    public void resetFields() {
//
////        firstField.setText("");
////        middleField.setText("");
////        lastField.setText("");
//
//        Platform.runLater(() -> {
//            //Only 'Doc Type' should be clear when inserting new item
//            dtCombo.getSelectionModel().clearSelection();
//        });
////        statusCombo.getSelectionModel().clearSelection();
////        ss.clear();
////        conditCombo.getCheckModel().clearChecks();
//    }
//
//
//    @Override
//    public void groupSelectTask(Group nv, JFXTreeTableView<? extends Item> tree) {
//        boolean sameVal = false;
//        if (nv != null && !nv.getName().isEmpty()) {
//            if (!nv.getName().equals(addGroupLabel)) {
//                if (ControllerHandler.selGroup != null && nv == ControllerHandler.selGroup) {
//                    sameVal = true;
//                }
//                if (!sameVal) {
//                    //Daily Log already initialized
//                    if (scanLogID != 0) {
//                        DailyLog.endDailyLog();
//                    }
//                    ControllerHandler.selGroup = nv;
//                    groupCountProp.set(0);
//                    tree.getRoot().getChildren().clear();
//                    selGroup.setText(nv.getName());
//                    tree.setPlaceholder(indicator);
//                    tree.getPlaceholder().autosize();
//                    CompletableFuture.runAsync(() -> {
//                        ObservableList<String> comboItems = getFolders(ControllerHandler.selGroup);
//                        FXCollections.sort(comboItems);
//                        folderCombo.getItems().setAll(comboItems);
//                    }).supplyAsync(() -> {
//                        ObservableList<?> entryItems = getGroupItems(ControllerHandler.selGroup);
//                        return entryItems;
//                    }).thenApplyAsync(entryItems -> {
////                        ObservableList group = ControllerHandler.selGroup.getItemList();
////                        group.setAll(entryItems);
////                        Platform.runLater(() -> {
////                            itemCombo.getSelectionModel().clearSelection();
////                        });
//                        List itemList = entryItems.stream().map(TreeItem::new).collect(Collectors.toList());
//                        return itemList;
//                    }).thenApplyAsync(itemList -> {
//                        tree.getRoot().getChildren().addAll(itemList);
//                        return itemList;
//                    }).thenRunAsync(() -> {
//                        Platform.runLater(() -> {
//                            groupCountProp.set(countGroupTotal());
//                            tree.refresh();
//                        });
//                        //Start a new log entry
//                        DailyLog.insertNewDailyLog(nv.getID());
//                    });
//                }
//                insertBtn.setDisable(false);
//            }
//        }
//    }
//
//    @Override
//    @FXML
//    public void insert() throws IOException {
//        final DocType docType = dtCombo.getSelectionModel().getSelectedItem();
//        final String folder = folderCombo.getSelectionModel().getSelectedItem();
//        final String name = "test";
//        final String status = statusCombo.getSelectionModel().getSelectedItem();
//        final List<String> condition = conditCombo.getCheckModel().getCheckedItems();
//        final String comments = commentsField.getText();
//        final String mcn = mcnField.getText();
//        final String pc = pcField.getText();
//        final Group group = groupTree.getSelectionModel().getSelectedItem();
//        boolean isPresent = tree.getRoot().getChildren().stream().anyMatch(e -> {
//            if (e != null) {
//                return e.getValue().name.get().equals(name) && e.getValue().getDocType().equals(docType) && e.getValue().getMcn().equals(mcn);
//            }
//            return false;
//        });
//        if (!isPresent) {
//            final GileadItem item = new GileadItem(0, group.getCollection(), group, name, pc, mcn, docType, status, 0, false, "Multi-Paged", condition, comments, LocalDateTime.now().toString(), "", COMP_NAME, false);
//            insertHelper(item);
//            TreeItem<GileadItem> dtNode = new TreeItem<GileadItem>(new GileadItem(docType.getName()));
//            TreeItem<GileadItem> pcNode = new TreeItem<GileadItem>(new GileadItem(pc));
//            pcNode.getChildren().add(dtNode);
//            TreeItem<GileadItem> child = new TreeItem<GileadItem>(item);
//
//            if (docType.mcn) {
//                TreeItem<GileadItem> mcnNode = new TreeItem<GileadItem>(new GileadItem(mcn));
//                pcNode.getChildren().add(mcnNode);
//                mcnNode.getChildren().add(child);
//            } else {
//                dtNode.getChildren().add(child);
//            }
//            tree.getRoot().getChildren().add(pcNode);
//            ObservableList<GileadItem> items = (ObservableList<GileadItem>) group.getItemList();
//            items.add(item);
//            fxTrayIcon.showInfoMessage("Item: `" + item.id.get() + "` Inserted");
//            createFoldersFromStruct(item);
//            resetFields();
//        } else {
//            errorLbl.setVisible(true);
//            fadeIn(errorLbl, Duration.seconds(.5));
//            fadeOut(errorLbl, Duration.seconds(3));
//        }
//    }
//
//    @FXML
//    private void editDocList() {
//
//    }
//
//
//    @Override
//    public int insertHelper(Item<? extends Item> item) {
//        GileadItem entryItem = (GileadItem) item;
//        Connection connection = null;
//        ResultSet set = null;
//        PreparedStatement ps = null;
//        int key = 0;
//        try {
//            connection = ConnectionHandler.createDBConnection();
//            ps = connection.prepareStatement("INSERT INTO `" + Main.JsonHandler.getSelJob().getName() + "` (name,started_on,employee_id,collection_id,group_id,comments,name,pc,mcn,doc_type,status,workstation) VALUES(?,?,(SELECT id FROM employees WHERE employees.name= '" + ConnectionHandler.user.getName() + "'),?,?,?,?,?,?,?,?,?)", PreparedStatement.RETURN_GENERATED_KEYS);
//            ps.setString(1, item.getName());
//            Date now = formatDateTime(item.getStarted_On());
//            ps.setTimestamp(2, new Timestamp(now.toInstant().toEpochMilli()));
//            ps.setInt(3, item.getCollection().getID());
//            ps.setInt(4, item.getGroup().getID());
//            ps.setString(5, item.getComments());
//            ps.setString(6, entryItem.getName());
//            ps.setString(7, entryItem.getPc());
//            ps.setString(8, entryItem.getMcn());
//            ps.setInt(9, entryItem.getDocType().getId());
//            ps.setString(10, entryItem.getStatus());
//            ps.setString(11, COMP_NAME);
//            ps.executeUpdate();
//            set = ps.getGeneratedKeys();
//
//        } catch (SQLException | ParseException e) {
//            e.printStackTrace();
//            Main.LOGGER.log(Level.SEVERE, "There was an error inserting a new JIB item!", e);
//
//        } finally {
//            try {
//                set = ps.getGeneratedKeys();
//                if (set.next()) {
//                    key = set.getInt(1);
//                }
//                item.id.set(key);
//            } catch (SQLException e) {
//                Main.LOGGER.log(Level.SEVERE, "There was an error trying to generating a key!", e);
//            }
//            DbUtils.closeQuietly(set);
//            DbUtils.closeQuietly(ps);
//            DbUtils.closeQuietly(connection);
//            updateItemProps(item);
//        }
//        return key;
//    }
//
//    public AnchorPane getRoot() {
//        return super.root;
//    }
//
//    public JFXTreeTableView<? extends Item> getTree() {
//        return super.tree;
//    }
//
//    public SearchableComboBox<Group> getGroupCombo() {
//        return super.groupCombo;
//    }
//
//    public SearchableComboBox<Collection> getColCombo() {
//        return super.colCombo;
//    }
//
//    public String getUid() {
//        return uid;
//    }
//
//    public void setUid(String uid) {
//        this.uid = uid;
//    }
//
//    public String getGroupCol() {
//        return groupCol;
//    }
//
//    public void setGroupCol(String groupCol) {
//        this.groupCol = groupCol;
//    }
//}
