package eu.hansolo.fx;

import eu.hansolo.fxgtools.fxg.FxgElement;
import eu.hansolo.fxgtools.fxg.Language;
import eu.hansolo.fxgtools.main.FxgLiveParser;
import eu.hansolo.fxgtools.main.FxgParser;
import eu.hansolo.fxgtools.main.FxgTranslator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by
 * User: hansolo
 * Date: 04.03.13
 * Time: 14:49
 */
public class Main extends Application {
    private static final double    PREVIEW_SIZE = 430;
    private double                 dragX;
    private double                 dragY;
    private HashMap<String, Node>  convertedGroups;
    private TextField              packageInfo;
    private RadioButton            optionJavaFX;
    private RadioButton            optionCanvas;
    private CheckBox               extendRegion;
    private ListView<CheckBox>     layerListView;
    private Rectangle              transpDropBackground;
    private Shape                  dropZoneSymbol;
    private StackPane              dropPane;
    private String                 fxgFileName;
    private Dimension2D            originalSize;
    private InvalidationListener   sizeListener;
    private Watcher                watcher;
    private Thread                 dirWatcherThread;
    private File                   fxgFileToWatch;
    private PropertiesPane         propertiesPane;


    // ******************** Initialization ************************************
    @Override public void init() {
        convertedGroups = new LinkedHashMap<>();
        layerListView   = new ListView<>();
        layerListView.setPrefWidth(150);
        layerListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        transpDropBackground = new Rectangle(PREVIEW_SIZE, PREVIEW_SIZE);
        transpDropBackground.setFill(new ImagePattern(new Image(getClass().getResource("opacitypattern.png").toExternalForm()), 0, 0, 20, 20, false));

        dropZoneSymbol = createDropZone(PREVIEW_SIZE);

        registerListeners();
    }

    private void registerListeners() {
        sizeListener = observable -> {
            double size = dropPane.getWidth() < dropPane.getHeight() ? dropPane.getWidth() : dropPane.getHeight();
            dropPane.setPrefSize(size, size);
            dropZoneSymbol.setScaleX(size / 132 * 0.8);
            dropZoneSymbol.setScaleY(size / 132 * 0.8);
        };
    }


    // ******************** Private methods ***********************************
    private Shape createDropZone(final double SIZE) {
        final SVGPath DROP_ZONE = new SVGPath();
        DROP_ZONE.setContent("M 60 0 L 30 0 L 30 6 L 60 6 L 60 0 ZM 102 0 L 72 0 L 72 6 " +
                             "L 102 6 L 102 0 ZM 132 18 L 132 16.5 C 132 5.6616 126.3385 " +
                             "0 115.5 0 L 114 0 L 114 6 L 115 6 C 123.0623 6 126 8.9377 " +
                             "126 17 L 126 18 L 132 18 ZM 132 60 L 132 30 L 126 30 L 126 " +
                             "60 L 132 60 ZM 132 102 L 132 72 L 126 72 L 126 102 L 132 " +
                             "102 ZM 114 132 L 115.5 132 C 126.3385 132 132 126.3385 132 " +
                             "115.5 L 132 114 L 126 114 L 126 115 C 126 123.0623 " +
                             "123.0623 126 115 126 L 114 126 L 114 132 ZM 72 132 " +
                             "L 102 132 L 102 126 L 72 126 L 72 132 ZM 30 132 L 60 132 " +
                             "L 60 126 L 30 126 L 30 132 ZM 0 114 L 0 115.5 C 0 126.3385 " +
                             "5.6616 132 16.5 132 L 18 132 L 18 126 L 17 126 C 8.9377 " +
                             "126 6 123.0623 6 115 L 6 114 L 0 114 ZM 0 72 L 0 102 " +
                             "L 6 102 L 6 72 L 0 72 ZM 0 30 L 0 60 L 6 60 L 6 30 L 0 30 Z" +
                             "M 0 16.5 L 0 18 L 6 18 L 6 17 C 6 8.9377 8.9377 6 17 6 " +
                             "L 18 6 L 18 0 L 16.5 0 C 5.6616 0 0 5.6616 0 16.5 Z" +
                             "M 80 37.3333 L 50.5 37.3333 L 50.5 66.8333 L 50.5 68.8333 " +
                             "L 32.5 68.8333 L 65.25 95.5 L 98 68.8333 L 80 68.8333 " +
                             "L 80 37.3333 Z");
        DROP_ZONE.setFill(Color.web("#918c8f"));
        DROP_ZONE.setStroke(null);
        DROP_ZONE.setScaleX(SIZE / 132 * 0.8);
        DROP_ZONE.setScaleY(SIZE / 132 * 0.8);
        return DROP_ZONE;
    }

    private void initDragAndDrop(final Parent DROP_ZONE) {
        DROP_ZONE.setOnDragOver(new EventHandler<DragEvent>() {
            @Override public void handle(DragEvent dragEvent) {
                Dragboard dragboard = dragEvent.getDragboard();
                if (dragboard.hasFiles() || dragboard.hasUrl()) {
                    dragEvent.acceptTransferModes(TransferMode.ANY);
                }
                dragEvent.consume();
            }
        });
        DROP_ZONE.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override public void handle(DragEvent dragEvent) {
                Dragboard dragboard = dragEvent.getDragboard();
                String url = null;
                URI uri = null;
                if (dragboard.hasFiles()) {
                    uri = dragboard.getFiles().get(0).toURI();
                } else if (dragboard.hasUrl()) {
                    url = dragboard.getUrl();
                }
                // do the conversion if a files was dropped
                if (uri != null) {
                    try {
                        final String FULL_PATH = URLDecoder.decode(uri.getPath(), "UTF-8");
                        fxgFileName = FULL_PATH;

                        // Attach file watcher
                        if (null != dirWatcherThread && dirWatcherThread.isAlive()) dirWatcherThread.interrupt();
                        fxgFileToWatch = new File(fxgFileName);
                        watcher = new Watcher(fxgFileToWatch);
                        watcher.setOnFileModified(new EventHandler<WatcherEvent>() {
                            @Override public void handle(WatcherEvent watcherEvent) {
                                // refresh preview image
                                Platform.runLater(new Runnable() {
                                    @Override public void run() {
                                        createPreview(FULL_PATH);
                                    }
                                });
                            }
                        });
                        watcher.setOnFileRemoved(new EventHandler<WatcherEvent>() {
                            @Override public void handle(WatcherEvent watcherEvent) {
                                dirWatcherThread.interrupt();
                            }
                        });
                        dirWatcherThread = new Thread(watcher);
                        dirWatcherThread.start();

                        // Create the preview image
                        createPreview(FULL_PATH);
                        //convert();
                    } catch (UnsupportedEncodingException e) {

                    }
                }
                if (url != null) {
                    System.out.println("URL: " + url);
                }
                dragEvent.setDropCompleted(url != null);
                dragEvent.consume();
            }
        });
    }

    private void createPreview(final String FILE_NAME) {
        convertedGroups.clear();
        convertedGroups.put("", transpDropBackground);
        FxgLiveParser liveParser = new FxgLiveParser();
        convertedGroups.putAll(liveParser.parse(FILE_NAME, dropPane.getWidth(), dropPane.getHeight(), true));
        propertiesPane.setPropertiesMap(liveParser.getControlProperties());
        System.out.println(liveParser.getControlProperties().values());
        if (convertedGroups.isEmpty()) {
            dropPane.getChildren().setAll(dropZoneSymbol);
        } else {
            dropPane.getChildren().setAll(convertedGroups.values());
            layerListView.getItems().clear();
            for (String key : convertedGroups.keySet()) {
                if (key.toLowerCase().startsWith("properties") || key.isEmpty()) continue;
                CheckBox checkBox = new CheckBox(key);
                checkBox.setSelected(true);
                checkBox.setOnAction(event -> { updatePreview((CheckBox) event.getSource()); });
                layerListView.getItems().add(checkBox);
            }
            originalSize = liveParser.getDimension(FILE_NAME);
        }
    }

    private void updatePreview(final CheckBox CHECKBOX) {
        convertedGroups.get(CHECKBOX.getText()).setVisible(CHECKBOX.isSelected());
    }

    private void convert() {
        final String FILE_NAME = fxgFileName.substring(fxgFileName.lastIndexOf(System.getProperty("file.separator")) + 1);
        FxgParser parser = new FxgParser();
        FxgTranslator translator = new FxgTranslator();
        Map<String, List<FxgElement>> layers = parser.getElements(fxgFileName);
        for (CheckBox layer : layerListView.getItems()) {
            if (layers.containsKey(layer.getText())) {
                if (!layer.isSelected()) {
                    layers.remove(layer.getText());
                }
            }
        }
        translator.setPackageInfo(packageInfo.getText());
        translator.translate(new StringBuilder(System.getProperties().getProperty("user.home")).append(File.separator).append("Desktop").append(File.separator).toString(), FILE_NAME, layers, optionJavaFX.isSelected() ? Language.JAVAFX : Language.CANVAS, Double.toString(originalSize.getWidth()), Double.toString(originalSize.getHeight()), true, "", propertiesPane.getPropertiesMap(), extendRegion.isSelected());
    }

    private void initOnFxThread() {
        propertiesPane = new PropertiesPane();
    }


    // ******************** App start/stop ************************************
    @Override public void start(Stage stage) {
        initOnFxThread();

        dropPane = new StackPane();
        dropPane.setPadding(new Insets(10, 10, 10, 10));
        dropPane.setPrefSize(PREVIEW_SIZE, PREVIEW_SIZE);
        dropPane.setMinSize(PREVIEW_SIZE, PREVIEW_SIZE);
        dropPane.setMaxSize(PREVIEW_SIZE, PREVIEW_SIZE);
        dropPane.widthProperty().addListener(sizeListener);
        dropPane.heightProperty().addListener(sizeListener);
        dropPane.getChildren().add(dropZoneSymbol);
        /*
        dropPane.setBorder(BorderBuilder.create()
                                        .strokes(BorderStrokeBuilder.create()
                                                                    .bottomStroke(Color.web("#918c8f"))
                                                                    .bottomStyle(BorderStrokeStyle.SOLID)
                                                                    .rightStroke(Color.web("#918c8f"))
                                                                    .rightStyle(BorderStrokeStyle.SOLID)
                                                                    .build())
                                        .build());
                                        */
        initDragAndDrop(dropPane);

        VBox optionPane = new VBox();
        optionPane.setSpacing(10);
        optionPane.setPadding(new Insets(10, 10, 10, 10));
        packageInfo  = new TextField();
        packageInfo.setPromptText("eu.hansolo.fx");
        ToggleGroup optionGroup = new ToggleGroup();
        optionJavaFX = new RadioButton("JavaFX");
        optionJavaFX.setSelected(true);
        optionJavaFX.setToggleGroup(optionGroup);
        optionCanvas = new RadioButton("Canvas");
        optionCanvas.setToggleGroup(optionGroup);

        extendRegion = new CheckBox("extend Region");

        ToggleButton propertiesButton = new ToggleButton("Properties");
        propertiesButton.setFont(Font.font("Arial", FontWeight.BOLD, FontPosture.REGULAR, 13));
        propertiesButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent actionEvent) {
                if (propertiesButton.isSelected()) {
                    propertiesPane.show();
                } else {
                    propertiesPane.hide();
                }
            }
        });
        propertiesPane.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override public void handle(WindowEvent windowEvent) {
                propertiesButton.setSelected(false);
            }
        });

        Button drawingCodeButton = new Button("Drawing Code");
        drawingCodeButton.setFont(Font.font("Arial", FontWeight.BOLD, FontPosture.REGULAR, 13));
        drawingCodeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent actionEvent) {
                final String FILE_NAME = fxgFileName.substring(fxgFileName.lastIndexOf(System.getProperty("file.separator")) + 1);
                FxgParser     parser     = new FxgParser();
                FxgTranslator translator = new FxgTranslator();
                Map<String, List<FxgElement>> layers = parser.getElements(fxgFileName);
                for (CheckBox layer : layerListView.getItems()) {
                    if (layers.containsKey(layer.getText())) {
                        if (!layer.isSelected()) {
                            layers.remove(layer.getText());
                        }
                    }
                }
                translator.translate(new StringBuilder(System.getProperties().getProperty("user.home")).append(File.separator).append("Desktop").append(File.separator).toString(), FILE_NAME, layers, Language.JAVAFX, Double.toString(originalSize.getWidth()), Double.toString(originalSize.getHeight()), false, "", propertiesPane.getPropertiesMap(), false);
                String content = translator.getJavaFxDrawingCode(layers);
                Path path = FileSystems.getDefault().getPath(new StringBuilder(System.getProperties().getProperty("user.home")).append(File.separator).append("Desktop").toString(), "jfxDrawingCode.java");
                try {
                    Files.write(path, content.getBytes(), StandardOpenOption.CREATE);
                }
                catch (IOException exception) {
                    System.out.println("Error writing jfxDrawingCode.java");
                }
            }
        });

        optionPane.getChildren().addAll(packageInfo, optionJavaFX, optionCanvas, extendRegion, drawingCodeButton, propertiesButton);

        GridPane grid = new GridPane();
        grid.add(dropPane, 0, 0);
        grid.add(optionPane, 1, 0);
        grid.add(layerListView, 2, 0);
        GridPane.setHgrow(dropPane, Priority.ALWAYS);
        GridPane.setVgrow(dropPane, Priority.ALWAYS);
        GridPane.setHgrow(layerListView, Priority.SOMETIMES);
        GridPane.setVgrow(layerListView, Priority.ALWAYS);

        Button exitButton    = new Button("Exit");
        exitButton.setFont(Font.font("Arial", FontWeight.BOLD, FontPosture.REGULAR, 13));
        exitButton.setOnAction(actionEvent -> { Platform.exit(); });

        Button convertButton = new Button("Convert");
        convertButton.setFont(Font.font("Arial", FontWeight.BOLD, FontPosture.REGULAR, 13));
        convertButton.setOnAction(actionEvent -> { convert(); });

        HBox buttonPane = new HBox();
        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        buttonPane.setAlignment(Pos.CENTER_RIGHT);
        buttonPane.setSpacing(30);
        buttonPane.setPadding(new Insets(5, 5, 5, 5));
        buttonPane.getChildren().addAll(convertButton, exitButton);

        GridPane.setColumnSpan(buttonPane, 3);
        grid.add(buttonPane, 0, 1);

        // Window Header
        Label headerLabel = new Label("FXG Converter 8");
        headerLabel.setPrefWidth(190);
        headerLabel.setPrefHeight(22);
        headerLabel.setAlignment(Pos.CENTER);
        headerLabel.setFont(Font.font("Arial", FontWeight.NORMAL, FontPosture.REGULAR, 12));
        headerLabel.setTextFill(Color.WHITE);

        Region closeIcon = new Region();
        closeIcon.getStyleClass().add("close");
        closeIcon.setOnMousePressed(mouseEvent -> { Platform.exit(); });
        closeIcon.setMinWidth(16);
        closeIcon.setMinHeight(16);
        closeIcon.setPrefWidth(16);
        closeIcon.setPrefHeight(16);
        closeIcon.setPickOnBounds(true);
        closeIcon.setLayoutX(5);
        closeIcon.setLayoutY(3);

        Pane windowHeader = new Pane();
        windowHeader.getStyleClass().add("header");
        windowHeader.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent mouseEvent) {
                dragX = stage.getX() - mouseEvent.getScreenX();
                dragY = stage.getY() - mouseEvent.getScreenY();
            }
        });
        windowHeader.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent mouseEvent) {
                stage.setX(mouseEvent.getScreenX() + dragX);
                stage.setY(mouseEvent.getScreenY() + dragY);
            }
        });
        windowHeader.getChildren().addAll(headerLabel, closeIcon);

        Background bkg = new Background(new BackgroundFill(Color.rgb(41, 32, 32), new CornerRadii(5), Insets.EMPTY));
        VBox pane = new VBox();
        pane.setBackground(bkg);
        pane.getChildren().addAll(windowHeader, grid);

        Scene scene = new Scene(pane, null);
        scene.getStylesheets().add(getClass().getResource("fxgconverter.css").toExternalForm());

        stage.setScene(scene);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setResizable(false);
        stage.show();
    }

    @Override public void stop() {
        if (null != dirWatcherThread && dirWatcherThread.isAlive()) dirWatcherThread.interrupt();
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
