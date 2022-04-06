package com.idi.userlogin.Controllers;

import com.idi.userlogin.Handlers.ConnectionHandler;
import com.idi.userlogin.Handlers.ControllerHandler;
import com.idi.userlogin.Handlers.JsonHandler;
import com.idi.userlogin.JavaBeans.Collection;
import com.idi.userlogin.JavaBeans.Group;
import com.idi.userlogin.JavaBeans.Item;
import com.idi.userlogin.utils.DBUtils;
import com.sun.javafx.scene.control.skin.TextFieldSkin;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ComboBoxTreeTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.controlsfx.control.SearchableComboBox;
import com.idi.userlogin.Main;
import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import org.apache.commons.dbutils.DbUtils;
import org.controlsfx.control.textfield.CustomTextField;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.*;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import static com.idi.userlogin.Handlers.JsonHandler.trackPath;
import static com.idi.userlogin.JavaBeans.User.COMP_NAME;
import static com.idi.userlogin.Main.*;

public class JIBController extends BaseEntryController<JIBController.JIBEntryItem> implements Initializable {
    private final ArrayList<String> STATUS = new ArrayList<>(Arrays.asList("LIVING", "DECEASED"));
    private final String addDocLabel = "Add New Doc Type";
    private final ObservableList<String> DOC_TYPES = FXCollections.observableArrayList();
    private final JIBEntryItem treeRoot = new JIBEntryItem();
    private final RecursiveTreeItem<JIBEntryItem> rootItem = new RecursiveTreeItem<>(treeRoot, RecursiveTreeObject::getChildren);

    @FXML
    protected JFXTreeTableView<JIBEntryItem> tree;
    @FXML
    protected JFXTreeTableColumn<JIBEntryItem, String> idColumn;
    @FXML
    protected JFXTreeTableColumn<JIBEntryItem, String> dtColumn;
    @FXML
    protected JFXTreeTableColumn<JIBEntryItem, String> statusColumn;
    @FXML
    protected JFXTreeTableColumn<JIBEntryItem, String> ssColumn;
    @FXML
    private CustomTextField firstField, lastField, middleField;
    @FXML
    protected SearchableComboBox<String> dtCombo; //Doc Type
    @FXML
    private SearchableComboBox<String> statusCombo;
    @FXML
    private PasswordField ss;
    @FXML
    Label editDocListBtn;
    //Social Security Helper
    private final SimpleStringProperty ssHelper = new SimpleStringProperty();
//    private final RecursiveTreeItem<ID> subRoot2 = new RecursiveTreeItem<>(new ID("Videos", "Folder", 200), RecursiveTreeObject::getChildren);
//    private final RecursiveTreeItem<ID> subRoot = new RecursiveTreeItem<>(new ID("Loose Images", "Folder", 200), RecursiveTreeObject::getChildren);
//    private final RecursiveTreeItem<ID> video = new RecursiveTreeItem<>(new ID("Video Test.mp4", "Video", 1), RecursiveTreeObject::getChildren);

    private void newChildNode(TreeItem root, TreeItem newNode) {
        if (!root.equals(newNode)) {
            root.getChildren().add(newNode);
        }
    }

    public static class SocialFormatter extends TextFieldSkin {

        TextField field;

        public SocialFormatter(TextField textField) {
            super(textField);
            field = textField;
        }

        public void moveCaret(int index) {
            Platform.runLater(() -> {
                field.positionCaret(index);
            });
        }


        @Override
        protected String maskText(String txt) {
            /* 012 34 5678 910
               111-11-1111-11 */
            final StringBuilder builder = new StringBuilder();

            try {
                final List<Integer> dash = new ArrayList<>(Arrays.asList(3, 5, 9)); //Dashes
                final TextField field = getSkinnable();
                final int n = field.getLength();
                for (int i = 0; i < n; i++) {
                    if (dash.contains(i)) {
                        builder.append("-");
                        builder.append("*");
                    } else {
                        builder.append("*");
                    }
                }

            } catch (IllegalArgumentException ignored) {
            }
            return builder.toString();
        }
    }

    @Override
    public ObservableList<? extends Item> getGroupItems(Group group) {
        Connection connection = null;
        ResultSet set = null;
        PreparedStatement ps = null;
        ObservableList<JIBEntryItem> group_items = FXCollections.observableArrayList();

        try {
            connection = ConnectionHandler.createDBConnection();
            ps = connection.prepareStatement("SELECT m.workstation,m.ss,m.doc_type,m.full_name,m.status, m.id,g.id as group_id, g.name as group_name, m.completed, e.name as employee, c.name as collection, m.total,t.name as type,m.conditions,m.started_On,m.completed_On,m.comments FROM `" + JsonHandler.getSelJob().getJob_id() + "" + DBUtils.DBTable.M.getTable() + "` m INNER JOIN employees e ON m.employee_id = e.id INNER JOIN `" + JsonHandler.getSelJob().getJob_id() + "_g` g ON m.group_id = g.id INNER JOIN item_types t ON m.type_id = t.id INNER JOIN `" + JsonHandler.getSelJob().getJob_id() + "" + DBUtils.DBTable.C.getTable() + " c ON m.collection_id = c.id  WHERE group_id=" + group.getID() + "");
            set = ps.executeQuery();
            while (set.next()) {
                final JIBEntryItem item = new JIBEntryItem(set.getInt("m.id"), group.getCollection(), group, set.getString("m.full_name"), set.getString("ss"), set.getString("doc_type"), set.getString("status"), set.getInt("m.total"), set.getInt("m.completed") == 1, "Multi-Paged", null, set.getString("m.comments"), set.getString("m.started_On"), set.getString("m.completed_On"), set.getString("m.workstation"));
                String condition = set.getString("m.conditions");
                if (condition != null && !condition.isEmpty()) {
                    String[] splitConditions = condition.split(", ");
                    item.getConditions().setAll(Arrays.asList(splitConditions));
                }
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


    @PostConstruct
    @Override
    public void afterInitialize() {
        super.afterInitialize();
    }

    @Override
    public void updateItem(String column, String newValue, Item item) {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = ConnectionHandler.createDBConnection();
            ps = connection.prepareStatement("Update `" + JsonHandler.getSelJob().getJob_id() + "" + DBUtils.DBTable.D.getTable() + "` SET `" + column + "`=? WHERE employee_id=" + ConnectionHandler.user.getId() + " AND id=?");
            ps.setString(1, newValue);
            ps.setInt(2, item.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            Main.LOGGER.log(Level.SEVERE, "There was an error updating a field!", e);
        } finally {
            DbUtils.closeQuietly(ps);
            DbUtils.closeQuietly(connection);
        }
    }

    @Override
    public void updateName(String oldValue, String newValue, Item item) {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = ConnectionHandler.createDBConnection();
            ps = connection.prepareStatement("Update `" + JsonHandler.getSelJob().getJob_id() + "" + DBUtils.DBTable.M.getTable() + "` SET `full_name`=? WHERE employee_id=" + ConnectionHandler.user.getId() + " AND full_name=?");
            ps.setString(1, newValue);
            ps.setString(2, oldValue);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            Main.LOGGER.log(Level.SEVERE, "There was an error updating a field!", e);
        } finally {
            DbUtils.closeQuietly(ps);
            DbUtils.closeQuietly(connection);
        }
        this.tree.getRoot().getChildren().stream().filter(e -> (e.getValue()).fullName.get().equals(oldValue)).forEach(e -> ((JIBEntryItem) e.getValue()).setFullName(newValue));
    }

    public class CustomJIBTableCell<S, T> extends TreeTableCell<JIBEntryItem, T> {
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null) {

                if (item instanceof Label) {
                    setText(((Label) item).getText());
                } else {
                    setText(item.toString());
                }
            }
            tree.refresh();
        }

        public CustomJIBTableCell() {
            super();
        }
    }

    public void docTypeHelper(String oldtype, String newType) {

        Connection connection = null;
        ResultSet set = null;
        PreparedStatement ps = null;
        try {
            connection = ConnectionHandler.createDBConnection();
            ps = connection.prepareStatement("UPDATE `jib_dt` SET name=? WHERE name=?");
            ps.setString(1, newType);
            ps.setString(2, oldtype);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            Main.LOGGER.log(Level.SEVERE, "There was an error editing a doc type!", e);

        } finally {
            DbUtils.closeQuietly(set);
            DbUtils.closeQuietly(ps);
            DbUtils.closeQuietly(connection);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle bundle) {
        //We add a listener to when one of the name fields are changed, we then clear the SS field. (New Patient)
        final ObservableList<TextField> nameFields = FXCollections.observableArrayList();
        nameFields.addAll(firstField, middleField, lastField);
        nameFields.forEach(e -> e.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.equals(oldValue)) {
                    ss.clear();
                }
            }
        }));

        jibController = this;
        tree.setRoot(rootItem);
        afterInitialize();
        DOC_TYPES.addAll(getDocTypes());
        FXCollections.sort(DOC_TYPES);
        dtCombo.getItems().addAll(DOC_TYPES);
        dtCombo.getItems().set(dtCombo.getItems().size() - 1, addDocLabel);
        dtCombo.setCellFactory(e -> {
            ListCell<String> cell = new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null && !item.isEmpty()) {
                        setText(item);
                        setContentDisplay(ContentDisplay.TEXT_ONLY);
                        if (!item.equals(addDocLabel)) {
                            StackPane stackPane = new StackPane();
                            Label text = new Label(item);
                            HBox left = new HBox(text);
                            left.setAlignment(Pos.CENTER_LEFT);
                            Label edit = new Label("Edit");
                            edit.getStyleClass().add("labels");
                            edit.setStyle("-fx-text-fill: #07255E;");
                            ChangeListener listViewShowListener = new ChangeListener<Boolean>() {
                                @Override
                                public void changed(ObservableValue observable, Boolean oldValue, Boolean newValue) {
                                    if (!newValue) {
                                        dtCombo.show();
                                    }
                                }
                            };

                            edit.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
                                dtCombo.showingProperty().addListener(listViewShowListener);
                                TextField textField = new TextField(item);
                                StackPane pane2 = new StackPane();
                                HBox root = new HBox();
                                root.setSpacing(8);
                                HBox left2 = new HBox();
                                HBox right2 = new HBox();
                                root.getChildren().addAll(left2, right2);
                                left2.setAlignment(Pos.CENTER_LEFT);
                                right2.setAlignment(Pos.CENTER_RIGHT);
                                HBox.setHgrow(left2, Priority.ALWAYS);
                                HBox.setHgrow(right2, Priority.ALWAYS);
                                Label cancelEdit = new Label("X");
                                cancelEdit.setMaxWidth(40);
                                cancelEdit.getStyleClass().add("labels");
                                cancelEdit.setStyle("-fx-text-fill: #07255E;");
                                left2.getChildren().add(textField);
                                right2.getChildren().add(cancelEdit);
                                pane2.getChildren().addAll(root);

                                cancelEdit.addEventFilter(MouseEvent.MOUSE_PRESSED, e2 -> {
                                    setGraphic(stackPane);
                                    dtCombo.showingProperty().removeListener(listViewShowListener);
                                });

                                textField.addEventFilter(KeyEvent.KEY_PRESSED, e2 -> {
                                    if (e2.getCode().equals(KeyCode.ENTER)) {
                                        System.out.println(textField.getText());
                                        docTypeHelper(text.getText(), textField.getText());
                                        fxTrayIcon.showInfoMessage("Doc Type: `" + text.getText() + "` Renamed To `" + textField.getText() + "`");
                                        text.setText(textField.getText());
                                        setText(textField.getText());
                                        dtCombo.getItems().set(getIndex(), textField.getText());
                                        setGraphic(stackPane);
                                        dtCombo.showingProperty().removeListener(listViewShowListener);
                                    }
                                });

                                setGraphic(pane2);
                            });
                            HBox right = new HBox(edit);
                            right.setAlignment(Pos.CENTER_RIGHT);
                            stackPane.getChildren().addAll(left, right);
                            setGraphic(stackPane);
                            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                        }
                    } else {
                        setText("");
                    }
                }
            };

            //ADD NEW DOC TYPE was selected
            cell.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, evt -> {

                if (!cell.getItem().isEmpty() && !cell.isEmpty()) {

                    if (cell.getItem().equals(addDocLabel)) {
                        TextInputDialog dialog = new TextInputDialog();
                        dialog.setContentText("Enter Type");

                        dialog.showAndWait().ifPresent(text -> {
                            String textUpper = text.toUpperCase();
                            boolean noMatch = dtCombo.getItems().stream().noneMatch(e3 -> e3.toUpperCase().equals(textUpper));
                            if (noMatch) {
                                newDocType(textUpper);
                                Platform.runLater(() -> {
                                    DOC_TYPES.add(textUpper);
                                    dtCombo.getItems().set(dtCombo.getItems().size() - 2, textUpper);
                                    sortDTList();
                                    final int index = dtCombo.getItems().indexOf(textUpper);
                                    dtCombo.getSelectionModel().select(index);
                                });

                                fxTrayIcon.showInfoMessage("DocType '" + text + "' Has Been Created");
                            } else {
                                fxTrayIcon.showErrorMessage("DocType '" + text + "' Exists Already!");
                            }
                        });
                    }
                }
            });
            return cell;
        });

//        nameColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("fullName"));
        idColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("id"));
        dtColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("docType"));

        dtColumn.setCellFactory(e -> new ComboBoxTreeTableCell<JIBEntryItem, String>(FXCollections.observableArrayList(DOC_TYPES)) {

            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
            }

            @Override
            public void startEdit() {
                super.startEdit();
            }


            @Override
            public void cancelEdit() {
                super.cancelEdit();
            }

            @Override
            public void commitEdit(String newValue) {
                JIBEntryItem item = getTreeTableRow().getTreeItem().getValue();
                item.setDocType(newValue);
                ControllerHandler.updateItem(item, "UPDATE `" + JsonHandler.getSelJob().getJob_id() + "" + DBUtils.DBTable.D.getTable() + "` SET doc_type='" + item.getDocType() + "' WHERE id=" + item.id.get() + "");
                super.commitEdit(newValue);
            }
        });
        statusColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("status"));
        ssColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("ss"));
        ssColumn.setCellFactory(e -> {
            TreeTableCell<JIBEntryItem, String> cell = new TreeTableCell<JIBEntryItem, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null && !item.isEmpty()) {
                        final StringBuilder builder = formatHiddenSocial(item);
                        setText(builder.toString());
                    } else {
                        setGraphic(null);
                        setText("");
                    }
                }
            };
            return cell;
        });
        countColumn.setCellFactory(e -> new CustomJIBTableCell());
        countColumn.setCellValueFactory(
                (JFXTreeTableColumn.CellDataFeatures<JIBEntryItem, Number> param) ->
                        new SimpleIntegerProperty(param.getValue().getValue().total.get()));
        final JIBEntryItem treeRoot = new JIBEntryItem();
        statusCombo.getItems().addAll(STATUS);

        ss.setSkin(new SocialFormatter(ss));
        ss.textProperty().bindBidirectional(ssHelper);
        ssHelper.addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                Tooltip.install(ss, new Tooltip(formatVisibleSocial(ssHelper.get()).toString()));
            }
        });

        final ObservableList<StringProperty> textProperties = FXCollections.observableArrayList(ss.textProperty(), lastField.textProperty(), firstField.textProperty());
        textProperties.forEach(e -> {
            e.addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    legalDocumentHelper();
                }
            });
        });


        statusCombo.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                legalDocumentHelper();
            }
        });

        dtCombo.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                legalDocumentHelper();
            }
        });
    }

    private void sortDTList() {
        FXCollections.sort(dtCombo.getItems(), new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                //Keep the "add new doc type" cell at the last index
                if (o1.equals(addDocLabel) || o2.equals(addDocLabel)) {
                    return 0;
                }
                return o1.compareTo(o2);
            }
        });
    }

    public void newDocType(String type) {

        Connection connection = null;
        ResultSet set = null;
        PreparedStatement ps = null;
        try {
            connection = ConnectionHandler.createDBConnection();
            ps = connection.prepareStatement("INSERT INTO `jib_dt` (name) VALUES(?)", PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, type);
            ps.executeUpdate();
            set = ps.getGeneratedKeys();

        } catch (SQLException e) {
            e.printStackTrace();
            Main.LOGGER.log(Level.SEVERE, "There was an error inserting a new group!", e);

        } finally {

            DbUtils.closeQuietly(set);
            DbUtils.closeQuietly(ps);
            DbUtils.closeQuietly(connection);
        }
    }

    private List<String> getDocTypes() {
        Connection connection = null;
        ResultSet set = null;
        PreparedStatement ps = null;
        List<String> dts = new ArrayList<>();
        try {
            connection = ConnectionHandler.createDBConnection();
            ps = connection.prepareStatement("SELECT name FROM `jib_dt`");
            set = ps.executeQuery();
            while (set.next()) {
                dts.add(set.getString("name"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Main.LOGGER.log(Level.SEVERE, "There was an error getting the data of a column from the db!", e);

        } finally {
            DbUtils.closeQuietly(set);
            DbUtils.closeQuietly(ps);
            DbUtils.closeQuietly(connection);
        }
        return dts;
    }

    //Adds dashes in between the numerical numbers
    private StringBuilder formatVisibleSocial(String item) {
        StringBuilder builder = new StringBuilder();
        if (item.length() == 9) {
            builder.append(item, 0, 3);
            builder.append("-");
            builder.append(item, 3, 5);
            builder.append("-");
            builder.append(item.substring(5));

        } else if (item.length() >= 10) {
            builder.append(item, 0, 3);
            builder.append("-");
            builder.append(item, 3, 5);
            builder.append("-");
            builder.append(item, 5, 9);
            builder.append("-");
            builder.append(item.substring(9));
        }
        return builder;
    }

    private StringBuilder formatHiddenSocial(String item) {
        StringBuilder builder = new StringBuilder();

        builder.append("***-**-");
        if (item.length() == 11) {
            builder.append(item, 7, 11);
        } else if (item.length() > 11) {
            builder.append(item, 7, 11);
            builder.append("-");
            builder.append(item.substring(11));
        }
        return builder;
    }


    public class JIBEntryItem extends Item<JIBEntryItem> {

        private StringProperty fullName;
        private StringProperty ss;
        private StringProperty docType;
        private StringProperty status;

        public JIBEntryItem() {
        }

        public JIBEntryItem(int id, com.idi.userlogin.JavaBeans.Collection collection, Group group, String fullName, String ss, String docType, String status, int total, boolean completed, String type, List<String> condition, String comments, String started_On, String completed_On, String workstation) {
            super(id, collection, group, fullName, total, 0, "Multi-Paged", completed, comments, started_On, completed_On, workstation);
            super.type.setText("Multi-Paged");
            this.fullName = new SimpleStringProperty(fullName);
            this.ss = new SimpleStringProperty(ss);
            this.docType = new SimpleStringProperty(docType);
            this.status = new SimpleStringProperty(status);
            if (condition != null) {
                super.conditions.addAll(condition);
            }

            this.details.setOnMousePressed(e -> {
                setupDetailsPop(this);
            });

            this.delete.setOnMousePressed(e2 -> {
                setupDelete(this, tree);
            });


            super.location = Paths.get(trackPath + "\\" + JsonHandler.getSelJob().getJob_id() + "\\" + buildFolderStruct(super.id.get(), this) + "\\" + super.id.get());


            if (super.id.get() > 0) {
                super.location = Paths.get(trackPath + "\\" + JsonHandler.getSelJob().getJob_id() + "\\" + buildFolderStruct(super.id.get(), this) + "\\" + super.id.get());
            }
            //This is required for newly inserted items -- The id is updated to the new row id once the new item is inserted into the db. For this project, the file name
            // is the id. The id would be "0" if it's not updated after inserting.
            super.idProperty().addListener((ob, ov, nv) -> {
                if (!ov.equals(nv)) {
                    super.location = Paths.get(trackPath + "\\" + JsonHandler.getSelJob().getJob_id() + "\\" + buildFolderStruct(super.id.get(), this) + "\\" + super.id.get());
                }
            });
        }

        public String getFullName() {
            return fullName.get();
        }

        public StringProperty fullNameProperty() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName.set(fullName);
        }

        public String getSs() {
            return ss.get();
        }

        public StringProperty ssProperty() {
            return ss;
        }

        public void setSs(String ss) {
            this.ss.set(ss);
        }

        public String getDocType() {
            return docType.get();
        }

        public StringProperty docTypeProperty() {
            return docType;
        }

        public void setDocType(String docType) {
            this.docType.set(docType);
        }

        public String getStatus() {
            return status.get();
        }

        public StringProperty statusProperty() {
            return status;
        }

        public void setStatus(String status) {
            this.status.set(status);
        }
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

    public boolean legalDocument() {
        return statusCombo.getSelectionModel().getSelectedItem() != null && groupTree.getSelectionModel().getSelectedItem() != null && firstField.getText() != null && !firstField.getText().isEmpty() && lastField.getText() != null && !lastField.getText().isEmpty() && ss.getText() != null && !ss.getText().isEmpty() && ss.getText().length() >= 9 && dtCombo.getSelectionModel().getSelectedItem() != null;
    }

    public void legalDocumentHelper() {
        boolean isLegal = legalDocument();
        if (isLegal) {
            insertBtn.setDisable(false);
        } else {
            insertBtn.setDisable(true);
        }
    }

    @Override
    public void resetFields() {

//        firstField.setText("");
//        middleField.setText("");
//        lastField.setText("");

        Platform.runLater(() -> {
            //Only 'Doc Type' should be clear when inserting new item
            dtCombo.getSelectionModel().clearSelection();
        });
//        statusCombo.getSelectionModel().clearSelection();
//        ss.clear();
//        conditCombo.getCheckModel().clearChecks();
    }

    @Override
    @FXML
    public void insert() throws IOException {
        final String fullName = (firstField.getText() + " " + lastField.getText() + " " + middleField.getText()).trim();
        final String docType = dtCombo.getSelectionModel().getSelectedItem();
        final String status = statusCombo.getSelectionModel().getSelectedItem();
        final List<String> condition = conditCombo.getCheckModel().getCheckedItems();
        final String comments = commentsField.getText();
        final String social = formatVisibleSocial(ssHelper.get()).toString();
        final Group group = groupTree.getSelectionModel().getSelectedItem().getValue();
        boolean isPresent = tree.getRoot().getChildren().stream().anyMatch(e -> {
            if (e != null) {
                return e.getValue().getFullName().equals(fullName) && e.getValue().getDocType().equals(docType) && e.getValue().getSs().equals(social) && social.length() >= 9;
            }
            return false;
        });
        if (!isPresent) {
            final JIBEntryItem item = new JIBEntryItem(0, group.getCollection(), group, fullName, social, docType, status, 0, false, "Multi-Paged", condition, comments, LocalDateTime.now().toString(), "", COMP_NAME);
            insertHelper(item);
            tree.getRoot().getChildren().add(new TreeItem<JIBEntryItem>(item));
            ObservableList<JIBEntryItem> items = (ObservableList<JIBEntryItem>) group.getItemList();
            items.add(item);
            fxTrayIcon.showInfoMessage("Item: `" + item.id.get() + "` Inserted");
            createFoldersFromStruct(item);
            resetFields();
        } else {
            errorLbl.setVisible(true);
            fadeIn(errorLbl, Duration.seconds(.5));
            fadeOut(errorLbl, Duration.seconds(3));
        }
    }

    @FXML
    private void editDocList() {

    }

    @Override
    public int insertHelper(Item<? extends Item> item) {
        JIBEntryItem entryItem = (JIBEntryItem) item;
        Connection connection = null;
        ResultSet set = null;
        PreparedStatement ps = null;
        int key = 0;
        try {
            connection = ConnectionHandler.createDBConnection();
            ps = connection.prepareStatement("INSERT INTO `" + JsonHandler.getSelJob().getJob_id() + "" + DBUtils.DBTable.D.getTable() + "` (name,started_on,employee_id,collection_id,group_id,comments,full_name,ss,doc_type,status,workstation) VALUES(?,?,(SELECT id FROM employees WHERE employees.name= '" + ConnectionHandler.user.getName() + "'),?,?,?,?,?,?,?,?)", PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, item.getName());
            Date now = formatDateTime(item.getStarted_On());
            ps.setTimestamp(2, new Timestamp(now.toInstant().toEpochMilli()));
            ps.setInt(3, item.getCollection().getID());
            ps.setInt(4, item.getGroup().getID());
            ps.setString(5, item.getComments());
            ps.setString(6, entryItem.getFullName());
            ps.setString(7, entryItem.getSs());
            ps.setString(8, entryItem.getDocType());
            ps.setString(9, entryItem.getStatus());
            ps.setString(10, COMP_NAME);
            ps.executeUpdate();
            set = ps.getGeneratedKeys();

        } catch (SQLException | ParseException e) {
            e.printStackTrace();
            Main.LOGGER.log(Level.SEVERE, "There was an error inserting a new JIB item!", e);

        } finally {
            try {
                set = ps.getGeneratedKeys();
                if (set.next()) {
                    key = set.getInt(1);
                }
                item.id.set(key);
            } catch (SQLException e) {
                Main.LOGGER.log(Level.SEVERE, "There was an error trying to generating a key!", e);
            }
            DbUtils.closeQuietly(set);
            DbUtils.closeQuietly(ps);
            DbUtils.closeQuietly(connection);
            updateItemProps(item);
        }
        return key;
    }

    public AnchorPane getRoot() {
        return super.root;
    }

    public JFXTreeTableView<? extends Item> getTree() {
        return super.tree;
    }


    public SearchableComboBox<Collection> getColCombo() {
        return super.colCombo;
    }


}
