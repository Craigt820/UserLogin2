package com.idi.userlogin.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.apache.commons.dbutils.DbUtils;
import com.idi.userlogin.JsonHandler;

import java.net.URL;
import java.sql.*;
import java.util.Optional;
import java.util.ResourceBundle;

import static com.idi.userlogin.Main.jsonHandler;

public class SpecsController implements Initializable {

    @FXML
    private Label close;

    @FXML
    private Label fileType;

    @FXML
    private Label dpi;

    @FXML
    private Label compress;

    @FXML
    private Label mode;

    @FXML
    private TextArea comments;

    public class Specs {
        private String type;
        private String field;
        private String comments;

        public Specs(String type, String field, String comments) {
            this.type = type;
            this.field = field;
            this.comments = comments;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getComments() {
            return comments;
        }

        public void setComments(String comments) {
            this.comments = comments;
        }
    }

    private ObservableList<Specs> getSpecs() {
        Connection connection = null;
        ResultSet set = null;
        PreparedStatement ps = null;
        ObservableList<Specs> specList = FXCollections.observableArrayList();

        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + JsonHandler.hostName + "/Tracking", JsonHandler.user, JsonHandler.pass);
            ps = connection.prepareStatement("SELECT a.id,a.comments, sf.name, st.name, p.job_id FROM tracking.job_specs a INNER JOIN projects p ON a.job_id=p.id INNER JOIN spec_types st ON a.type_id = st.id INNER JOIN spec_fields sf ON a.field_id = sf.id  WHERE p.job_id='" + jsonHandler.getSelJobID() + "'");
            set = ps.executeQuery();
            while (set.next()) {
                Specs specs = new Specs(set.getString("sf.name"), set.getString("st.name"), set.getString("a.comments"));
                specList.add(specs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(set);
            DbUtils.closeQuietly(ps);
            DbUtils.closeQuietly(connection);
        }
        return specList;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<Specs> specList = getSpecs();
        Optional<Specs> dpiSpec = specList.stream().filter(e -> e.getType().equals("DPI")).findFirst();
        dpiSpec.ifPresent(specs -> dpi.setText(specs.getField()));
        Optional<Specs> modeSpec = specList.stream().filter(e -> e.getType().equals("Mode")).findFirst();
        modeSpec.ifPresent(specs -> mode.setText(specs.getField()));
        Optional<Specs> compressSpec = specList.stream().filter(e -> e.getType().equals("Compression")).findFirst();
        compressSpec.ifPresent(specs -> compress.setText(specs.getField()));
        Optional<Specs> fileTypeSpec = specList.stream().filter(e -> e.getType().equals("FileType")).findFirst();
        fileTypeSpec.ifPresent(specs -> fileType.setText(specs.getField()));
        Optional<Specs> commentSpec = specList.stream().filter(e -> e.getType().equals("Comments")).findFirst();
        commentSpec.ifPresent(specs -> comments.setText(specs.getComments()));
//        specList.forEach(e -> {
//            System.out.println(e.getType() + " " + e.getField() + " " + e.getComments());
//        });
    }

    public Label getClose() {
        return close;
    }

}
