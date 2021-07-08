package com.idi.userlogin.Controllers;

import com.idi.userlogin.Handlers.ConnectionHandler;
import com.idi.userlogin.Handlers.ControllerHandler;
import com.idi.userlogin.JavaBeans.Collection;
import com.idi.userlogin.JavaBeans.Group;
import com.idi.userlogin.JavaBeans.Job;
import com.idi.userlogin.Main;
import com.idi.userlogin.utils.Utils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
import java.util.*;
import java.util.logging.Level;

import static com.idi.userlogin.Handlers.ControllerHandler.groupCountProp;
import static com.idi.userlogin.Main.jsonHandler;
import static com.idi.userlogin.utils.Utils.getCollections;
import static com.idi.userlogin.utils.Utils.getGroups;

public class JobSelectController implements Initializable {

    @FXML
    private Label greetLbl;
    @FXML
    private VBox greetPane;
    @FXML
    private SearchableComboBox<Job> jobID;

    @FXML
    private void toMain() throws IOException {
        ControllerHandler.sceneTransition(ControllerHandler.mainMenuController.root, getClass().getResource("/fxml/MainMenu.fxml"), false);
    }

    @FXML
    private void confirm() throws IOException, SQLException {

        if (jobID.getSelectionModel().getSelectedItem() != null) {
            jsonHandler.setSelJobID(jobID.getSelectionModel().getSelectedItem().getName());
            jsonHandler.setSelJobDesc(jobID.getSelectionModel().getSelectedItem().getDesc());
//            ControllerHandler.checkListScene = FXMLLoader.load(BaseEntryController.class.getResource("/fxml/CheckListView.fxml"));

            final Stage stage = (Stage) ControllerHandler.mainMenuController.root.getScene().getWindow();
            stage.setMaximized(true);
            ObservableList<Collection> collections = getCollections();
            //Add the corresponding groups to the right collection and set complete datetime
            collections.forEach(e -> {
                ObservableList<Group> groups = getGroups(e);
                e.getGroupList().addAll(groups);
            });

            final Job selJob = jobID.getSelectionModel().getSelectedItem();
            ControllerHandler.selJob = selJob;

            if (selJob.getName().contains("JIB")) {
                ControllerHandler.sceneTransition(ControllerHandler.mainMenuController.root, getClass().getResource("/fxml/JIB.fxml"), true);
                ControllerHandler.jibController.getRoot().setPrefSize(ControllerHandler.mainMenuController.root.getWidth(), ControllerHandler.mainMenuController.root.getHeight());
                ControllerHandler.jibController.getCollectionList().addAll(collections);
                ControllerHandler.jibController.getColCombo().getItems().addAll(collections);
            } else if (selJob.getName().contains("GILEAD")) {
                ControllerHandler.sceneTransition(ControllerHandler.mainMenuController.root, getClass().getResource("/fxml/Gilead.fxml"), true);
                ControllerHandler.gileadController.getRoot().setPrefSize(ControllerHandler.mainMenuController.root.getWidth(), ControllerHandler.mainMenuController.root.getHeight());
                ControllerHandler.gileadController.getCollectionList().addAll(collections);
                ControllerHandler.gileadController.getColCombo().getItems().addAll(collections);
                ControllerHandler.gileadController.setUid(selJob.getUid());
                ControllerHandler.gileadController.setGroupCol(selJob.getGroupCol());
            } else {
                if (selJob.isUserEntry()) {
                    ControllerHandler.sceneTransition(ControllerHandler.mainMenuController.root, getClass().getResource("/fxml/UserEntry.fxml"), true);
                    ControllerHandler.entryController.getRoot().setPrefSize(ControllerHandler.mainMenuController.root.getWidth(), ControllerHandler.mainMenuController.root.getHeight());
                    ControllerHandler.entryController.getCollectionList().addAll(collections);
                    ControllerHandler.entryController.getColCombo().getItems().addAll(collections);
                    ControllerHandler.entryController.setGroupCol(selJob.getGroupCol());
                } else {
                    ControllerHandler.sceneTransition(ControllerHandler.mainMenuController.root, getClass().getResource("/fxml/ManifestView.fxml"), true);
                    ControllerHandler.maniViewController.getRoot().setPrefSize(ControllerHandler.mainMenuController.root.getWidth(), ControllerHandler.mainMenuController.root.getHeight());
                    ControllerHandler.maniViewController.getCollectionList().addAll(collections);
                    ControllerHandler.maniViewController.getColCombo().getItems().addAll(collections);
                    ControllerHandler.maniViewController.setUid(selJob.getUid());
                    ControllerHandler.maniViewController.setGroupCol(selJob.getGroupCol());
                }
            }

            ControllerHandler.loggedInController.getDesc().setText(jsonHandler.getSelJobDesc());
            ControllerHandler.loggedInController.getName().setText(ConnectionHandler.user.getName());
            ControllerHandler.loggedInController.getJobID().setText(jsonHandler.getSelJobID());

        }
        groupCountProp.setValue(0);
    }


    private ObservableList<Job> getJobs() {
        Connection connection = null;
        ResultSet set = null;
        PreparedStatement ps = null;
        ObservableList<Job> jobs = FXCollections.observableArrayList();

        try {
            connection = ConnectionHandler.createDBConnection();
            ps = connection.prepareStatement("SELECT p.id,p.client_id,p.user_entry,p.uid,p.group_col,p.complete,p.job_id,c.name from Projects p LEFt JOIN sc_collections c ON p.id = c.job_id");
            set = ps.executeQuery();
            while (set.next()) {
                jobs.add(new Job(set.getInt("p.id"), set.getString("p.job_id"), Utils.intToBoolean(set.getInt("p.user_entry")), Utils.intToBoolean(set.getInt("p.complete")), set.getString("p.uid"), set.getString("p.group_col"), set.getString("c.name")));
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
        greetLbl.setText("Welcome, " + ConnectionHandler.user.getName() + "!");
        FXUtils.fadeIn(greetPane, Duration.seconds(2));
        jobID.getItems().setAll(getJobs());
        FXCollections.sort(jobID.getItems(), new Comparator<Job>() {
            @Override
            public int compare(Job o1, Job o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        jobID.setButtonCell(new JobListCell());
        jobID.setCellFactory(e -> new JobListCell());
        jobID.setConverter(new StringConverter<Job>() {
            @Override
            public String toString(Job object) {
                if (object != null) {
                    return object.getName();
                }
                return "";
            }

            @Override
            public Job fromString(String string) {
                if (string == null || string.isEmpty()) {
                    return null;
                } else {
                    return jobID.getValue();
                }
            }
        });

        jobID.getEditor().setTextFormatter(new TextFormatter<Job>(new StringConverter<Job>() {
            @Override
            public String toString(Job object) {
                if (object != null) {
                    return object.getName();
                }
                return "";
            }

            @Override
            public Job fromString(String string) {
                if (string == null || string.isEmpty()) {
                    return null;
                } else {
                    return jobID.getValue();
                }
            }
        }));

    }

    public class JobListCell extends ListCell<Job> {

        @Override
        protected void updateItem(Job item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null && !item.getName().isEmpty()) {
                setText(item.getName() + " (" + item.getDesc() + ")");
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
