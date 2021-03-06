package eu.hansolo.fx;

import eu.hansolo.fxgtools.fxg.FxgVariable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by
 * User: hansolo
 * Date: 07.04.13
 * Time: 13:44
 */
public class PropertiesPane extends Stage {
    private static final String[] TYPES = {
        "boolean",
        "double",
        "float",
        "int",
        "long",
        "String",
        "Color",
        "Object"
    };
    private double                 dragX;
    private double                 dragY;
    private GridPane grid;
    private ObservableList<String> types;
    private ComboBox               typeSelection;
    private CheckBox               readOnly;
    private TextField              name;
    private TextField              defaultValue;
    private Button                 addButton;
    private ListView<FxgVariable>  propertiesList;


    public PropertiesPane() {
        grid = new GridPane();
        types          = FXCollections.observableArrayList(TYPES);
        typeSelection  = new ComboBox(types);
        readOnly       = new CheckBox();
        name           = new TextField();
        defaultValue   = new TextField();
        addButton      = new Button();
        propertiesList = new ListView(FXCollections.observableArrayList(new ArrayList<FxgVariable>()));
        init();
        initGraphics();
        registerListeners();
    }

    private void init() {
        grid.getStylesheets().add(getClass().getResource("fxgconverter.css").toExternalForm());
        grid.getStyleClass().add("converter");
    }

    private void initGraphics() {
        Label typeLabel = new Label("Type");
        GridPane.setHalignment(typeLabel, HPos.CENTER);
        //typeSelection.setEditable(true);
        typeSelection.setPrefWidth(90);
        typeSelection.getSelectionModel().select(0);
        grid.add(typeLabel, 0, 0);
        grid.add(typeSelection, 0, 1);

        Label readOnlyLabel = new Label("R/O");
        GridPane.setHalignment(readOnlyLabel, HPos.CENTER);
        grid.add(readOnlyLabel, 1, 0);
        grid.add(readOnly, 1, 1);

        Label nameLabel = new Label("Name");
        GridPane.setHalignment(nameLabel, HPos.CENTER);
        name.setAlignment(Pos.CENTER_RIGHT);
        grid.add(nameLabel, 2, 0);
        grid.add(name, 2, 1);

        Label defaultValueLabel = new Label("Default");
        GridPane.setHalignment(defaultValueLabel, HPos.CENTER);
        defaultValue.setAlignment(Pos.CENTER_RIGHT);
        defaultValue.setPrefWidth(80);
        grid.add(defaultValueLabel, 3, 0);
        grid.add(defaultValue, 3, 1);

        addButton.getStyleClass().add("add-button");
        addButton.setPrefSize(20, 20);
        grid.add(addButton, 4, 0);
        GridPane.setRowSpan(addButton, 2);

        propertiesList.setCellFactory((ListView<FxgVariable> param) -> {
            FxgVariableCell cell = new FxgVariableCell();
            cell.setAlignment(Pos.CENTER);
            cell.setOnDeleteEventFired(deleteEvent -> {
                propertiesList.getItems().remove(deleteEvent.getVariable());
            });
            return cell;
        });
        propertiesList.setPrefHeight(150);
        grid.add(propertiesList, 0, 2);
        GridPane.setColumnSpan(propertiesList, 5);

        grid.setHgap(10);
        grid.setVgap(5);
        grid.setPadding(new Insets(10, 10, 10, 10));

        // Window Header
        Label headerLabel = new Label("Properties");
        headerLabel.setPrefWidth(190);
        headerLabel.setPrefHeight(22);
        headerLabel.setAlignment(Pos.CENTER);
        headerLabel.setFont(Font.font("Arial", FontWeight.NORMAL, FontPosture.REGULAR, 12));
        headerLabel.setTextFill(Color.WHITE);

        Region closeIcon = new Region();
        closeIcon.getStyleClass().add("close");
        closeIcon.setOnMousePressed(mouseEvent -> { close(); });
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
                dragX = getX() - mouseEvent.getScreenX();
                dragY = getY() - mouseEvent.getScreenY();
            }
        });
        windowHeader.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent mouseEvent) {
                setX(mouseEvent.getScreenX() + dragX);
                setY(mouseEvent.getScreenY() + dragY);
            }
        });
        windowHeader.getChildren().addAll(headerLabel, closeIcon);

        Background bkg = new Background(new BackgroundFill(Color.rgb(41, 32, 32), new CornerRadii(5), Insets.EMPTY));
        VBox pane = new VBox();
        pane.setBackground(bkg);
        pane.getChildren().addAll(windowHeader, grid);

        Scene scene = new Scene(pane, null);
        scene.getStylesheets().add(getClass().getResource("fxgconverter.css").toExternalForm());
        setScene(scene);

        initStyle(StageStyle.TRANSPARENT);
        setResizable(false);

    }

    private void registerListeners() {
        addButton.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent actionEvent) {
                if (isDefaultValid()) {
                    System.out.println("Add a new row to the properties list");
                    System.out.println(typeSelection.getSelectionModel().getSelectedItem().toString() + " " + name.getText() + " = " + defaultValue.getText());
                    FxgVariable property = new FxgVariable();
                    property.setType(typeSelection.getSelectionModel().getSelectedItem().toString());
                    property.setName(name.getText());
                    property.setDefaultValue(defaultValue.getText());
                    property.setReadOnly(readOnly.isSelected());
                    if (!isInList(property)) {
                        propertiesList.getItems().add(property);
                    }
                } else {
                    System.out.println("Default doesn't fit to selected type");
                }
            }
        });

    }

    private boolean isDefaultValid() {
        final String DEFAULT       = defaultValue.getText().trim();
        final String SELECTED_TYPE = typeSelection.getSelectionModel().getSelectedItem().toString();
        if ("boolean" == SELECTED_TYPE) {
            if (DEFAULT.toLowerCase().equals("true")) {
                return true;
            } else if (DEFAULT.toLowerCase().equals("false")) {
                return true;
            }

        } else if ("double" == SELECTED_TYPE) {
            try {
                Double.parseDouble(DEFAULT);
                return true;
            } catch (NumberFormatException exception) {
                return false;
            }
        } else if ("float" == SELECTED_TYPE) {
            try {
                Float.parseFloat(DEFAULT);
                return true;
            } catch (NumberFormatException exception) {
                return false;
            }
        } else if ("int" == SELECTED_TYPE) {
            try {
                Integer.parseInt(DEFAULT);
                return true;
            } catch (NumberFormatException exception) {
                return false;
            }
        } else if ("long" == SELECTED_TYPE) {
            try {
                Long.parseLong(DEFAULT);
                return true;
            } catch (NumberFormatException exception) {
                return false;
            }
        } else if ("String" == SELECTED_TYPE) {
            return true;
        } else if ("Color" == SELECTED_TYPE) {
            return true;
        } else if ("Object" == SELECTED_TYPE) {
            return true;
        }
        return false;
    }

    private boolean isInList(final FxgVariable VARIABLE) {
        boolean retValue = false;
        for (FxgVariable variable : propertiesList.getItems()) {
            if (variable.getName().equals(VARIABLE.getName())) {
                retValue = true;
            } else {
                return false;
            }
        }
        return retValue;
    }

    public HashMap<String, FxgVariable> getPropertiesMap() {
        HashMap<String, FxgVariable> properties = new HashMap<>(propertiesList.getItems().size());
        for (FxgVariable variable : propertiesList.getItems()) {
            properties.put(variable.getName().toLowerCase(), variable);
        }
        return properties;
    }
    public void setPropertiesMap(final HashMap<String, FxgVariable> PROPERTIES) {
        propertiesList.getItems().setAll(PROPERTIES.values());
    }

    // ******************** Inner Classes *************************************
    private static class FxgVariableCell extends ListCell<FxgVariable> {
        private HBox        pane;
        private Label       type;
        private CheckBox    readOnly;
        private Label       name;
        private Label       defaultValue;
        private Region      spacer;
        private Button      button;
        private FxgVariable variable;


        public FxgVariableCell() {
            super();
            pane         = new HBox();
            type         = new Label("(empty)");
            readOnly     = new CheckBox();
            name         = new Label();
            defaultValue = new Label();
            spacer       = new Region();
            button       = new Button();
            init();
            initGraphics();
            registerListeners();
        }

        private void init() {

        }

        private void initGraphics() {
            type.setPrefWidth(80);
            name.setPrefWidth(150);
            defaultValue.setPrefWidth(80);
            readOnly.setDisable(true);
            button.getStyleClass().add("delete-button");
            pane.getChildren().addAll(type, readOnly, name, defaultValue, spacer, button);
            HBox.setHgrow(spacer, Priority.ALWAYS);
            pane.setAlignment(Pos.CENTER);
            pane.setSpacing(5);
        }

        private void registerListeners() {
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent event) {
                    fireDeleteEvent(new DeleteEvent(variable, this, null, DeleteEvent.DELETE_ROW));
                }
            });
        }

        @Override protected void updateItem(final FxgVariable VARIABLE, final boolean EMPTY) {
            super.updateItem(VARIABLE, EMPTY);
            setText(null);  // No text in type of super class
            if (EMPTY) {
                variable = null;
                setGraphic(null);
            } else {
                variable = VARIABLE;
                type.setText(VARIABLE != null ? VARIABLE.getType() : "-");
                readOnly.setSelected(VARIABLE != null ? VARIABLE.isReadOnly() : false);
                name.setText(VARIABLE != null ? VARIABLE.getName() : "-");
                defaultValue.setText(VARIABLE != null ? VARIABLE.getDefaultValue() : "-");
                setGraphic(pane);
            }
        }


        // ******************** Event Handling ********************************
        public final ObjectProperty<EventHandler<DeleteEvent>> onDeleteEventFiredProperty() { return onDeleteEventFired; }
        public final void setOnDeleteEventFired(EventHandler<DeleteEvent> value) { onDeleteEventFiredProperty().set(value); }
        public final EventHandler<DeleteEvent> getOnDeleteEventFired() { return onDeleteEventFiredProperty().get(); }
        private ObjectProperty<EventHandler<DeleteEvent>> onDeleteEventFired = new ObjectPropertyBase<EventHandler<DeleteEvent>>() {
            @Override public Object getBean() { return this; }
            @Override public String getName() { return "onDeleteEventFired";}
        };
        public void fireDeleteEvent(final DeleteEvent EVENT) {
            final EventHandler<DeleteEvent> HANDLER;
            HANDLER = getOnDeleteEventFired();
            if (HANDLER != null) {
                HANDLER.handle(EVENT);
            }
        }
    }

    private static class DeleteEvent extends Event {
        public static final EventType<DeleteEvent> DELETE_ROW  = new EventType(ANY, "deleteRow");
        private FxgVariable variable;


        // ******************* Constructors ***************************************
        public DeleteEvent(final FxgVariable VARIABLE, final Object SOURCE, final EventTarget TARGET, final EventType<DeleteEvent> EVENT_TYPE) {
            super(SOURCE, TARGET, EVENT_TYPE);
            variable = VARIABLE;
        }


        // ******************* Methods ********************************************
        public FxgVariable getVariable() {
            return variable;
        }
    }
}
