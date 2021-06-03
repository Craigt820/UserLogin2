package com.idi.userlogin.Controllers;

import com.idi.userlogin.Handlers.ConnectionHandler;
import com.idi.userlogin.Handlers.ControllerHandler;
import com.idi.userlogin.utils.DailyLog;
import com.jfoenix.controls.JFXDrawer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import com.idi.userlogin.Main;
import com.idi.userlogin.utils.CustomAlert;
import org.apache.commons.dbutils.DbUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.*;
import java.util.logging.Level;

import static com.idi.userlogin.Handlers.ControllerHandler.*;
import static com.idi.userlogin.Main.jsonHandler;

public class LoggedInController implements Initializable {

    @FXML
    private AnchorPane root;
    @FXML
    private Label hour, min, sec;
    @FXML
    private Button pause_resume;
    @FXML
    private Label name;
    @FXML
    private Label status;
    @FXML
    private Label desc;
    @FXML
    private Label jobID;
    @FXML
    private Label jobTotals;
    @FXML
    private Label job1Total;

    @FXML
    private JFXDrawer specsDrawer;

    private ScheduledFuture timeClock;

    private int h, m, s;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ControllerHandler.loggedInController = this;
        setTimeService();
    }

    private void setTimeService() {
        ScheduledExecutorService executors = Executors.newScheduledThreadPool(2);
        timeClock = executors.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                runTime();
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
    }

    @FXML
    void showSpecs() throws IOException {
        specsDrawer.setPrefSize(315, 680);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Specs.fxml"));
        AnchorPane content = loader.load();
        SpecsController controller = loader.getController();
        controller.getClose().setOnMouseClicked(e -> {
            specsDrawer.close();
            specsDrawer.setOnDrawerClosed(e4 -> {
                specsDrawer.setPrefWidth(0);
                specsDrawer.setMinWidth(0);
            });

            specsDrawer.setOnDrawerClosing(e3 -> {

            });
        });
        specsDrawer.setSidePane(content);
        specsDrawer.open();
    }

    @FXML
    void changeJob() {
        ControllerHandler.sceneTransition(ControllerHandler.mainMenuController.root, getClass().getResource("/fxml/JobSelect.fxml"), false);
        if (ControllerHandler.getMainMenuPop().isShowing()) {
            ControllerHandler.getMainMenuPop().hide();
        }
    }

    public static void updateStatus(String status) {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = ConnectionHandler.createDBConnection();
            ps = connection.prepareStatement("UPDATE `employees` SET status=(SELECT id from ul_status WHERE name='" + status + "') WHERE id=(SELECT id FROM employees WHERE name='" + jsonHandler.getName() + "')");
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            Main.LOGGER.log(Level.SEVERE, "There was an error updating the scan log", e);
        } finally {
            DbUtils.closeQuietly(ps);
            DbUtils.closeQuietly(connection);
        }
    }

    @FXML
    void signOut() {
        ControllerHandler.getOpaqueOverlay().setVisible(true);
        final CustomAlert alert = new CustomAlert(Alert.AlertType.NONE, "Are you sure you want to sign out?", ButtonType.YES, ButtonType.NO);
        alert.setHeaderText("Sign Out");
        final Optional<ButtonType> wait = alert.showAndWait();
        if (wait.isPresent()) {
            final String elapsed = hour.getText() + " : " + min.getText() + " : " + sec.getText();
            if (wait.get().equals(ButtonType.YES)) {
                updateStatus("Offline");
                if (ControllerHandler.getMainMenuPop().isShowing()) {
                    ControllerHandler.getMainMenuPop().hide();
                }

                ControllerHandler.sceneTransition(ControllerHandler.mainMenuController.root, getClass().getResource("/fxml/MainMenu.fxml"), false);
                resetTime();

                if (getMainTreeView() != null) {
                    CompletableFuture.runAsync(() -> {
                        updateAll(selGroup.getItemList());
                    }).thenRunAsync(BaseEntryController::countGroupTotal).thenRunAsync(() -> {
                        updateGroup(false);
                    }).thenRunAsync(()->DailyLog.updateTotal(selGroup.getID())).thenRunAsync(() -> {
                        updateStatus("Offline");
                    }).thenRunAsync(() -> {
                        DailyLog.endDailyLog();
                    }).join();

                    Main.fxTrayIcon.showInfoMessage("Time Elapsed: " + elapsed + "\nToday's Total: " + jsonHandler.getSelJobID() + ": " + getJob1Total().getText());
                }
            }
            ControllerHandler.getOpaqueOverlay().setVisible(false);
        }
    }

    private void resetTime() {
        h = 0;
        m = 0;
        s = 0;
        sec.setText(String.format("%02d", s));
        min.setText(String.format("%02d", m));
        hour.setText(String.format("%02d", h));
    }

    @FXML
    private void pause_Resume() throws URISyntaxException {
        switch (pause_resume.getText()) {

            case "Resume":
                pause_resume.setText("Pause");
                status.setText("Online");
                status.setStyle("-fx-font-size: 14;-fx-font-weight:bold;-fx-text-fill:#12960d;");
                if (timeClock.isCancelled()) {
                    setTimeService();
                }
                updateStatus("Online");
                ControllerHandler.getOpaqueOverlay().setVisible(false);
                ImageView pause = new ImageView(new Image(getClass().getResource("/images/pause.png").toURI().toString()));
                pause.setEffect(new ColorAdjust(0, 0, 1.0, 0.0));
                pause.setTranslateX(62.0);
                pause.setFitHeight(16);
                pause.setFitWidth(16);
                pause_resume.setGraphic(pause);
                break;
            case "Pause":
                if (getMainTreeView() != null) {
                    CompletableFuture.runAsync(() -> {
                        updateAll(selGroup.getItemList());
                    }).thenRunAsync(BaseEntryController::countGroupTotal).thenRunAsync(() -> {
                        updateGroup(false);
                    }).thenRunAsync(()->DailyLog.updateTotal(selGroup.getID())).thenRunAsync(() -> {
                        updateStatus("Away");
                    });
                }
                pause_resume.setText("Resume");
                ControllerHandler.getOpaqueOverlay().setVisible(true);
                timeClock.cancel(true);
                status.setText("Away");
                status.setStyle("-fx-font-size: 14;-fx-font-weight:bold;-fx-text-fill:#bC2414;");
                ImageView play = new ImageView(new Image(getClass().getResource("/images/play.png").toURI().toString()));
                play.setEffect(new ColorAdjust(0, 0, 1.0, 0.0));
                play.setFitHeight(16);
                play.setFitWidth(16);
                play.setTranslateX(60.0);
                pause_resume.setGraphic(play);
                break;
        }
    }

    public void runTime() {
        Platform.runLater(() -> {
            if (h <= 60) {
                s++;
                sec.setText(String.format("%02d", s));
            }

            if (s >= 60) {
                s = 0;
                m++;
                sec.setText(String.format("%02d", s));
                min.setText(String.format("%02d", m));
            }

            if (m >= 60) {
                m = 0;
                h++;
                min.setText(String.format("%02d", m));
                hour.setText(String.format("%02d", h));
            }
        });
    }

    public Label getName() {
        return name;
    }

    public void setName(Label name) {
        this.name = name;
    }

    public Label getStatus() {
        return status;
    }

    public void setStatus(Label status) {
        this.status = status;
    }

    public Label getDesc() {
        return desc;
    }

    public void setDesc(Label desc) {
        this.desc = desc;
    }

    public Label getJobID() {
        return jobID;
    }

    public void setJobID(Label jobID) {
        this.jobID = jobID;
    }

    public AnchorPane getRoot() {
        return root;
    }

    public Label getJobTotals() {
        return jobTotals;
    }

    public Label getJob1Total() {
        return job1Total;
    }

}
