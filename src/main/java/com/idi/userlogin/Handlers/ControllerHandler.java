package com.idi.userlogin.Handlers;

import com.idi.userlogin.Controllers.*;
import com.idi.userlogin.JavaBeans.Group;
import com.idi.userlogin.JavaBeans.Item;
import com.idi.userlogin.Main;
import com.idi.userlogin.utils.Utils;
import com.itextpdf.text.pdf.PdfReader;
import com.jfoenix.controls.JFXTreeTableView;
import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.util.Duration;
import org.apache.commons.dbutils.DbUtils;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.textfield.CustomTextField;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import static com.idi.userlogin.Main.fxTrayIcon;
import static com.idi.userlogin.Main.jsonHandler;
import static com.idi.userlogin.utils.Utils.booleanToInt;

public abstract class ControllerHandler {

    public final static List<String> CONDITION_LIST = Arrays.asList("Best Scan Possible", "Bleed-Through", "Bounded", "Creases", "Cut-Off Text", "Faded/Discolored", "Holes/Sections Missing", "Ripped/Torn/Fragile", "Tight Gutters");
    public static Region opaqueOverlay = new Region();
    public static MainMenuController mainMenuController;
    public static JIBController jibController;
    public static UserEntryViewController entryController;
    public static ManifestViewController maniViewController;
    public static LoggedInController loggedInController;
    public static Group selGroup = null;
    public com.idi.userlogin.JavaBeans.Collection selColItem = null;
    public static PopOver mainMenuPop;
    public static SimpleIntegerProperty totalCountProp = new SimpleIntegerProperty(0); //For Total Count
    public static SimpleIntegerProperty groupCountProp = new SimpleIntegerProperty(0); //For Total Count
    public static JFXTreeTableView mainTree;
    public static final String addGroupLabel = "Add New Group";

    static {
        final FXMLLoader loader = new FXMLLoader(ControllerHandler.class.getResource("/fxml/LoggedInMenu_.fxml"));
        Parent settingsRoot = null;
        try {
            settingsRoot = (Parent) loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            Main.LOGGER.log(Level.SEVERE, "There was an error loading the 'LoggedInMenu' Scene!", e);
        }

        mainMenuPop = new PopOver(settingsRoot);
        mainMenuPop.setTitle("Main Menu");
        mainMenuPop.detachedProperty().addListener(e -> {
            opaqueOverlay.setVisible(!mainMenuPop.isDetached());
            opaquePOS();
        });
        mainMenuPop.showingProperty().addListener(e -> {
            opaqueOverlay.setVisible(mainMenuPop.isShowing());
            opaquePOS();
        });
    }

    public ControllerHandler() {

    }

    private static String getFolderStructure() {
        String scanPathStruct = null;
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet set = null;
        try {
            String sql = "SELECT folder_structure as structure FROM projects WHERE job_id='" + jsonHandler.getSelJobID() + "'";
            connection = ConnectionHandler.createDBConnection();
            ps = connection.prepareStatement(sql);
            set = ps.executeQuery(sql);

            if (set.next()) {
                scanPathStruct = set.getString("structure");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Main.LOGGER.log(Level.SEVERE, "There was an error getting the folder struct. from the db!", e);

        } finally {
            DbUtils.closeQuietly(connection);
            DbUtils.closeQuietly(ps);
            DbUtils.closeQuietly(set);
        }
        return scanPathStruct;
    }


    public static List<String> getColumns() throws SQLException {
        List<String> cols = new ArrayList<>();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet set = null;

        try {
            connection = ConnectionHandler.createDBConnection();
            String sql = "SELECT * FROM `" + jsonHandler.getSelJobID() + "` LIMIT 1";
            ps = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.TYPE_SCROLL_SENSITIVE);
            set = ps.executeQuery(sql);
            ResultSetMetaData metadata = set.getMetaData();
            int columnCount = metadata.getColumnCount();
            for (int i = 0; i < columnCount; i++) {
                String columnName = metadata.getColumnName(i + 1);
                cols.add(columnName);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Main.LOGGER.log(Level.SEVERE, "There was an error getting the columns from the db!", e);

        } finally {
            DbUtils.close(connection);
            DbUtils.close(ps);
            DbUtils.close(set);
        }
        return cols;
    }

    public static void createFoldersFromStruct(Item item) {
        Path newStructPath = item.getLocation();
        if (item.getType().getText().equals("Multi-Paged")) {
            newStructPath = newStructPath.getParent();
        }
        if (!newStructPath.toFile().exists()) {
            try {
                Files.createDirectories(newStructPath);
            } catch (IOException e) {
                fxTrayIcon.showErrorMessage("Error creating the folder structure! Please make sure your track path is valid!");
                e.printStackTrace();
            }
            fxTrayIcon.showInfoMessage(item.getLocation().toString() + " Created");
        }
    }

    public static String buildFolderStruct(int id, Item item) {
        final StringBuilder builder = new StringBuilder();
        try {
            String folderStruct = getFolderStructure();
            String[] fSplit = folderStruct.split("\\\\");
            List<String> cols = getColumns();
            for (String s : fSplit) {
                int idx = cols.indexOf(s);
                switch (s) {
                    case "group_id":
                        builder.append(item.getGroup().getName()).append("\\");
                        break;
                    case "collection_id":
                        builder.append(item.getGroup().getCollection().getName()).append("\\");
                        break;
                    default:
                        String colData = getDataByCol(id, cols.get(idx)).toString();
                        builder.append(colData).append("\\");
                        break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Main.LOGGER.log(Level.SEVERE, "There was an error building the folder structure!", e);

        }
        return builder.toString();
    }

    public static Object getDataByCol(int id, String colName) throws SQLException {
        Connection connection = null;
        ResultSet set = null;
        PreparedStatement ps = null;
        Object data = null;
        try {
            connection = ConnectionHandler.createDBConnection();
            ps = connection.prepareStatement("SELECT " + colName + " FROM `" + jsonHandler.getSelJobID() + "` WHERE id=? LIMIT 1");
            ps.setInt(1, id);
            set = ps.executeQuery();
            if (set.next()) {
                data = set.getString(colName);
            }


        } catch (SQLException e) {
            e.printStackTrace();
            Main.LOGGER.log(Level.SEVERE, "There was an error getting the data of a column from the db!", e);

        } finally {
            DbUtils.closeQuietly(set);
            DbUtils.closeQuietly(ps);
            DbUtils.closeQuietly(connection);

        }
        return data;
    }

    public static void fadeIn(Node node, Duration duration) {
        FadeTransition ft = new FadeTransition();
        ft.setNode(node);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.setRate(1.0);
        ft.setDuration(duration);
        SequentialTransition s = new SequentialTransition(ft);
        s.play();
    }

    public static void fadeOut(Node node, Duration duration) {
        FadeTransition ft = new FadeTransition();
        ft.setNode(node);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setRate(1.0);
        ft.setDuration(duration);
        SequentialTransition s = new SequentialTransition(ft);
        s.play();
    }

    public abstract int insertHelper(Item<? extends Item> item);

    public static Date formatDateTime(String dateTime) throws ParseException {
        if (!dateTime.isEmpty()) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date now = format.parse(dateTime);
            return now;
        }
        return null;
    }

    public static ImageView adjustFitSize(Image image) {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(22);
        imageView.setFitHeight(22);
        return imageView;
    }

    public abstract void updateTotal();

    public abstract ObservableList<? extends Item> getGroupItems(Group group);

    public static void updateItemProps(Item<? extends Item> item) {
        Connection connection = null;
        PreparedStatement ps = null;

        try {
            connection = ConnectionHandler.createDBConnection();
            ps = connection.prepareStatement("UPDATE `" + jsonHandler.getSelJobID() + "` SET conditions=?, comments=? WHERE id=?");
            ps.setString(1, item.getConditions().toString().replaceAll("[\\[|\\]]", ""));
            ps.setString(2, item.getComments());
            ps.setInt(3, item.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            Main.LOGGER.log(Level.SEVERE, "There was an error updating an items properties!", e);

        } finally {
            DbUtils.closeQuietly(ps);
            DbUtils.closeQuietly(connection);
        }
    }

    public abstract void groupSelectTask(Group nv, JFXTreeTableView<? extends Item> tree);

    public static void opaquePOS() {
        if (opaqueOverlay.isVisible()) {
//            opaqueOverlay.toFront();
            Pane root = (Pane) opaqueOverlay.getParent();
            opaqueOverlay.setMinSize(root.getWidth(), root.getHeight());
        } else {
//            opaqueOverlay.toBack();
            opaqueOverlay.setMinSize(0, 0);
        }
    }

    public static void addOpaqueLayer(Pane root) {
        opaqueOverlay.setStyle("-fx-background-color: #00000033;");
        opaqueOverlay.setOpacity(.8);
        opaqueOverlay.setVisible(false);
        if (!root.getChildren().contains(opaqueOverlay)) {
            Pane root2 = (Pane) root.getChildren().get(0);
            root2.getChildren().add(opaqueOverlay);
        }
        opaquePOS();
    }

    public static Map<Integer, Boolean> countHandler(Path path, String type) {
        boolean exists = false;
        int pages = 0;
        try {
            if (path != null) {
                if (type.equals("Multi-Paged")) {
                    File[] files = path.getParent().toFile().listFiles();
                    if (files != null) {
                        Optional<File> file = Arrays.stream(files).filter(e -> e.getName().contains(path.getFileName().toString())).findAny();
                        if (file.isPresent()) {
                            File file1 = file.get();
                            exists = true;
                            if (file1.renameTo(file1)) {
                                if (file1.toString().contains(".pdf")) {
                                    pages = countPDF(file.get());
                                } else if (file.get().toString().contains(".tif") || file.get().toString().contains(".tiff")) {
                                    pages = countTiff(file.get());
                                }
                            }
                        }
                    }
                } else {
                    if (path.toFile().listFiles() != null) {
                        exists = true;
                        pages = Objects.requireNonNull(path.toFile().listFiles()).length;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Main.LOGGER.log(Level.SEVERE, "There was an error with counting a file/folder!", e);
        }
        return Collections.singletonMap(pages, exists);
    }

    private static List<Image> createThumbnails(Item item) {
        List<Image> thumbs = new ArrayList<>();
        Image thumb = null;
        Path path = item.getLocation();
        List<Image> images = (List<Image>) item.getPreviews();

        try {
            if (path != null) {
                File[] files = path.toFile().listFiles();
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        if (files[i].toString().contains(".tif")) {
                            BufferedImage bi = null;
                            try {
                                bi = ImageIO.read(files[i]);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                            if (bi != null) {
                                java.awt.Image tmp = bi.getScaledInstance(160, 160, java.awt.Image.SCALE_SMOOTH);
                                BufferedImage dimg = new BufferedImage(160, 160, BufferedImage.TYPE_INT_ARGB);
                                Graphics2D g2d = dimg.createGraphics();
                                g2d.drawImage(tmp, 0, 0, null);
                                g2d.dispose();
                                thumb = SwingFXUtils.toFXImage(dimg, null);
                            }
                        } else {
                            try {
                                thumb = new Image(new URL("file:" + files[i].toString()).toExternalForm(), 160, 160, true, false);
                            } catch (MalformedURLException ex) {
                                ex.printStackTrace();
                            }
                        }
                        thumbs.add(thumb);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return thumbs;
    }

    public static JFXTreeTableView getMainTreeView() {
        JFXTreeTableView tree = null;
        if (entryController != null) {
            tree = entryController.getTree();
        } else if (jibController != null) {
            tree = jibController.getTree();
        }
        return tree;
    }

    public static void updateAll(ObservableList<? extends Item> items) {
        List<CompletableFuture> futures = new ArrayList<>();
        items.forEach(e2 -> {
            CompletableFuture future = CompletableFuture.supplyAsync(() -> {
                Map<Integer, Boolean> count = countHandler(e2.getLocation(), e2.getType().getText());
                e2.totalProperty().set((Integer) count.keySet().toArray()[0]);
                e2.existsProperty().set((Boolean) count.values().toArray()[0]);
                return e2;
            }).thenAcceptAsync(item1 -> {
                updateItemDB(item1);
            });
            futures.add(future);
        });

        CompletableFuture[] compFutures = futures.stream().filter(Objects::nonNull).toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(compFutures).join();
    }

    public abstract void resetFields();

    public static void updateSelected(Item item) {
        CompletableFuture.supplyAsync(() -> {
            Map<Integer, Boolean> pages = countHandler(item.getLocation(), item.type.getText());
            item.totalProperty().set((Integer) pages.keySet().toArray()[0]);
            item.existsProperty().set((Boolean) pages.values().toArray()[0]);
            return item;
        }).thenRunAsync(() -> {
            updateItemDB(item);
        }).join();
    }

    public static void updateGroup(boolean completed) {
    }

    public static void updateItemDB(final Item item, String sql) {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = ConnectionHandler.createDBConnection();
            ps = connection.prepareStatement("Update `" + jsonHandler.getSelJobID() + "` SET total=?, completed=?, completed_On=? WHERE id=?");
            ps.setInt(1, item.getTotal());
            ps.setInt(2, booleanToInt(item.getCompleted().isSelected()));
            if (item.getCompleted().isSelected()) {
                Date now = formatDateTime(item.getCompleted_On().replace(" ", "T"));
                if (now == null) {
                    ps.setString(3, null);
                } else {
                    ps.setString(3, new Timestamp(now.toInstant().toEpochMilli()).toString());
                }
            } else {
                ps.setString(3, null);
            }
            ps.setInt(4, item.getId());
            ps.executeUpdate();
            ps.executeUpdate(sql);
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
            Main.LOGGER.log(Level.SEVERE, "There was an error updating an item!", e);

        } finally {
            DbUtils.closeQuietly(ps);
            DbUtils.closeQuietly(connection);
        }
    }

    public static void updateItemDB(final Item item) {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = ConnectionHandler.createDBConnection();
            ps = connection.prepareStatement("Update `" + jsonHandler.getSelJobID() + "` SET total=?, completed=?, completed_On=?,overridden=? WHERE id=?");
            ps.setInt(1, item.getTotal());
            ps.setInt(2, booleanToInt(item.getCompleted().isSelected()));
            if (item.getCompleted_On() != null && item.getCompleted().isSelected()) {
                Date now = formatDateTime(item.getCompleted_On().replace(" ", "T"));
                ps.setString(3, new Timestamp(now.toInstant().toEpochMilli()).toString());
            } else {
                ps.setString(3, null);
            }
            ps.setInt(4, Utils.booleanToInt(item.isOverridden()));
            ps.setInt(5, item.getId());
            ps.executeUpdate();

        } catch (SQLException | ParseException e) {
            e.printStackTrace();
            Main.LOGGER.log(Level.SEVERE, "There was an error updating an item!", e);

        } finally {
            DbUtils.closeQuietly(ps);
            DbUtils.closeQuietly(connection);
        }
    }

    public static int countPDF(File path) {
        int numPages = 0;
        try {
            if (path.renameTo(path) && path.toString().toLowerCase().contains(".pdf")) {
                PdfReader r = new PdfReader(path.toString());
                numPages = r.getNumberOfPages();
                r.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
            Main.LOGGER.log(Level.SEVERE, "There was an error reading a pdf file!", e);
        }

        return numPages;
    }

    public static int countTiff(File file) throws IOException {
        int num = 0;
        ImageInputStream image = null;
        Iterator<ImageReader> reader = null;
        ImageReader read = null;
        File tiff = null;
        try {

            if (!file.toString().toLowerCase().contains(".tif")) {
                tiff = new File(file + ".tif");
            } else {
                tiff = file;
            }
            if (tiff != null) {
                image = ImageIO.createImageInputStream(tiff);
                reader = ImageIO.getImageReaders(image);
                read = reader.next();
                read.setInput(image);
                num += read.getNumImages(true);
            }
        } catch (IOException e) {
            if (read != null) {
                read.dispose();
            }
            if (image != null) {
                image.close();
            }
            e.printStackTrace();
            Main.LOGGER.log(Level.SEVERE, "There was an error reading a tiff file!", e);

        } finally {
            if (read != null) {
                read.dispose();
            }
            if (image != null) {
                image.close();
            }
        }
        return num;
    }

    public static void sceneTransition(Pane root, URL fxml, boolean overlay) {
        try {
            root.getChildren().setAll((Pane) FXMLLoader.load(fxml));
            if (overlay) {
                addOpaqueLayer(root);
            }

            mainTree = getMainTreeView();

        } catch (IOException e) {
            e.printStackTrace();
            Main.LOGGER.log(Level.SEVERE, "There was an error loading a scene!", e);

        }
        fadeOut(root, Duration.seconds(.5));
        fadeIn(root, Duration.seconds(.5));
    }

    public abstract void legalTextTest(boolean isLegal, CustomTextField node);

    public static Region getOpaqueOverlay() {
        return opaqueOverlay;
    }

    public static PopOver getMainMenuPop() {
        return mainMenuPop;
    }

    public static void setMainMenuPop(PopOver mainMenuPop) {
        ControllerHandler.mainMenuPop = mainMenuPop;
    }
}

