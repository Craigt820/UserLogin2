package com.idi.userlogin.Controllers;

import com.idi.userlogin.Handlers.ConnectionHandler;
import com.idi.userlogin.Handlers.ControllerHandler;
import com.idi.userlogin.JavaBeans.Collection;
import com.idi.userlogin.JavaBeans.Group;
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

            Job selJob = jobID.getSelectionModel().getSelectedItem();

            if (selJob.name.contains("JIB")) {
                ControllerHandler.sceneTransition(ControllerHandler.mainMenuController.root, getClass().getResource("/fxml/JIB.fxml"), true);
                ControllerHandler.jibController.getRoot().setPrefSize(ControllerHandler.mainMenuController.root.getWidth(), ControllerHandler.mainMenuController.root.getHeight());
                ControllerHandler.jibController.getCollectionList().addAll(collections);
                ControllerHandler.jibController.getColCombo().getItems().addAll(collections);
            } else {
                if (selJob.userEntry) {
                    ControllerHandler.sceneTransition(ControllerHandler.mainMenuController.root, getClass().getResource("/fxml/UserEntry.fxml"), true);
                    ControllerHandler.entryController.getRoot().setPrefSize(ControllerHandler.mainMenuController.root.getWidth(), ControllerHandler.mainMenuController.root.getHeight());
                    ControllerHandler.entryController.getCollectionList().addAll(collections);
                    ControllerHandler.entryController.getColCombo().getItems().addAll(collections);
                } else {
                    ControllerHandler.sceneTransition(ControllerHandler.mainMenuController.root, getClass().getResource("/fxml/ManifestView.fxml"), true);
                    ControllerHandler.maniViewController.getRoot().setPrefSize(ControllerHandler.mainMenuController.root.getWidth(), ControllerHandler.mainMenuController.root.getHeight());
                    ControllerHandler.maniViewController.getCollectionList().addAll(collections);
                    ControllerHandler.maniViewController.getColCombo().getItems().addAll(collections);
                    ControllerHandler.maniViewController.setUid(selJob.getUid());
                }
            }

            ControllerHandler.loggedInController.getDesc().setText(jsonHandler.getSelJobDesc());
            ControllerHandler.loggedInController.getName().setText(jsonHandler.name);
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
            ps = connection.prepareStatement("SELECT id,client_id,user_entry,uid,complete,job_id,description from Projects");
            set = ps.executeQuery();
            while (set.next()) {
                jobs.add(new Job(set.getInt("id"), set.getString("job_id"), Utils.intToBoolean(set.getInt("user_entry")), Utils.intToBoolean(set.getInt("complete")), set.getString("uid"), set.getString("description")));
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
        jobID.setConverter(new StringConverter<Job>() {
            @Override
            public String toString(Job object) {
                if (object != null) {
                    return object.name;
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
                    return object.name;
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

    public static class Job {
        private String desc;
        private String name;
        private int id;
        private boolean userEntry;
        private boolean complete;
        private String folder_Struct;
        private int loc_id;
        private int client_id;
        private String uid;
        private String description;

        public Job(int id, String name, boolean entry, boolean complete, String uid, String desc) {
            this.id = id;
            this.name = name;
            this.desc = desc;
            this.uid = uid;
            this.userEntry = entry;
            this.complete = complete;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public boolean isUserEntry() {
            return userEntry;
        }

        public void setUserEntry(boolean userEntry) {
            this.userEntry = userEntry;
        }

        public boolean isComplete() {
            return complete;
        }

        public void setComplete(boolean complete) {
            this.complete = complete;
        }

        public String getFolder_Struct() {
            return folder_Struct;
        }

        public void setFolder_Struct(String folder_Struct) {
            this.folder_Struct = folder_Struct;
        }

        public int getLoc_id() {
            return loc_id;
        }

        public void setLoc_id(int loc_id) {
            this.loc_id = loc_id;
        }

        public int getClient_id() {
            return client_id;
        }

        public void setClient_id(int client_id) {
            this.client_id = client_id;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
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
