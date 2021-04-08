package com.idi.userlogin.Controllers;

import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.tableview2.TableColumn2;
import org.controlsfx.control.tableview2.TableView2;
import org.controlsfx.control.textfield.CustomTextField;
import com.idi.userlogin.JavaBeans.Collection;
import com.idi.userlogin.JavaBeans.Group;
import com.idi.userlogin.JavaBeans.Item;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

import static com.idi.userlogin.Main.devices;
import static com.idi.userlogin.utils.Utils.DATE_FORMAT;

public class CheckListController extends BaseEntryController<BaseEntryController.EntryItem> implements Initializable {

    private FilteredList<EntryItem> filteredData;
    private SortedList<EntryItem> sortedData;

    @FXML
    private CustomTextField search;

    @FXML
    private ComboBox<String> clFilter;

    @FXML
    private ComboBox<String> clGroup;

    @FXML
    private TableColumn2<EntryItem, CheckBox> cComp;

    @FXML
    private TableColumn2<EntryItem, Label> details;

    @FXML
    private TableColumn2<EntryItem, String> cID;

    @FXML
    private TableColumn2<EntryItem, String> cName;

    @FXML
    private TableColumn2<EntryItem, Integer> cTotal;

    @FXML
    private TableColumn2<EntryItem, Label> cType;

    @FXML
    private TableColumn2<EntryItem, String> cStarted;

    @FXML
    private TableColumn2<EntryItem, String> cCompOn;

    @FXML
    private TableColumn2<EntryItem, Group> cGroup;

    @FXML
    private TableColumn2<EntryItem, Collection> cCollect;

    @FXML
    private TableColumn2<EntryItem, Integer> cNonFeed;
    @FXML
    private TableColumn2<EntryItem, String> cComments;
    @FXML
    private TableView2<EntryItem> clAllTable;

    @Override
    public int insertHelper(Item<? extends Item> item) {
        return 0;
    }

    @Override
    public void resetFields() {

    }


    public class Wrapper {
        private JIBController.JIBEntryItem jibItem;
        private EntryItem entryItem;


    }

//    public ObservableList<ChecklistItem> getCLItems() {
//        Connection connection = null;
//        ResultSet set = null;
//        PreparedStatement ps = null;
//        final ObservableList<ChecklistItem> cl_Items = FXCollections.observableArrayList();
//
//        try {
//            connection = ConnectionHandler.createDBConnection();
//            ps = connection.prepareStatement("SELECT m.id,g.id as group_id, g.name as group_name,g.scanned as g_completed,g.completed_on, m.name as item_Name,m.non_feeder, m.completed, e.name as employee,c.id as col_id, c.name as collection, m.total, t.name as type,m.comments,m.started_on,m.conditions,m.completed_on FROM `" + jsonHandler.getSelJobID() + "` m INNER JOIN employees e ON m.employee_id = e.id INNER JOIN sc_groups g ON m.group_id = g.id INNER JOIN item_types t ON m.type_id = t.id INNER JOIN sc_collections c ON m.collection_id = c.id");
//            set = ps.executeQuery();
//            while (set.next()) {
//                final String m_started_on = (set.getString("m.started_on")) == null ? set.getString("m.started_on") : "";
//                final String g_completed_On = (set.getString("g.completed_on")) == null ? set.getString("g.completed_on") : "";
//                final String m_completed_On = (set.getString("m.completed_on")) == null ? set.getString("m.completed_on") : "";
//                final ChecklistItem item = new ChecklistItem(set.getInt("m.id"), new Group(set.getInt("group_id"), set.getInt("col_id"), set.getString("collection"), set.getString("group_name"), set.getInt("g_completed") == 1, g_completed_On), set.getString("item_Name"), set.getInt("m.total"), set.getInt("m.non_feeder"), set.getString("type"), set.getInt("m.completed") == 1, set.getString("m.comments"), m_started_on, m_completed_On);
//                String condition = set.getString("m.conditions");
//                if (condition != null && !condition.isEmpty()) {
//                    String[] splitConditions = condition.split(", ");
//                    item.setConditions(Arrays.asList(splitConditions));
//                }
//                cl_Items.add(item);
//            }
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } finally {
//            DbUtils.closeQuietly(set);
//            DbUtils.closeQuietly(ps);
//            DbUtils.closeQuietly(connection);
//        }
//
//        return cl_Items;
//    }

    @Override
    public void complete() {

    }

    @Override
    public void insert() throws IOException {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        checkListController = this;
        cID.setCellValueFactory(new PropertyValueFactory<>("id"));
        details.setCellValueFactory(new PropertyValueFactory<>("details"));
        cName.setCellValueFactory(new PropertyValueFactory<>("name"));
        cType.setCellValueFactory(new PropertyValueFactory<>("type"));
        cGroup.setCellValueFactory(new PropertyValueFactory<>("group"));
        cGroup.setCellFactory(e -> {
            TableCell<EntryItem, Group> cell = new TableCell<EntryItem, Group>() {
                @Override
                protected void updateItem(Group item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null && !item.getName().isEmpty()) {
                        setText(item.getName());
                        final Hyperlink link = new Hyperlink(item.getName());
                        link.setOnAction(e -> {
                            System.out.println("Link Clicked!");
                            setupGroupPop(item);
                        });
                        setGraphic(link);
                        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    } else {
                        setGraphic(null);
                        setText("");
                    }
                }
            };

            return cell;
        });

        cCollect.setCellValueFactory(new PropertyValueFactory<>("collection"));
        cCollect.setCellFactory(e -> {
            TableCell<EntryItem, Collection> colCell = new TableCell<EntryItem, Collection>() {
                @Override
                protected void updateItem(Collection item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null && !item.getName().isEmpty()) {
                        setText(item.getName());
                        final Hyperlink link = new Hyperlink(item.getName());
                        link.setOnAction(e -> {
                            System.out.println("Link Clicked!");
                        });
                        setGraphic(link);
                        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    } else {
                        setGraphic(null);
                        setText("");
                    }
                }
            };

            return colCell;
        });
        cTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        cStarted.setCellValueFactory(new PropertyValueFactory<>("started_On"));
        cStarted.setCellFactory(e -> new TimeDateCell());
        cCompOn.setCellValueFactory(new PropertyValueFactory<>("completed_On"));
        cCompOn.setCellFactory(e -> new TimeDateCell());
        cNonFeed.setCellValueFactory(new PropertyValueFactory<>("nonFeeder"));
        cComp.setCellValueFactory(new PropertyValueFactory<>("completed"));
        cComp.setCellFactory(e -> {
            TableCell<EntryItem, CheckBox> comp = new TableCell<EntryItem, CheckBox>() {
                @Override
                protected void updateItem(CheckBox item, boolean empty) {
                    if (item != null) {
                        setGraphic(item);
                    } else {
                        setGraphic(null);
                    }
                    super.updateItem(item, empty);
                }
            };
            return comp;
        });
        cComments.setCellValueFactory(new PropertyValueFactory<>("comments"));

        cComments.setCellFactory(e -> {
            TableCell<EntryItem, String> cell = new TableCell<EntryItem, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null && !item.isEmpty()) {
                        Text text = new Text(item);
                        text.setStyle("-fx-text-alignment:center;-fx-padding:8,8,8,8;-fx-font-size:14;");
                        text.wrappingWidthProperty().bind(getTableColumn().widthProperty().subtract(35));
                        setGraphic(text);
                        this.setPadding(new Insets(8, 8, 8, 8));
//                        setText(text.getText());
                    } else {
                        setGraphic(null);
                        setText("");
                    }
                }
            };
            return cell;
        });

        filteredData = new FilteredList<EntryItem>(getClAllTable().getItems(), p -> true);
        // Use textProperty
        search.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(p -> {
                // If filter text is empty, display all persons.
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                final String lowerCaseFilter = newValue.toLowerCase();

                if (p.name.getText().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (p.group.getName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (p.group.getCollection().getName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (p.getStarted_On().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (p.getCompleted_On() != null && p.getCompleted_On().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (p.comments.get().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false; // Does not match.
            });
            clAllTable.setItems(filteredData);
        });

        sortedData = new SortedList<EntryItem>(filteredData);
        sortedData.comparatorProperty().bind(clAllTable.comparatorProperty());
//        clTable.setItems(sortedData);
    }

    //Read-Only Scene
    //TODO: This scene needs to be planned
//    public void setupColPop(Group group) {
//
//        final FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ColPop.fxml"));
//        Parent root = null;
//        try {
//            root = loader.load();
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//
//        final ColPopController detailPopCont = loader.getController();
//        final PopOver popOver = new PopOver(root);
//        popOver.setDetached(true);
//        popOver.setTitle(group.getName());
//
//        popOver.setOnHiding(e2 -> {
//            getOpaqueOverlay().setVisible(popOver.isShowing());
//            opaquePOS();
//        });
//
//        popOver.showingProperty().addListener(e2 -> {
//            getOpaqueOverlay().setVisible(popOver.isShowing());
//            opaquePOS();
//        });
//
//        popOver.show(clAllTable.getScene().getWindow());
//
////        if (group.getLocation() != null) {
////            detailPopCont.location.setText(item.getLocation().toString());
////        }
//
//        if (group.getCompleted_On() != null && !group.getCompleted_On().isEmpty()) {
//            detailPopCont.compOn.setText(LocalDateTime.parse(group.getCompleted_On().replace(" ", "T")).format(DATE_FORMAT));
//        }
//
//        if (group.getStarted_On() != null && !group.getStarted_On().isEmpty()) {
//            detailPopCont.startedOn.setText(LocalDateTime.parse(group.getStarted_On().replace(" ", "T")).format(DATE_FORMAT));
//        }
//
//        if (group.getCollection().getName() != null && !group.getCollection().getName().isEmpty()) {
//            detailPopCont.collection.setText(group.getCollection().getName());
//        }
//
//        int sum = group.getItemList().stream().map(Item::getTotal).mapToInt(e -> e).sum();
//        if (sum > 0) {
//            detailPopCont.total.setText(String.valueOf(sum));
//        }
//    }


    //Read-Only Scene
    public void setupGroupPop(Group group) {

        final FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/GroupPop.fxml"));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        final GroupPopController detailPopCont = loader.getController();
        final PopOver popOver = new PopOver(root);
        popOver.setDetached(true);
        popOver.setTitle(group.getName());

        popOver.setOnHiding(e2 -> {
            getOpaqueOverlay().setVisible(popOver.isShowing());
            opaquePOS();
        });

        popOver.showingProperty().addListener(e2 -> {
            getOpaqueOverlay().setVisible(popOver.isShowing());
            opaquePOS();
        });

        popOver.show(clAllTable.getScene().getWindow());

//        if (group.getLocation() != null) {
//            detailPopCont.location.setText(item.getLocation().toString());
//        }

        if (group.getCompleted_On() != null && !group.getCompleted_On().isEmpty()) {
            detailPopCont.compOn.setText(LocalDateTime.parse(group.getCompleted_On().replace(" ", "T")).format(DATE_FORMAT));
        }

        if (group.getStarted_On() != null && !group.getStarted_On().isEmpty()) {
            detailPopCont.startedOn.setText(LocalDateTime.parse(group.getStarted_On().replace(" ", "T")).format(DATE_FORMAT));
        }

        if (group.getCollection().getName() != null && !group.getCollection().getName().isEmpty()) {
            detailPopCont.collection.setText(group.getCollection().getName());
        }

        int sum = group.getItemList().stream().map(Item::getTotal).mapToInt(e -> e).sum();
        if (sum > 0) {
            detailPopCont.total.setText(String.valueOf(sum));
        }
    }

    public class TimeDateCell extends TableCell<EntryItem, String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null) {
                if (item.isEmpty()) {
                    setText("-");
                } else {
                    item = LocalDateTime.parse(item.replace(" ", "T")).format(DATE_FORMAT);
                    setStyle("-fx-opacity:.7;-fx-font-size:12;-fx-alignment:center;");
                    setAlignment(Pos.CENTER);
                    setText(item);
                }
            } else {
                setGraphic(null);
                setText("");
            }
        }
    }

    public TableView2<EntryItem> getClAllTable() {
        return clAllTable;
    }

    public FilteredList<EntryItem> getFilteredData() {
        return filteredData;
    }

    public void setFilteredData(FilteredList<EntryItem> filteredData) {
        this.filteredData = filteredData;
    }

    public SortedList<EntryItem> getSortedData() {
        return sortedData;
    }

    public void setSortedData(SortedList<EntryItem> sortedData) {
        this.sortedData = sortedData;
    }
}
