package com.idi.userlogin.Controllers;

import com.idi.userlogin.Handlers.ConnectionHandler;
import com.idi.userlogin.Handlers.JsonHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.apache.commons.dbutils.DbUtils;

import java.net.URL;
import java.sql.*;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class SpecsController implements Initializable {

    @FXML
    public Label close;

    @FXML
    public Label fileType;

    @FXML
    public Label dpi;

    @FXML
    public Label compress;

    @FXML
    public Label mode;

    @FXML
    public TextArea comments;

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

    public ObservableList<Specs> getSpecs() {
        Connection connection = null;
        ResultSet set = null;
        PreparedStatement ps = null;
        ObservableList<Specs> specList = FXCollections.observableArrayList();

        try {
            connection = ConnectionHandler.createDBConnection();
            ps = connection.prepareStatement("SELECT a.id,a.comments, sf.name, st.name, p.job_id FROM tracking.job_specs a INNER JOIN projects p ON a.job_id=p.id INNER JOIN spec_types st ON a.type_id = st.id INNER JOIN spec_fields sf ON a.field_id = sf.id  WHERE p.job_id='" + JsonHandler.getSelJob().getJob_id() + "'");
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
        String fileTypes = specList.stream().filter(e -> e.getType().equals("FileType")).map(e -> e.getField()).collect(Collectors.joining("/"));
        fileType.setText(fileTypes);
        Optional<Specs> commentSpec = specList.stream().filter(e -> e.getType().equals("Comments")).findFirst();
        commentSpec.ifPresent(specs -> comments.setText(specs.getComments()));
    }

    public Label getClose() {
        return close;
    }

}
