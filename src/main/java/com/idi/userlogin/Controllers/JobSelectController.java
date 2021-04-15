package com.idi.userlogin.Controllers;

import com.idi.userlogin.JavaBeans.Collection;
import com.idi.userlogin.JavaBeans.Group;
import com.idi.userlogin.Main;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.apache.commons.dbutils.DbUtils;
import org.controlsfx.control.SearchableComboBox;
import com.idi.userlogin.utils.FXUtils;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;

import static com.idi.userlogin.Main.jsonHandler;
import static com.idi.userlogin.utils.Utils.getCollections;
import static com.idi.userlogin.utils.Utils.getGroups;

public class JobSelectController implements Initializable {

    @FXML
    private Label greetLbl;
    @FXML
    private VBox greetPane;
    @FXML
    private SearchableComboBox<Jobs> jobID;

    @FXML
    private void toMain() throws IOException {
        ControllerHandler.sceneTransition(ControllerHandler.mainMenuController.root, getClass().getResource("/fxml/MainMenu.fxml"), false);
    }

    @FXML
    private void confirm() throws IOException, SQLException {

        if (jobID.getSelectionModel().getSelectedItem() != null) {
            jsonHandler.setSelJobID(jobID.getSelectionModel().getSelectedItem().getJobId());
            jsonHandler.setSelJobDesc(jobID.getSelectionModel().getSelectedItem().getDesc());
            ControllerHandler.checkListScene = FXMLLoader.load(BaseEntryController.class.getResource("/fxml/CheckListView.fxml"));

            final Stage stage = (Stage) ControllerHandler.mainMenuController.root.getScene().getWindow();
            stage.setMaximized(true);
            ObservableList<Collection> collections = getCollections();
            //Add the corresponding groups to the right collection and set complete datetime
            collections.forEach(e -> {
                ObservableList<Group> groups = getGroups(e);
                e.getGroupList().addAll(groups);
            });
            if (jobID.getSelectionModel().getSelectedItem().jobId.contains("JIB")) {
                ControllerHandler.sceneTransition(ControllerHandler.mainMenuController.root, getClass().getResource("/fxml/JIB.fxml"), true);
                ControllerHandler.jibController.getRoot().setPrefSize(ControllerHandler.mainMenuController.root.getWidth(), ControllerHandler.mainMenuController.root.getHeight());
                ControllerHandler.jibController.getCollectionList().addAll(collections);
                ControllerHandler.jibController.getColCombo().getItems().addAll(collections);
                collections.forEach(e -> {
                    e.getGroupList().forEach(e2 -> {
                        ObservableList<JIBController.JIBEntryItem> entryItems = (ObservableList<JIBController.JIBEntryItem>) e2.getItemList();
                        ObservableList<JIBController.JIBEntryItem> groupItems = (ObservableList<JIBController.JIBEntryItem>) ControllerHandler.jibController.getGroupItems(e2);
                        entryItems.addAll(groupItems);
                    });
                });
                ControllerHandler.jibController.initCheckListScene();
            } else {
                ControllerHandler.sceneTransition(ControllerHandler.mainMenuController.root, getClass().getResource("/fxml/EntryCheckList.fxml"), true);
                ControllerHandler.entryController.getRoot().setPrefSize(ControllerHandler.mainMenuController.root.getWidth(), ControllerHandler.mainMenuController.root.getHeight());
                ControllerHandler.entryController.getCollectionList().addAll(collections);
//                ControllerHandler.entryController.getGroupCombo().getItems().addAll(collections.get(0).getGroupList());
                ControllerHandler.entryController.getColCombo().getItems().addAll(collections);
                collections.forEach(e -> {
                    e.getGroupList().forEach(e2 -> {
                        ObservableList entryItems = e2.getItemList();
                        ObservableList<?> groupItems = ControllerHandler.entryController.getGroupItems(e2);
                        entryItems.addAll(groupItems);
                    });
                });
                ControllerHandler.entryController.initCheckListScene();
            }
            ControllerHandler.loggedInController.getDesc().setText(jsonHandler.getSelJobDesc());
            ControllerHandler.loggedInController.getName().setText(jsonHandler.name);
            ControllerHandler.loggedInController.getJobID().setText(jsonHandler.getSelJobID());
        }
    }


    private ObservableList<Jobs> getJobs() {
        Connection connection = null;
        ResultSet set = null;
        PreparedStatement ps = null;
        ObservableList<Jobs> jobs = FXCollections.observableArrayList();

        try {
            connection = ConnectionHandler.createDBConnection();
            ps = connection.prepareStatement("SELECT Job_ID,Description from Projects WHERE Complete = '0'");
            set = ps.executeQuery();
            while (set.next()) {
                jobs.add(new Jobs(set.getString("Job_ID"), set.getString("Description")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Main.LOGGER.log(Level.SEVERE, "There was an error getting the jobs from the db!", e);

        } finally {
            DbUtils.closeQuietly(set);
            DbUtils.closeQuietly(ps);
            DbUtils.closeQuietly(connection);
        }
        return jobs;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        greetLbl.setText("Welcome, " + jsonHandler.name + "!");
        FXUtils.fadeIn(greetPane, Duration.seconds(2));
        jobID.getItems().setAll(getJobs());
        jobID.setButtonCell(new JobListCell());
        jobID.setCellFactory(e -> new JobListCell());
        jobID.setConverter(new StringConverter<Jobs>() {
            @Override
            public String toString(Jobs object) {
                if (object != null) {
                    return object.jobId;
                }
                return "";
            }

            @Override
            public Jobs fromString(String string) {
                if (string == null || string.isEmpty()) {
                    return null;
                } else {
                    return jobID.getValue();
                }
            }
        });

        jobID.getEditor().setTextFormatter(new TextFormatter<Jobs>(new StringConverter<Jobs>() {
            @Override
            public String toString(Jobs object) {
                if (object != null) {
                    return object.jobId;
                }
                return "";
            }

            @Override
            public Jobs fromString(String string) {
                if (string == null || string.isEmpty()) {
                    return null;
                } else {
                    return jobID.getValue();
                }
            }
        }));

    }


    public class Jobs {
        private String jobId;
        private String desc;

        public Jobs(String jobId, String desc) {
            this.jobId = jobId;
            this.desc = desc;
        }

        public String getJobId() {
            return jobId;
        }

        public void setJobId(String jobId) {
            this.jobId = jobId;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    public class JobListCell extends ListCell<Jobs> {

        @Override
        protected void updateItem(Jobs item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null && !item.getJobId().isEmpty()) {
                setText(item.getJobId() + " (" + item.getDesc() + ")");
            } else {
                setText("");
                setGraphic(null);
            }
        }

        @Override
        public void startEdit() {
            super.startEdit();
        }
    }
}
