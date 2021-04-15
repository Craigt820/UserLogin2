package com.idi.userlogin.Controllers;

import com.idi.userlogin.JavaBeans.Collection;
import com.idi.userlogin.JavaBeans.Group;
import com.idi.userlogin.JavaBeans.Item;
import com.idi.userlogin.Main;
import com.idi.userlogin.utils.ImgFactory;
import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.JFXTreeTableView;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeTableCell;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;
import org.apache.commons.dbutils.DbUtils;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.SearchableComboBox;
import org.controlsfx.control.textfield.CustomTextField;
import com.idi.userlogin.utils.AutoCompleteTextField;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.*;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static com.idi.userlogin.JsonHandler.trackPath;
import static com.idi.userlogin.Main.*;
import static com.idi.userlogin.utils.ImgFactory.IMGS.CHECKMARK;
import static com.idi.userlogin.utils.ImgFactory.IMGS.EXMARK;
import static com.idi.userlogin.utils.Utils.*;
import static org.apache.commons.lang3.text.WordUtils.capitalizeFully;

public abstract class BaseEntryController<T extends Item> extends ControllerHandler implements Initializable {

    public ProgressIndicator indicator = new ProgressIndicator();
    public List<com.idi.userlogin.JavaBeans.Collection> collectionList;
    public Group selGroupItem = null;
    public com.idi.userlogin.JavaBeans.Collection selColItem = null;
    final MenuItem compAll = new MenuItem("Complete All");

    @FXML
    protected AnchorPane root;
    @FXML
    protected JFXTreeTableView<? extends Item> tree;
    @FXML
    protected JFXTreeTableColumn<T, Boolean> existColumn;
    @FXML
    protected JFXTreeTableColumn<T, Label> delColumn;
    @FXML
    protected JFXTreeTableColumn<T, Label> nameColumn;
    @FXML
    protected JFXTreeTableColumn<T, Label> typeColumn;
    @FXML
    protected JFXTreeTableColumn<T, Number> countColumn;
    @FXML
    protected JFXTreeTableColumn<T, Integer> nonFeederCol;
    @FXML
    protected JFXTreeTableColumn<T, CheckBox> compColumn;
    @FXML
    protected JFXTreeTableColumn<T, Label> detailsColumn;
    @FXML
    protected Button insertBtn, compBtn;
    @FXML
    protected Label selCol;
    @FXML
    protected Label selGroup;
    @FXML
    protected Label errorLbl;
    @FXML
    protected VBox checkListRoot;
    @FXML
    protected CheckComboBox<String> conditCombo;
    @FXML
    protected TabPane tabPane;
    @FXML
    protected TextArea commentsField;
    @FXML
    protected AutoCompleteTextField nameField;
    @FXML
    protected ComboBox<Label> typeCombo;
    @FXML
    protected SearchableComboBox<Group> groupCombo;
    @FXML
    protected SearchableComboBox<com.idi.userlogin.JavaBeans.Collection> colCombo;
    @FXML
    protected Button settings;
    @FXML
    protected Label totalCount;

    protected URL location;
    protected ResourceBundle resources;


    public class CustomTreeTableCell<S, T> extends TreeTableCell<Item, T> {
        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            if (getTreeTableRow().getTreeItem() != null) {
                if (empty || getTreeTableRow().getTreeItem().getValue().getType().getText().equals("Root")) {
                    setText(null);
                    setGraphic(null);
                } else {

                    if (item instanceof Label) {
                        setText(((Label) item).getText());
                    } else {
                        setText(item.toString());
                    }
                }
            }
            tree.refresh();
        }

        public CustomTreeTableCell() {
            super();
        }
    }


    public class TypeCell extends ListCell<Label> {
        @Override
        protected void updateItem(Label item, boolean isEmpty) {

            super.updateItem(item, isEmpty);
            if (item != null) {

                final ImageView view = new ImageView();
                switch (getText()) {
                    case "Folder":
                    case "Root":
                        view.setImage(Item.folderIcon);
                        break;
                    case "Multi-Paged":
                        view.setImage(Item.fileIcon);
                        break;
                }
                view.setFitWidth(20);
                view.setFitHeight(20);
                setGraphic(view);
                setGraphicTextGap(16);
            }
            setText(item == null ? "" : item.getText());
        }
    }

    @FXML
    public void complete() {
        completeGroupTask(tree);
    }

    private void completeGroupTask(JFXTreeTableView<? extends Item> tree) {
        Optional<? extends TreeItem<? extends Item>> items = tree.getRoot().getChildren().stream().filter(e -> !e.getValue().overridden.get()).filter(e -> !e.getValue().exists.get()).findAny();
        if (!items.isPresent()) {
            fxTrayIcon.showInfoMessage("Group '" + selGroupItem.getName() + "' has been completed!");
            tree.getRoot().getChildren().forEach(e2 -> e2.getValue().getCompleted().setSelected(true));
            groupCombo.getSelectionModel().getSelectedItem().setCompleted_On(LocalDateTime.now().toString());
            groupCombo.getSelectionModel().getSelectedItem().setComplete(true);
            Task task = new Task() {
                @Override
                protected Object call() throws Exception {
                    updateAll(tree);
                    return null;
                }
            };
            new Thread(task).start();
            task.setOnSucceeded(e -> {
                updateGroup(tree, true);
                tree.getRoot().getChildren().clear();
                selCol.setText("");
                selGroup.setText("");
                groupCountProp.setValue(0);
                groupCombo.getSelectionModel().clearSelection();
                selGroupItem = null;
            });
        } else {
            fxTrayIcon.showErrorMessage("Some Items Don't Exist!");
        }
    }

    public class EntryItem extends Item<T> {

        EntryItem item;

        public EntryItem() {
            super();
            item = this;
        }

        public EntryItem(int id, com.idi.userlogin.JavaBeans.Collection collection, Group group, String name, int total, int non_feeder, String type, boolean completed, String comments, String started_On, String completed_On, Boolean overridden) {
            super(id, collection, group, name, total, non_feeder, type, completed, comments, started_On, completed_On, overridden);

            super.details.setOnMousePressed(e -> {
                setupDetailsPop(this);
            });

            super.delete.setOnMousePressed(e2 -> {
                setupDelete(this, tree);
            });

            super.location = Paths.get(trackPath + "\\" + jsonHandler.getSelJobID() + "\\" + buildFolderStruct(this.id.get(), this) + "\\" + this.name.getText().trim());

        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Item item = (Item) o;
            return item.getId() == getId();
        }

        @Override
        public int hashCode() {
            return Objects.hash(getId(), getName());
        }

    }


    public void setupDelete(Item item, JFXTreeTableView<? extends Item> tree) {
        //Confirmation - Yes/No
        final Button yes = new Button("Yes");
        final Button no = new Button("No");
        yes.setPadding(new Insets(8, 8, 8, 0));
        no.setPadding(new Insets(8, 0, 8, 8));
        yes.setPrefWidth(40);
        no.setPrefWidth(40);
        yes.setStyle("-fx-font-size:14;-fx-text-fill:black; -fx-background-color: #fefefe66; -fx-border-color: #afafaf; -fx-border-width: 0 .3 0 0");
        no.setStyle("-fx-font-size:14;-fx-text-fill:black;-fx-background-color: #fefefe66;");
        item.delete.setGraphic(new HBox(yes, no));
        item.delete.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        no.setOnMousePressed(e3 -> {
            item.delete.setContentDisplay(ContentDisplay.TEXT_ONLY);
        });

        yes.setOnMousePressed(e3 -> {
            if (item != null) {
                Item.removeItem(item);
                fxTrayIcon.showInfoMessage("Item: '" + item.getName().getText().trim() + "' Has Been Removed");

                //Remove from group item list
                final Optional<?> groupItem = selGroupItem.getItemList().stream().filter(e -> e.getId() == item.getId()).findAny();
                if (groupItem.isPresent()) {
                    selGroupItem.getItemList().remove(groupItem.get());
                }

                //Remove from checklist
                final Optional<?> chkitem = checkListController.getClAllTable().getItems().stream().filter(e -> e.getId() == item.getId()).findAny();

                boolean remove = tree.getRoot().getChildren().removeIf(e -> e.getValue().id.get() == item.getId());

                updateGroup(tree, false);
                if (chkitem.isPresent()) {
                    checkListController.getClAllTable().getItems().remove(chkitem.get());
                }
            }
        });
    }

    private void groupSelectTask(Group nv, JFXTreeTableView<? extends Item> tree) {
        boolean sameVal = false;
        if (nv != null && !nv.getName().isEmpty()) {
            if (nv.getName() != "Add New Group") {
                if (selGroupItem != null && nv == selGroupItem) {
                    sameVal = true;
                }
                if (!sameVal) {
                    selGroupItem = nv;
                    groupCountProp.set(0);
                    tree.getRoot().getChildren().clear();
                    selGroup.setText(nv.getName());
                    tree.setPlaceholder(indicator);
                    tree.getPlaceholder().autosize();
                    final Task task = new Task() {
                        @Override
                        protected Object call() {
                            final ObservableList<?> entryItems = getGroupItems(selGroupItem);
                            final ObservableList group = selGroupItem.getItemList();
                            if (entryItems != null) {
                                group.setAll(entryItems);

                                final List itemList = entryItems.stream().map(TreeItem::new).collect(Collectors.toList());
                                tree.getRoot().getChildren().addAll(itemList);

                                itemList.forEach(e -> {
                                    TreeItem<Item> item = (TreeItem<Item>) e;
                                    Task task1 = new Task() {
                                        @Override
                                        protected Object call() throws Exception {
                                            Map<Integer, Boolean> pages = countHandler(item.getValue().getLocation(), "Multi-Paged");
                                            item.getValue().setExists((Boolean) pages.values().toArray()[0]);
                                            item.getValue().setTotal((Integer) pages.keySet().toArray()[0]);
                                            return null;
                                        }
                                    };
                                    new Thread(task1).start();

                                    checkListController.getClAllTable().getItems().stream().filter(e2 -> {
                                        return item.getValue().getId() == e2.getId();
                                    }).findAny().ifPresent(e3 -> {
                                        item.getValue().totalProperty().bindBidirectional(e3.totalProperty());
                                        item.getValue().completed.selectedProperty().bindBidirectional(e3.completed.selectedProperty());
                                        item.getValue().name.textProperty().bindBidirectional(e3.name.textProperty());
                                        item.getValue().comments.bindBidirectional(e3.comments);
                                        item.getValue().completed_On.bindBidirectional(e3.completed_On);
                                        item.getValue().started_On.bindBidirectional(e3.started_On);
                                        item.getValue().conditions.bindBidirectional(e3.conditions);
                                        item.getValue().overridden.bindBidirectional(e3.overridden);
                                        e3.setLocation(item.getValue().getLocation());
                                        e3.setExists(item.getValue().isExists());
                                    });
                                });
                            }
                            return null;
                        }
                    };
                    new Thread(task).start();

                    task.setOnSucceeded(e -> {
                        updateTotal();
                    });
                }
            }
        }
    }

    @FXML
    public abstract void insert() throws IOException;


    public void setupDetailsPop(Item item) {
        if (item.type.equals("Root")) {
            item.details.setDisable(true);
            item.details.setVisible(false);
        }

        final FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DetailsPop.fxml"));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        final DetailsPopController detailPopCont = loader.getController();
        final PopOver popOver = new PopOver(root);
        popOver.setDetached(true);
        popOver.setTitle(item.name.getText());

        popOver.setOnHiding(e2 -> {
            if (detailPopCont.commentsField.getText() != null) {
                item.setComments(detailPopCont.commentsField.getText());
            }

            if (detailPopCont.conditCombo.getCheckModel().getCheckedItems() != null) {
                if (!item.conditions.equals(detailPopCont.conditCombo.getCheckModel().getCheckedItems())) {
                    item.conditions.setAll(detailPopCont.conditCombo.getCheckModel().getCheckedItems());
                }
            }

            if (detailPopCont.scannerCombo.getCheckModel().getCheckedItems() != null) {
                item.scanners = new ArrayList<String>(detailPopCont.scannerCombo.getCheckModel().getCheckedItems());
            }

            updateItemProps(item);
            getOpaqueOverlay().setVisible(popOver.isShowing());
            opaquePOS();
        });

        popOver.showingProperty().addListener(e2 -> {
            getOpaqueOverlay().setVisible(popOver.isShowing());
            opaquePOS();
        });
        final Bounds boundsInScreen = tree.localToScene(tree.getBoundsInLocal());
        popOver.show(tree.getScene().getWindow(), boundsInScreen.getMinX(), boundsInScreen.getMinY());

        //Test for Thumbnail Previews - TODO: Come back to later!
        //TODO: I may incorporate "Thumbnail Previews" Later
        //                if (super.previews != null && !super.getPreviews().isEmpty()) {
//                    for (Image prev : super.previews) {
//                        detailPopCont.prevRoot.getChildren().add(new ImageView(prev));
//                    }
//                }


        if (item.getLocation() != null) {
            detailPopCont.location.setText(item.getLocation().toString());
        }

        if (item.completed_On.get() != null && !item.completed_On.get().isEmpty()) {
            detailPopCont.compOn.setText(LocalDateTime.parse(item.completed_On.get().replace(" ", "T")).format(DATE_FORMAT));
        }

        if (item.started_On.get() != null && !item.started_On.get().isEmpty()) {
            detailPopCont.startedOn.setText(LocalDateTime.parse(item.started_On.get().replace(" ", "T")).format(DATE_FORMAT));
        }

        if (item.comments != null && !item.comments.get().isEmpty()) {
            detailPopCont.commentsField.setText(item.comments.get());
        }
        if (item.conditions != null) {
            for (Object s : item.conditions.get()) {
                detailPopCont.conditCombo.getItemBooleanProperty(s.toString()).set(true);
            }
        }

        if (item.scanners != null && !devices.isEmpty()) {
            detailPopCont.scannerCombo.getItems().addAll(devices);
            for (Object s : item.scanners) {
                detailPopCont.scannerCombo.getItemBooleanProperty(s.toString()).set(true);
            }
        }

        detailPopCont.overridden.setText(capitalizeFully(String.valueOf(item.isOverridden())));
        //
//                ImageView testImg = new ImageView(getClass().getResource("/images/testImg.png").toExternalForm());
//                testImg.setFitWidth(140);
//                testImg.setFitHeight(140);
//                detailPopCont.prevRoot.getChildren().add(testImg);
    }

    BaseEntryController() {
        super();
        collectionList = new ArrayList<>();
        indicator.setMaxSize(80, 80);
    }

    private void updateNonFeeder(T item) {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = ConnectionHandler.createDBConnection();
            ps = connection.prepareStatement("Update `" + jsonHandler.getSelJobID() + "` SET non_feeder=? WHERE id=?");
            ps.setInt(1, item.getNonFeeder());
            ps.setInt(2, item.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            Main.LOGGER.log(Level.SEVERE, "There was an error updating a non-feeder field!", e);

        } finally {
            DbUtils.closeQuietly(ps);
            DbUtils.closeQuietly(connection);
        }
    }

    public void initCheckListScene() {
        checkListRoot.getChildren().add(checkListScene);
        final List<List<Group>> colGroups = collectionList.stream().map(com.idi.userlogin.JavaBeans.Collection::getGroupList).collect(Collectors.toList());
        final ObservableList items = FXCollections.observableArrayList();
        for (List<Group> groupList : colGroups) {
            for (Group group : groupList) {
                ObservableList<? extends Item> groupItems = (ObservableList<? extends Item>) group.getItemList();
                groupItems.forEach(e -> {
                    final EntryItem clItem = new EntryItem(e.getId(), e.getCollection(), e.getGroup(), e.getName().getText(), e.getTotal(), e.getNonFeeder(), e.getType().getText(), e.getCompleted().isSelected(), e.getComments(), e.getStarted_On(), e.getCompleted_On(), e.isOverridden());
                    items.add(clItem);
                });
            }
        }

        checkListController.getClAllTable().getItems().addAll(items);
    }


    @PostConstruct
    public void afterInitialize() {
        totalCount.textProperty().bind(groupCountProp.asString());
        groupCombo.setEditable(true);

        tree.getColumns().forEach(e -> e.setContextMenu(new ContextMenu()));
        setupCompTask(tree);
        tree.setShowRoot(false);
        tree.getRoot().getChildren().addListener(new ListChangeListener<TreeItem<? extends Item>>() {
            @Override
            public void onChanged(Change<? extends TreeItem<? extends Item>> c) {
                compBtn.setDisable(c.getList().size() <= 0);
            }
        });
        nameColumn.setStyle("-fx-padding: 0 0 0 16;");
        final ContextMenu treeMenu = new ContextMenu();
        final MenuItem updateAll = new MenuItem("Update All");

        updateAll.setOnAction(e -> {
            if (!tree.getRoot().getChildren().isEmpty()) {
                updateAll(tree);
                updateGroup(tree, groupCombo.getSelectionModel().getSelectedItem().isComplete());
            }
        });

        treeMenu.getItems().add(updateAll);
        tree.setContextMenu(treeMenu);

        groupCombo.getSelectionModel().selectedItemProperty().addListener((ob, ov, nv) -> {
            groupSelectTask(nv, tree);
        });

        if (colCombo != null) {
            colCombo.getSelectionModel().selectFirst();
            colCombo.getSelectionModel().selectedItemProperty().addListener((ob, ov, nv) -> {
                if (nv != null) {
                    selCol.setText(nv.getName());
                }
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

            colCombo.setButtonCell(new ListCell<com.idi.userlogin.JavaBeans.Collection>() {
                @Override
                protected void updateItem(com.idi.userlogin.JavaBeans.Collection item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null && !item.getName().isEmpty()) {
                        setText(item.getName());
                    }


                }
            });
            colCombo.getSelectionModel().selectFirst();
            colCombo.getSelectionModel().selectedItemProperty().addListener((ob, ov, nv) -> {
                if (nv != null) {
                    selCol.setText(nv.getName());
                }
            });
        }

        if (nameField != null) {
            legalTextTest(legalText(""), nameField);
            nameField.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    legalTextTest(legalText(newValue), nameField);
                }
            });

            nameColumn.setCellFactory(e -> new TextFieldTreeTableCell<T, Label>(new StringConverter<Label>() {
                @Override
                public String toString(Label object) {
                    return object.getText();
                }

                @Override
                public Label fromString(String string) {
                    return new Label(string);
                }
            }) {
                public CustomTextField ctf = new CustomTextField();

                @Override
                public void updateItem(Label item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null && !item.getText().isEmpty()) {
                        setText(item.getText());
                    }
                }

                @Override
                public void startEdit() {
                    ctf.setText(getText());
                    setGraphic(ctf);
                    super.startEdit();
                }

                @Override
                public void commitEdit(Label newValue) {
                    if (legalText(newValue.getText())) {
                        super.commitEdit(newValue);
                        setGraphic(null);
                    } else {
                        ctf.setRight(ImgFactory.createView(EXMARK));
                    }
                }
            });
            nameColumn.setEditable(false);
            nameColumn.setPrefWidth(190);
            nameColumn.setEditable(true);
            nameColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("name"));
            legalTextTest(legalText(""), nameField);
            nameField.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    legalTextTest(legalText(newValue), nameField);
                }
            });
            nameColumn.setCellFactory(e -> new TextFieldTreeTableCell<T, Label>(new StringConverter<Label>() {
                @Override
                public String toString(Label object) {
                    return object.getText();
                }

                @Override
                public Label fromString(String string) {
                    return new Label(string);
                }
            }) {
                public CustomTextField ctf = new CustomTextField();

                @Override
                public void updateItem(Label item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null && !item.getText().isEmpty()) {
                        setText(item.getText());
                    }
                }

                @Override
                public void startEdit() {
                    ctf.setText(getText());
                    setGraphic(ctf);
                    super.startEdit();
                }

                @Override
                public void commitEdit(Label newValue) {
                    if (legalText(newValue.getText())) {
                        super.commitEdit(newValue);
                        Item item = (Item) getTreeTableRow().getTreeItem().getValue();
                        item.getName().setText(getText());
                        setGraphic(null);
                    } else {
                        ctf.setRight(ImgFactory.createView(EXMARK));
                    }
                }
            });
            nameColumn.setPrefWidth(190);
            nameColumn.setEditable(true);
            nameColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("name"));
        }

        if (typeCombo != null) {
            typeCombo.getItems().addAll(Arrays.asList(new Label("Folder", adjustFitSize(Item.folderIcon)), new Label("Multi-Paged", adjustFitSize(Item.fileIcon))));
            typeCombo.getSelectionModel().selectFirst();
            typeCombo.setButtonCell(new TypeCell());
            typeColumn.setPrefWidth(150);
            typeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("type"));
        }
        settings.setOnMousePressed(e -> {
            mainMenuPop.setArrowLocation(PopOver.ArrowLocation.TOP_LEFT);
            mainMenuPop.setAutoHide(true);
            mainMenuPop.show(settings, e.getScreenX(), e.getScreenY() + 20);
        });

        delColumn.setPrefWidth(100);
        delColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("delete"));
        countColumn.setPrefWidth(190);
        countColumn.setCellValueFactory(new TreeItemPropertyValueFactory("total"));
        countColumn.setCellFactory(e -> new CustomTreeTableCell());
        compColumn.setCellFactory(e -> new CheckBoxTreeTableCell<T, CheckBox>() {
            @Override
            public void updateItem(CheckBox item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    item.setSelected(this.getTreeTableRow().getTreeItem().getValue().isCompleted_prop());
                    setGraphic(item);
                } else {
                    setGraphic(null);
                }
            }
        });
        compColumn.setPrefWidth(190);
        compColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("completed"));

        detailsColumn.setPrefWidth(100);
        detailsColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("details"));
        conditCombo.getItems().addAll(CONDITION_LIST);
        conditCombo.setManaged(true);

        groupCombo.setConverter(new StringConverter<Group>() {
            @Override
            public String toString(Group group) {
                if (group == null) {
                    return null;
                } else {
                    return group.getName();
                }
            }

            @Override
            public Group fromString(String group) {
                return null;
            }
        });
        if (nonFeederCol != null) {
            nonFeederCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("nonFeeder"));
            nonFeederCol.setCellFactory(e -> new TextFieldTreeTableCell<T, Integer>(new IntegerStringConverter() {
                @Override
                public String toString(Integer object) {
                    return object.toString();
                }

                @Override
                public Integer fromString(String string) {
                    return Integer.valueOf(string);
                }
            }) {
                public final CustomTextField ctf = new CustomTextField();

                @Override
                public void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        setText(item.toString());
                    }
                }

                @Override
                public void startEdit() {
                    ctf.setText(getText());
                    ctf.setMaxWidth(90);
                    ctf.setMinWidth(90);
                    ctf.setAlignment(Pos.CENTER);
                    setGraphic(ctf);
                    super.startEdit();
                }

                @Override
                public void commitEdit(Integer newValue) {
                    super.commitEdit(newValue);
                    getTreeTableRow().getTreeItem().getValue().nonFeederProperty().set(newValue);
                    setGraphic(null);
                }
            });

            nonFeederCol.setOnEditCommit(e -> {
                e.getRowValue().getValue().setNonFeeder(e.getNewValue());
                updateNonFeeder(e.getRowValue().getValue());
            });
            nonFeederCol.setCellValueFactory(new TreeItemPropertyValueFactory<>("nonFeeder"));
            nonFeederCol.setCellFactory(e -> new TextFieldTreeTableCell<T, Integer>(new IntegerStringConverter() {
                @Override
                public String toString(Integer object) {
                    return object.toString();
                }

                @Override
                public Integer fromString(String string) {
                    return Integer.valueOf(string);
                }
            }) {
                public final CustomTextField ctf = new CustomTextField();

                @Override
                public void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        setText(item.toString());
                    }
                }

                @Override
                public void startEdit() {
                    ctf.setText(getText());
                    ctf.setMaxWidth(90);
                    ctf.setMinWidth(90);
                    ctf.setAlignment(Pos.CENTER);
                    setGraphic(ctf);
                    super.startEdit();
                }

                @Override
                public void commitEdit(Integer newValue) {
                    super.commitEdit(newValue);
                    getTreeTableRow().getTreeItem().getValue().nonFeederProperty().set(newValue);
                    setGraphic(null);
                }
            });

            nonFeederCol.setOnEditCommit(e -> {
                e.getRowValue().getValue().setNonFeeder(e.getNewValue());
                updateNonFeeder(e.getRowValue().getValue());
            });
        }
        groupCombo.setEditable(true);

        settings.setOnMousePressed(e -> {
            mainMenuPop.setArrowLocation(PopOver.ArrowLocation.TOP_LEFT);
            mainMenuPop.setAutoHide(true);
            mainMenuPop.show(settings, e.getScreenX(), e.getScreenY() + 20);
        });

        delColumn.setPrefWidth(100);
        delColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("delete"));
        countColumn.setPrefWidth(190);
        countColumn.setCellFactory(e -> new CustomTreeTableCell());
        compColumn.setPrefWidth(190);
        compColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("completed"));
        detailsColumn.setPrefWidth(100);
        detailsColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("details"));
        conditCombo.setManaged(true);
        updateTotal();

        groupCombo.setCellFactory(e -> {
            ListCell<Group> cell = new ListCell<Group>() {
                @Override
                protected void updateItem(Group item, boolean empty) {
                    super.updateItem(item, empty);

                    if (item != null && !item.getName().isEmpty()) {
                        setText(item.getName());
                        if (item.completeProperty() != null) {
                            if (item.isComplete()) {
                                setGraphic(ImgFactory.createView(CHECKMARK));
                                String completed_On = item.getCompleted_On();
                                if (completed_On != null && !completed_On.isEmpty()) {
                                    setTooltip(new Tooltip("Completed: " + LocalDateTime.parse(completed_On).format(DATE_FORMAT)));
                                }
                            } else {
                                setGraphic(null);
                                setTooltip(null);
                            }

                            item.completeProperty().addListener(new ChangeListener<Boolean>() {
                                @Override
                                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                                    if (item.completeProperty() != null && item.isComplete()) {
                                        setGraphic(ImgFactory.createView(CHECKMARK));
                                        String completed_On = item.getCompleted_On();
                                        if (completed_On != null && !completed_On.isEmpty()) {
                                            setTooltip(new Tooltip("Completed: " + LocalDateTime.parse(completed_On).format(DATE_FORMAT)));
                                        }
                                    } else {
                                        setGraphic(null);
                                        setTooltip(null);
                                    }
                                }
                            });
                        }
                    }
                }
            };

            cell.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, evt -> {
                if (cell.getItem().isEmpty() && !cell.isEmpty()) {
                    if (cell.getItem().getName().equals("Add New Group")) {
                        TextInputDialog dialog = new TextInputDialog();
                        dialog.setContentText("Enter item");
                        dialog.showAndWait().ifPresent(text -> {
                            final int index = groupCombo.getItems().size() - 1;
                            final Group group = new Group(0, getColCombo().getValue(), text, false, LocalDateTime.now().toString(), "");
                            boolean noMatch = groupCombo.getItems().stream().map(Group::getName).noneMatch(e3 -> e3.toLowerCase().equals(group.getName().toLowerCase()));
                            if (noMatch) {
                                newGroupHelper(group);
                                Platform.runLater(() -> {
                                    groupCombo.getItems().add(index, group);
                                    FXCollections.sort(groupCombo.getItems(), new Comparator<Group>() {
                                        @Override
                                        public int compare(Group o1, Group o2) {
                                            return o1.nameProperty().get().compareTo(o2.getName());
                                        }
                                    });
                                    groupCombo.getSelectionModel().select(group);
                                });

                                fxTrayIcon.showInfoMessage("Group '" + group.getName() + "' Has Been Created");
                            } else {
                                fxTrayIcon.showErrorMessage("Group '" + group.getName() + "' Exists Already!");
                            }
                        });
                    }
                }
            });
            return cell;
        });

        groupCombo.setButtonCell(new ListCell<Group>() {
            @Override
            protected void updateItem(Group item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !item.getName().isEmpty()) {
                    setText(item.getName());
                    if (item.completeProperty() != null && item.isComplete()) {
                        setGraphic(ImgFactory.createView(CHECKMARK));
                        String completed_On = item.getCompleted_On();
                        if (completed_On != null && !completed_On.isEmpty()) {
                            setTooltip(new Tooltip("Completed: " + LocalDateTime.parse(completed_On).format(DATE_FORMAT)));
                        }
                    }
                }
            }
        });

        groupCombo.setConverter(new StringConverter<Group>() {
            @Override
            public String toString(Group group) {
                if (group == null) {
                    return null;
                } else {
                    return group.getName();
                }
            }

            @Override
            public Group fromString(String group) {
                return null;
            }
        });


        if (colCombo != null) {
            colCombo.getSelectionModel().selectedItemProperty().addListener((ob, ov, nv) -> {

                boolean sameVal = false;
                if (nv != null && !nv.getName().isEmpty()) {

                    if (selColItem != null && nv == selColItem) {
                        sameVal = true;
                    }
                    if (!sameVal) {
                        selColItem = nv;
                        selCol.setText(nv.getName());
                        Task task = new Task() {
                            @Override
                            protected Object call() throws Exception {
                                Platform.runLater(() -> {
                                    groupCombo.getItems().addAll(selColItem.getGroupList());
                                });
                                return null;
                            }
                        };
                        new Thread(task).start();
                    }
                }
            });
        }

        groupCombo.getItems().add(new Group("Add New Group"));
        existColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("exists"));
        existColumn.setCellFactory(e -> {
                    TreeTableCell<T, Boolean> cell = new TreeTableCell<T, Boolean>() {
                        @Override
                        protected void updateItem(Boolean item, boolean empty) {
                            super.updateItem(item, empty);
                            if (item != null) {
                                Label label = new Label();
                                ImageView view;
                                Item itemObj = this.getTreeTableRow().getTreeItem().getValue();
                                if (itemObj.exists.get() || itemObj.overridden.get()) {
                                    view = ImgFactory.createView(ImgFactory.IMGS.CHECKMARK);
                                    Tooltip.install(label, new Tooltip("Exists"));
                                } else {
                                    view = ImgFactory.createView(EXMARK);
                                    Tooltip.install(label, new Tooltip("Doesn't Exist!"));
                                    itemObj.getCompleted().setSelected(false);
                                }
                                label.setGraphic(view);
                                label.setPadding(new Insets(0, 0, 0, 8));
                                setGraphic(label);
                                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                            }
                        }
                    };
                    return cell;
                }
        );
    }

    public void newGroupHelper(Group group) {

        Connection connection = null;
        ResultSet set = null;
        PreparedStatement ps = null;
        int key = 0;
        try {
            connection = ConnectionHandler.createDBConnection();
            ps = connection.prepareStatement("INSERT INTO `sc_groups` (name,collection_id,job_id,started_on,employees) VALUES(?,?,(SELECT id FROM projects WHERE job_id='" + jsonHandler.getSelJobID() + "'),?,?)", PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, group.getName());
            ps.setInt(2, group.getCollection().getID());
            Date now = formatDateTime(group.getStarted_On());
            ps.setTimestamp(3, new Timestamp(now.toInstant().toEpochMilli()));
            ps.setString(4, jsonHandler.getName());
            ps.executeUpdate();
            set = ps.getGeneratedKeys();

        } catch (SQLException | ParseException e) {
            e.printStackTrace();
            Main.LOGGER.log(Level.SEVERE, "There was an error inserting a new group!", e);

        } finally {
            try {
                set = ps.getGeneratedKeys();
                if (set.next()) {
                    key = set.getInt(1);
                }
                group.IDProperty().set(key);
            } catch (SQLException e) {
                Main.LOGGER.log(Level.SEVERE, "There was an error generating a new key!", e);
            }
            DbUtils.closeQuietly(set);
            DbUtils.closeQuietly(ps);
            DbUtils.closeQuietly(connection);
        }
    }

    public void setupCompTask(TreeTableView<? extends Item> tree) {
        compColumn.getContextMenu().getItems().add(compAll);
        compAll.setOnAction(e -> {
            CompletableFuture.runAsync(() -> {
                tree.getRoot().getChildren().forEach(e2 -> e2.getValue().getCompleted().setSelected(true));
            }).whenComplete((i, e2) -> {
                updateAll((JFXTreeTableView<? extends Item>) tree);
                updateTotal();
            });
        });
    }

    public List<com.idi.userlogin.JavaBeans.Collection> getCollectionList() {
        return collectionList;
    }

    public void setCollectionList(List<com.idi.userlogin.JavaBeans.Collection> collectionList) {
        this.collectionList = collectionList;
    }

    @Override
    public abstract void initialize(URL location, ResourceBundle bundle);

    @Override
    public synchronized void updateTotal() {
        final AtomicInteger ai = new AtomicInteger(0);
        final AtomicInteger ai2 = new AtomicInteger(0);

        if (tree.getRoot() != null && !tree.getRoot().getChildren().isEmpty()) {
            for (TreeItem<? extends Item> child : tree.getRoot().getChildren()) {
                ai.getAndAdd(child.getValue().total.get());
                final String compOn = child.getValue().completed_On.get();
                if (compOn != null && !compOn.isEmpty()) {
                    LocalDateTime now = LocalDateTime.now();
                    int nDay = now.getDayOfMonth();
                    int nMonth = now.getMonthValue();
                    int nYear = now.getYear();
                    final LocalDateTime itemTime = LocalDateTime.parse(child.getValue().completed_On.get().replace(" ", "T"));
                    int iDay = itemTime.getDayOfMonth();
                    int iMonth = itemTime.getMonthValue();
                    int iYear = itemTime.getYear();
                    if ((nDay + nMonth + nYear) == iDay + iMonth + iYear) {
                        ai2.getAndAdd(child.getValue().getTotal());
                    }
                }

                for (TreeItem<? extends Item> childChild : child.getChildren()) {
                    ai.getAndAdd(childChild.getValue().getTotal());
                }
            }
        }

        groupCountProp.setValue(ai.get());
        totalCountProp.setValue(ai2.get());
    }

    @Override
    public ObservableList<? extends Item> getGroupItems(Group group) {
        return null;
    }

    @Override
    public synchronized void updateGroup(JFXTreeTableView<? extends Item> tree, boolean completed) {

        updateTotal();
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = ConnectionHandler.createDBConnection();
            ps = connection.prepareStatement("Update `sc_groups` SET total=?, scanned=?, completed_On=? WHERE id=?");
            ps.setInt(1, groupCountProp.get());
            ps.setInt(2, booleanToInt(completed));
            if (completed) {
                final Date now = formatDateTime(LocalDateTime.now().toString());
                ps.setTimestamp(3, new Timestamp(now.toInstant().toEpochMilli()));
            } else {
                ps.setTimestamp(3, null);
            }

            ps.setInt(4, groupCombo.getSelectionModel().getSelectedItem().getID());
            ps.executeUpdate();

        } catch (SQLException | ParseException e) {
            e.printStackTrace();
            Main.LOGGER.log(Level.SEVERE, "There was an error updating a group!", e);
        } finally {
            DbUtils.closeQuietly(ps);
            DbUtils.closeQuietly(connection);
        }

    }

    @Override
    public void legalTextTest(boolean isLegal, CustomTextField node) {

    }

    public SearchableComboBox<Collection> getColCombo() {
        return colCombo;
    }

}
