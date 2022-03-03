package com.idi.userlogin.Controllers;

import com.idi.userlogin.Handlers.ConnectionHandler;
import com.idi.userlogin.Handlers.ControllerHandler;
import com.idi.userlogin.JavaBeans.Collection;
import com.idi.userlogin.JavaBeans.Group;
import com.idi.userlogin.JavaBeans.Item;
import com.idi.userlogin.Main;
import com.idi.userlogin.utils.DBUtils;
import com.idi.userlogin.utils.DailyLog;
import com.idi.userlogin.utils.ImgFactory;
import com.idi.userlogin.utils.Utils;
import com.jfoenix.controls.JFXHamburger;
import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.transitions.hamburger.HamburgerSlideCloseTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
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
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.SearchableComboBox;
import org.controlsfx.control.textfield.CustomTextField;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.*;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.idi.userlogin.Handlers.JsonHandler.trackPath;
import static com.idi.userlogin.JavaBeans.User.COMP_NAME;
import static com.idi.userlogin.Main.*;
import static com.idi.userlogin.utils.DailyLog.scanLogID;
import static com.idi.userlogin.utils.ImgFactory.IMGS.CHECKMARK;
import static com.idi.userlogin.utils.ImgFactory.IMGS.EXMARK;
import static com.idi.userlogin.utils.Utils.*;
import static org.apache.commons.lang3.text.WordUtils.capitalizeFully;

public abstract class BaseEntryController<T extends Item> extends ControllerHandler implements Initializable {

    public ProgressIndicator indicator = new ProgressIndicator();
    public List<com.idi.userlogin.JavaBeans.Collection> collectionList;
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
    protected JFXTreeTableColumn<T, String> nameColumn;
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
    protected JFXHamburger burger;
    @FXML
    protected Button insertBtn, compBtn;
    @FXML
    protected Label selCol;
    @FXML
    protected Label selGroup;
    @FXML
    protected Label errorLbl;
    @FXML
    protected CheckComboBox<String> conditCombo;
    @FXML
    protected TabPane tabPane;
    @FXML
    protected TextArea commentsField;
    @FXML
    protected CustomTextField nameField;
    @FXML
    protected ComboBox<Label> typeCombo;
    @FXML
    protected MenuButton groupMB;
    @FXML
    protected TreeView<Group> groupTree;
    @FXML
    protected SearchableComboBox<com.idi.userlogin.JavaBeans.Collection> colCombo;
    @FXML
    protected Button settings;
    @FXML
    protected Label totalCount;
    protected HamburgerSlideCloseTransition hbabt;
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
            if (item != null && !item.getText().isEmpty()) {
                final ImageView view = new ImageView();
                switch (item.getText()) {
                    case "Folder":
                    case "Root":
                        view.setImage(Item.folderIcon);
                        break;
                    case "Multi-Paged":
                        view.setImage(Item.fileIcon);
                        break;
                }
                view.setFitWidth(22);
                view.setFitHeight(22);
                setGraphic(view);
                setGraphicTextGap(8);
                setText(item.getText());

            } else {
                setText(null);
                setGraphic(null);
            }
        }
    }

    @FXML
    public void complete() {
        completeGroupTask(tree);
    }

    private void completeGroupTask(JFXTreeTableView<? extends Item> tree) {
        Optional<? extends TreeItem<? extends Item>> items = tree.getRoot().getChildren().stream().filter(e -> !e.getValue().exists.get()).findAny();
        if (!items.isPresent()) {
            tree.getRoot().getChildren().forEach(e2 -> e2.getValue().getCompleted().setSelected(true));
            groupTree.getSelectionModel().getSelectedItem().getValue().setCompleted_On(LocalDateTime.now().toString());
            groupTree.getSelectionModel().getSelectedItem().getValue().setComplete(true);
            CompletableFuture.runAsync(() -> {
                updateAllHelper(true);
            }).thenRunAsync(() -> DailyLog.updateLog(ControllerHandler.selGroup)).thenRunAsync(() -> DailyLog.endDailyLog()).thenRunAsync(() -> {
                fxTrayIcon.showInfoMessage("Group '" + ControllerHandler.selGroup.getName() + "' has been completed!");
                Platform.runLater(() -> {
                    tree.getRoot().getChildren().clear();
                    selCol.setText("");
                    selGroup.setText("");
                    groupCountProp.setValue(0);
                    groupTree.getSelectionModel().clearSelection();
                    ControllerHandler.selGroup = null;
                });
            });

        } else {
            tree.getSortOrder().add((TreeTableColumn) existColumn);
            tree.sort();
            fxTrayIcon.showErrorMessage("Some Items Don't Exist!");
        }
    }

    public static int countGroupTotal() {
        return ControllerHandler.selGroup.getItemList().stream().mapToInt(Item::getTotal).sum();
    }

    public class EntryItem extends Item<T> {

        public EntryItem() {
            super();
        }

        //Insert Constructor
        public EntryItem(com.idi.userlogin.JavaBeans.Collection collection, Group group, String name, String type, String comments) {
            super(0, collection, group, name, 0, 0, type, false, comments, LocalDateTime.now().toString(), null, COMP_NAME);

            super.details.setOnMousePressed(e -> {
                setupDetailsPop(this);
            });

            super.delete.setOnMousePressed(e2 -> {
                setupDelete(this, tree);
            });

        }

        public EntryItem(int id, com.idi.userlogin.JavaBeans.Collection collection, Group group, String name, int total, int non_feeder, String type, boolean completed, String comments, String started_On, String completed_On, String workstation) {
            super(id, collection, group, name, total, non_feeder, type, completed, comments, started_On, completed_On, workstation);

            super.details.setOnMousePressed(e -> {
                setupDetailsPop(this);
            });

            super.delete.setOnMousePressed(e2 -> {
                setupDelete(this, tree);
            });

            super.location = Paths.get(trackPath + "\\" + jsonHandler.getSelJobID() + "\\" + buildFolderStruct(this.id.get(), this) + "\\" + this.name.get().trim());

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
        no.setPadding(new Insets(8, 0, 8, 2));
        yes.setPrefWidth(40);
        no.setPrefWidth(40);
        yes.setStyle("-fx-font-size:.9em;-fx-text-fill:black; -fx-background-color: #fefefe66; -fx-border-color: #afafaf; -fx-border-width: 0 .3 0 0;");
        no.setStyle("-fx-font-size:.9em;-fx-text-fill:black;-fx-background-color: #fefefe66;");
        item.delete.setGraphic(new HBox(yes, no));
        item.delete.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        no.setOnMousePressed(e3 -> {
            item.delete.setContentDisplay(ContentDisplay.TEXT_ONLY);
        });

        yes.setOnMousePressed(e3 -> {
            if (item != null) {
                if (selJob.isUserEntry()) {
                    Item.removeItem(item);
                    fxTrayIcon.showInfoMessage("Item: '" + item.getName().trim() + "' Has Been Removed");
                    //Remove from group item list
                    final Optional<?> groupItem = ControllerHandler.selGroup.getItemList().stream().filter(e -> e.getId() == item.getId()).findAny();
                    groupItem.ifPresent(o -> ControllerHandler.selGroup.getItemList().remove(o));
                } else {
                    Platform.runLater(() -> {
                        ControllerHandler.maniViewController.getItemCombo().getItems().add((BaseEntryController.EntryItem) item);
                        ControllerHandler.maniViewController.sortItems(ControllerHandler.maniViewController.getItemCombo().getItems());
                    });
                    ControllerHandler.maniViewController.deleteItem(item);
                }


                //Remove from Tree View
                boolean remove = tree.getRoot().getChildren().removeIf(e -> e.getValue().id.get() == item.getId());
                if (remove) {
                    CompletableFuture.supplyAsync(() -> {
                        int gTotal = ControllerHandler.getGroupTotal(ControllerHandler.selGroup);
                        ControllerHandler.selGroup.setTotal(gTotal);
                        return ControllerHandler.selGroup;
                    }).thenAccept(group -> {
                        Platform.runLater(() -> {
                            groupCountProp.set(group.getTotal());
                        });
                        updateGroup(group, false);
                        DailyLog.updateLog(group);
                    }).thenRunAsync(() -> {
                        tree.refresh();
                    });
                }
            }
        });
    }

    @Override
    public void groupSelectTask(Group nv, JFXTreeTableView<? extends Item> tree) {
        boolean sameVal = false;
        if (nv.getSubGroup() == null && nv != null && !nv.getName().isEmpty()) {
            if (!nv.getName().equals(addGroupLabel)) {
                if (ControllerHandler.selGroup != null && nv == ControllerHandler.selGroup) {
                    sameVal = true;
                }
                if (!sameVal) {
                    //Daily Log already initialized
                    if (scanLogID != 0) {
                        DailyLog.endDailyLog();
                    }

                    //Nested Sub Group
                    Group g_Parent = nv.getG_Parent();

                    //If nested, select the main parent group
                    if (g_Parent != null) {
                        ControllerHandler.selGroup = g_Parent;
                    } else {
                        ControllerHandler.selGroup = nv;
                    }

                    groupCountProp.set(0);
                    tree.getRoot().getChildren().clear();
                    selGroup.setText(nv.getName());
                    tree.setPlaceholder(indicator);
                    tree.getPlaceholder().autosize();
                    CompletableFuture.supplyAsync(() -> {
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
                            nv.setTotal(ControllerHandler.getGroupTotal(nv));
                            groupCountProp.set(countGroupTotal());
                            tree.refresh();
                        });

                        //Start a new log entry
                        DailyLog.insertNewDailyLog(nv.getID());
                    }).whenComplete((ex, ig) -> {
                        Platform.runLater(() -> {
                            tree.setPlaceholder(null);
                        });
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
        popOver.setTitle(item.name.get());

        popOver.setOnHiding(e2 -> {
            if (detailPopCont.commentsField.getText() != null) {
                item.setComments(detailPopCont.commentsField.getText());
            }

            if (detailPopCont.conditCombo.getCheckModel().getCheckedItems() != null) {
                if (!item.conditions.equals(detailPopCont.conditCombo.getCheckModel().getCheckedItems())) {
                    item.conditions.get().setAll(detailPopCont.conditCombo.getCheckModel().getCheckedItems());
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

        if (item.getWorkstation() != null) {
            detailPopCont.workstation.setText(item.getWorkstation());
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

        detailPopCont.itemInfo.setRoot(new TreeItem<String>());
        ObservableList<TreeItem<String>> itemInfo = Utils.getItemInfo("group_id=" + item.getGroup().getID());
        detailPopCont.itemInfo.getRoot().getChildren().setAll(itemInfo);

        if (!DEVICE_LIST.isEmpty()) {
            detailPopCont.scannerCombo.getItems().addAll(DEVICE_LIST);
            if (item.scanners != null) {
                for (Object s : item.scanners) {
                    detailPopCont.scannerCombo.getItemBooleanProperty(s.toString()).set(true);
                }
            }
        }

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
            ps = connection.prepareStatement("Update `" + DBUtils.DBTable.D.getTable() + "` SET non_feeder=? WHERE id=?");
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

    public TreeItem<Group> getSubGroup(TreeItem<Group> group, StringBuilder sbH) {
        final TreeItem<Group> parent = group.getParent();
        if (parent != null && !groupTree.getRoot().equals(parent)) {
            System.out.println(parent.getValue().getName().trim());
            sbH.append(parent.getValue().getName().trim() + "\\");
            return getSubGroup(group.getParent(), sbH);
        }
        return parent;
    }

    @PostConstruct
    public void afterInitialize() {
        //For Main Menu
        // Create the tree
        TreeItem<Group> rootItem = new TreeItem<Group>(new Group(null, ""));
        rootItem.setExpanded(true);
        groupTree.setShowRoot(false);
        groupTree.setRoot(rootItem);
        hbabt = new HamburgerSlideCloseTransition(burger);
        totalCount.textProperty().bind(groupCountProp.asString());
        groupTree.setEditable(true);
        tree.getColumns().forEach(e -> e.setContextMenu(new ContextMenu()));
        setupCompTask();
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
                updateAllHelper(false);
            }
        });

        treeMenu.getItems().add(updateAll);
        tree.setContextMenu(treeMenu);


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
                        } else {
                            setText(null);
                            setGraphic(null);
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
                    } else {
                        setText(null);
                        setGraphic(null);
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
        nameColumn.setPrefWidth(190);
        nameColumn.setEditable(true);
        this.nameColumn.setCellFactory(e -> new TextFieldTreeTableCell<T, String>(new StringConverter<String>() {
            public String toString(String object) {
                return object;
            }

            public String fromString(String string) {
                return string;
            }
        }) {
            public CustomTextField ctf = new CustomTextField();

            String oldValue;

            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !item.isEmpty()) {
                    setText(item);
                } else {
                    setText("");
                }
            }

            public void startEdit() {
                this.oldValue = getText();
                this.ctf.setText(this.oldValue);
                setGraphic(this.ctf);
                super.startEdit();
            }

            public void commitEdit(String newValue) {
                if (Utils.legalText(newValue)) {
                    super.commitEdit(newValue);
                    Item item = getTreeTableRow().getTreeItem().getValue();
                    item.setName(getText());
                    setGraphic(null);
                    updateName(this.oldValue, newValue, item);
                } else {
                    this.ctf.setRight(ImgFactory.createView(ImgFactory.IMGS.EXMARK));
                }
            }
        });


        nameColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("name"));

        if (nameField != null) {
            legalTextTest(legalText(""), nameField);
            nameField.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    legalTextTest(legalText(newValue), nameField);
                }
            });

            legalTextTest(legalText(""), nameField);
            nameField.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    legalTextTest(legalText(newValue), nameField);
                }
            });

        }

        if (typeCombo != null) {
            typeCombo.getItems().addAll(Arrays.asList(new Label("Folder", adjustFitSize(Item.folderIcon)), new Label("Multi-Paged", adjustFitSize(Item.fileIcon))));
            typeCombo.getSelectionModel().selectFirst();
            typeCombo.setButtonCell(new TypeCell());
            typeCombo.setCellFactory(e -> {
                return new TypeCell();
            });
            typeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("type"));
        }

        delColumn.setPrefWidth(100);
        delColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("delete"));
        countColumn.setPrefWidth(190);
        countColumn.setCellValueFactory(new TreeItemPropertyValueFactory("total"));
        compColumn.setCellFactory(e -> {
            return new CheckBoxTreeTableCell<T, CheckBox>() {
                public void updateItem(CheckBox item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        setGraphic(item);
                        item.setOnAction(e -> {
                            CompletableFuture.runAsync(() -> {
                                if (item.isSelected()) {
                                    getTreeTableRow().getTreeItem().getValue().completed_On.set(LocalDateTime.now().toString());
                                } else {
                                    getTreeTableRow().getTreeItem().getValue().completed_On.set(null);
                                }
                                BaseEntryController.updateSelected(getTreeTableRow().getItem());
                            }).thenApply(e2 -> {
                                int gTotal = ControllerHandler.getGroupTotal(ControllerHandler.selGroup);
                                ControllerHandler.selGroup.setTotal(gTotal);
                                return ControllerHandler.selGroup;
                            }).thenAccept(group -> {
                                Platform.runLater(() -> {
                                    groupCountProp.set(group.getTotal());
                                });
                                updateGroup(group, false);
                            }).thenRunAsync(() -> {
                                tree.refresh();
                            });
                        });
                    } else {
                        setGraphic(null);
                    }
                }
            };
        });
        compColumn.setPrefWidth(190);
        compColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("completed"));

        detailsColumn.setPrefWidth(100);
        detailsColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("details"));
        conditCombo.getItems().addAll(CONDITION_LIST);
        conditCombo.setManaged(true);
//
//        groupTree.setConverter(new StringConverter<Group>() {
//            @Override
//            public String toString(Group group) {
//                if (group == null) {
//                    return null;
//                } else {
//                    return group.getName();
//                }
//            }
//
//            @Override
//            public Group fromString(String group) {
//                return null;
//            }
//        });

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
        groupTree.setEditable(true);

        settings.setOnMousePressed(e -> {
            mainMenuPop.setArrowLocation(PopOver.ArrowLocation.TOP_LEFT);
            mainMenuPop.setAutoHide(true);
            hbabt.setRate(1);
            hbabt.play();
            mainMenuPop.show(settings, e.getScreenX(), e.getScreenY() + 20);
        });
        mainMenuPop.setOnHiding(e -> {
            hbabt.setRate(-1);
            hbabt.play();
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

        groupTree.setCellFactory(e -> {
            TreeCell<Group> cell = new TreeCell<Group>() {
                @Override
                protected void updateItem(Group item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null && !item.getName().isEmpty()) {
                        setText(item.getName());
                        if (item.completeProperty() != null) {
                            if (item.isComplete()) {
                                setGraphic(ImgFactory.createView(CHECKMARK));
                                setGraphicTextGap(8);
                                setContentDisplay(ContentDisplay.LEFT);
                                String completed_On = item.getCompleted_On();
                                if (completed_On != null && !completed_On.isEmpty()) {
                                    setTooltip(new Tooltip("Completed: " + LocalDateTime.parse(completed_On).format(DATE_FORMAT)));
                                    getTooltip().setStyle("-fx-text-fill:white;");
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
                                        setGraphicTextGap(8);
                                        setContentDisplay(ContentDisplay.LEFT);
                                        String completed_On = item.getCompleted_On();
                                        if (completed_On != null && !completed_On.isEmpty()) {
                                            setTooltip(new Tooltip("Completed: " + LocalDateTime.parse(completed_On).format(DATE_FORMAT)));
                                            getTooltip().setStyle("-fx-text-fill:white;");
                                        }
                                    } else {
                                        setGraphic(null);
                                        setTooltip(null);
                                    }
                                }
                            });
                        }
                    } else {
                        setText(null);
                        setGraphic(null);
                    }
                }
            };

//            cell.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, evt -> {
//                if (cell.getItem().isEmpty() && !cell.isEmpty()) {
//                    if (cell.getItem().getName().equals(addGroupLabel)) {
//                        TextInputDialog dialog = new TextInputDialog();
//                        dialog.setContentText("Enter item");
//                        dialog.showAndWait().ifPresent(text -> {
//                            final int index = groupTree.getRoot().getChildren().size();
//                            final Group group = new Group(0, 0, getColCombo().getValue(), text, false, LocalDateTime.now().toString(), "");
//                            boolean noMatch = groupTree.getItems().stream().map(Group::getName).noneMatch(e3 -> e3.toLowerCase().equals(group.getName().toLowerCase()));
//                            if (noMatch) {
//                                newGroupHelper(group);
//                                Platform.runLater(() -> {
//                                    groupTree.getRoot().getChildren().add(index, group);
//                                    sortGroupList();
//                                    groupTree.getSelectionModel().select(group);
//                                });
//                                fxTrayIcon.showInfoMessage("Group '" + group.getName() + "' Has Been Created");
//                            } else {
//                                fxTrayIcon.showErrorMessage("Group '" + group.getName() + "' Exists Already!");
//                            }
//                        });
//                    }
//                }

            return cell;
        });

//        groupTree.setButtonCell(new ListCell<Group>() {
//            @Override
//            protected void updateItem(Group item, boolean empty) {
//                super.updateItem(item, empty);
//                if (item != null && !item.getName().isEmpty()) {
//                    setText(item.getName());
//                    if (item.completeProperty() != null && item.isComplete()) {
//                        setGraphic(ImgFactory.createView(CHECKMARK));
//                        String completed_On = item.getCompleted_On();
//                        if (completed_On != null && !completed_On.isEmpty()) {
//                            setTooltip(new Tooltip("Completed: " + LocalDateTime.parse(completed_On).format(DATE_FORMAT)));
//                        }
//                    }
//                }
//            }
//        });

//        groupCombo.setConverter(new StringConverter<Group>() {
//            @Override
//            public String toString(Group group) {
//                if (group == null) {
//                    return null;
//                } else {
//                    return group.getName();
//                }
//            }
//
//            @Override
//            public Group fromString(String group) {
//                return null;
//            }
//        });


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
                        List<TreeItem<Group>> groupTreeList = selColItem.getGroupList().stream().map(TreeItem::new).collect(Collectors.toList());
                        for (int i = 0; i < groupTreeList.size(); i++) {
                            List<TreeItem<Group>> subGroups = groupTreeList.get(i).getValue().getSubGroup().stream().map(e -> new TreeItem<Group>(e)).collect(Collectors.toList());
                            groupTreeList.get(i).getChildren().addAll(subGroups);
                        }
                        groupTree.getRoot().getChildren().addAll(groupTreeList);
                        Platform.runLater(() -> {
                            sortGroupList();
                        });
                    }
                }
            });
        }

        groupTree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<Group>>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem<Group>> observable, TreeItem<Group> oldValue, TreeItem<Group> newValue) {
                if (newValue.isLeaf() && newValue.getChildren().isEmpty()) {
                    StringBuilder sbHelper = new StringBuilder();
                    sbHelper.append(newValue.getValue().getName() + "\\");
                    getSubGroup(newValue, sbHelper);
                    List<String> split = Arrays.asList(sbHelper.toString().split("\\\\"));
                    Collections.reverse(split);
                    System.out.println(split.toString());
                    groupMB.setText(split.stream().reduce("", (i, e) -> i + " \\ " + e));
                    groupMB.hide();
                }
            }
        });

        existColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("exists"));
        existColumn.setComparator(new Comparator<Boolean>() {
            @Override
            public int compare(Boolean o1, Boolean o2) {
                return o1.compareTo(o2);
            }
        });

        existColumn.setCellFactory(e -> {
                    TreeTableCell<T, Boolean> cell = new TreeTableCell<T, Boolean>() {
                        @Override
                        protected void updateItem(Boolean item, boolean empty) {
                            super.updateItem(item, empty);
                            if (item != null) {
                                Label label = new Label();
                                ImageView view;
                                Item itemObj = this.getTreeTableRow().getTreeItem().getValue();
                                boolean exists = itemExists(itemObj);
                                itemObj.existsProperty().set(exists);
                                if (itemObj.exists.get()) {
                                    view = ImgFactory.createView(ImgFactory.IMGS.CHECKMARK);

                                    Tooltip.install(label, new Tooltip("Exists"));
                                } else {
                                    view = ImgFactory.createView(EXMARK);
                                    Tooltip.install(label, new Tooltip("Doesn't Exist!"));
                                }
                                view.setFitHeight(20);
                                view.setFitWidth(20);
                                label.setGraphic(view);
                                label.setPadding(new Insets(0, 0, 0, 8));
                                setGraphic(label);
                                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                            } else {
                                setGraphic(null);
                            }
                        }
                    };
                    return cell;
                }
        );
    }

    private void sortGroupList() {
        FXCollections.sort(groupTree.getRoot().getChildren(), new Comparator<TreeItem<Group>>() {
            @Override
            public int compare(TreeItem<Group> o1, TreeItem<Group> o2) {
                //Keep the "add new doc type" cell at the last index
                if (o1.getValue().getName().equals(addGroupLabel) || o2.getValue().equals(addGroupLabel)) {
                    return 1;
                }
                String o1StringPart = o1.getValue().getName().replaceAll("\\d", "").trim();
                String o2StringPart = o2.getValue().getName().replaceAll("\\d", "").trim();

                if (o1StringPart.equalsIgnoreCase(o2StringPart)) {
                    int[] o1Int = extractInt(o1.getValue().getName());
                    int[] o2Int = extractInt(o2.getValue().getName());

                    for (int i = 0; i < o1Int.length; i++) {
                        for (int j = 0; j < o2Int.length; j++) {
                            boolean o = o1Int[i] > o2Int[j];
                            if (o) {
                                return 1;
                            } else {
                                return 0;
                            }
                        }
                    }
                }
                return o1.getValue().getName().compareTo(o2.getValue().getName());
            }

            int[] extractInt(String s) {
                String[] num = s.replaceAll("\\D", " ").split(" ");
                // return 0 if no digits found
                IntStream sum = Arrays.stream(num).filter(e -> !e.isEmpty()).filter(StringUtils::isNumeric).mapToInt(Integer::parseInt);
                int[] toInt = sum.toArray();
                return toInt;
            }
        });
    }

    private void updateAllHelper(boolean completed) {
        CompletableFuture.runAsync(() -> {
            updateAll(ControllerHandler.selGroup.getItemList());
        }).join();
        updateGroupHandler(completed).join();
        tree.refresh();
    }

    private boolean itemExists(Item itemObj) {
        File[] files = itemObj.getLocation().getParent().toFile().listFiles();
        if (files != null) {
            Optional<File> file = Arrays.stream(files).filter(e -> e.getName().contains(itemObj.getLocation().getFileName().toString())).findAny();
            return file.isPresent();
        }
        return false;
    }

    public abstract void updateItem(String paramString1, String paramString2, Item paramItem);

    public abstract void updateName(String paramString1, String paramString2, Item paramItem);

    public void newGroupHelper(Group group) {

        Connection connection = null;
        ResultSet set = null;
        PreparedStatement ps = null;
        int key = 0;
        try {
            connection = ConnectionHandler.createDBConnection();
            ps = connection.prepareStatement("INSERT INTO `" + DBUtils.DBTable.M.getTable() + "` (name,collection_id,job_id,started_on,employees,status_id) VALUES(?,?,(SELECT id FROM projects WHERE job_id='" + jsonHandler.getSelJobID() + "'),?,?,(SELECT id FROM `sc_group_status` WHERE name=?))", PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, group.getName());
            ps.setInt(2, group.getCollection().getID());
            Date now = formatDateTime(group.getStarted_On());
            ps.setTimestamp(3, new Timestamp(now.toInstant().toEpochMilli()));
            ps.setString(4, ConnectionHandler.user.getName());
            ps.setString(5, "Scanning");
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

    public void setupCompTask() {
        compColumn.getContextMenu().getItems().add(compAll);
        compAll.setOnAction(e -> {
            CompletableFuture.runAsync(() -> {
                ControllerHandler.selGroup.getItemList().forEach(e2 -> e2.getCompleted().setSelected(true));
            }).thenRunAsync(() -> updateAllHelper(false));
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
    public void updateTotal() {
//        final AtomicInteger ai = new AtomicInteger(0);
//
//        if (tree.getRoot() != null && !tree.getRoot().getChildren().isEmpty()) {
//            for (TreeItem<? extends Item> child : tree.getRoot().getChildren()) {
//                ai.getAndAdd(child.getValue().total.get());
//                for (TreeItem<? extends Item> childChild : child.getChildren()) {
//                    ai.getAndAdd(childChild.getValue().getTotal());
//                }
//            }
//        }
//        groupCountProp.setValue(ai.get());
    }

    @Override
    public ObservableList<? extends Item> getGroupItems(Group group) {
        return null;
    }


    @Override
    public void legalTextTest(boolean isLegal, CustomTextField node) {

    }

    public SearchableComboBox<Collection> getColCombo() {
        return colCombo;
    }

}
