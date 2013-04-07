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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

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
        "byte",
        "long",
        "String",
        "Color",
        "Object"
    };
    private GridPane               pane;
    private ObservableList<String> types;
    private ComboBox               typeSelection;
    private CheckBox               readOnly;
    private TextField              name;
    private TextField              defaultValue;
    private Button                 addButton;
    private ListView<FxgVariable>  propertiesList;


    public PropertiesPane() {
        pane           = new GridPane();
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
        pane.getStylesheets().add(getClass().getResource("fxgconverter.css").toExternalForm());
        pane.getStyleClass().add("converter");
    }

    private void initGraphics() {
        Label typeLabel = new Label("Type");
        GridPane.setHalignment(typeLabel, HPos.CENTER);
        //typeSelection.setEditable(true);
        typeSelection.setPrefWidth(90);
        typeSelection.getSelectionModel().select(0);
        pane.add(typeLabel, 0, 0);
        pane.add(typeSelection, 0, 1);

        Label readOnlyLabel = new Label("R/O");
        GridPane.setHalignment(readOnlyLabel, HPos.CENTER);
        pane.add(readOnlyLabel, 1, 0);
        pane.add(readOnly, 1, 1);

        Label nameLabel = new Label("Name");
        GridPane.setHalignment(nameLabel, HPos.CENTER);
        name.setAlignment(Pos.CENTER_RIGHT);
        pane.add(nameLabel, 2, 0);
        pane.add(name, 2, 1);

        Label defaultValueLabel = new Label("Default");
        GridPane.setHalignment(defaultValueLabel, HPos.CENTER);
        defaultValue.setAlignment(Pos.CENTER_RIGHT);
        defaultValue.setPrefWidth(80);
        pane.add(defaultValueLabel, 3, 0);
        pane.add(defaultValue, 3, 1);

        addButton.getStyleClass().add("add-button");
        addButton.setPrefSize(20, 20);
        pane.add(addButton, 4, 0);
        GridPane.setRowSpan(addButton, 2);

        propertiesList.setCellFactory(param -> {
            FxgVariableCell cell = new FxgVariableCell();
            cell.setAlignment(Pos.CENTER);
            cell.setOnDeleteEventFired(deleteEvent -> {
                propertiesList.getItems().remove(deleteEvent.getVariable());
            });
            return cell;
        });
        propertiesList.setPrefHeight(150);
        pane.add(propertiesList, 0, 2);
        GridPane.setColumnSpan(propertiesList, 5);

        pane.setHgap(10);
        pane.setVgap(5);
        pane.setPadding(new Insets(10, 10, 10, 10));

        setScene(new Scene(pane));
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
        } else if ("byte" == SELECTED_TYPE) {
            try {
                Byte.parseByte(DEFAULT);
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

        } else if ("Object" == SELECTED_TYPE) {

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
        private Pane        spacer;
        private Button      button;
        private FxgVariable variable;


        public FxgVariableCell() {
            super();
            pane         = new HBox();
            type         = new Label("(empty)");
            readOnly     = new CheckBox();
            name         = new Label();
            defaultValue = new Label();
            spacer       = new Pane();
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
