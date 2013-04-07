package eu.hansolo.fx;

import eu.hansolo.fxgtools.fxg.FxgVariable;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.HashMap;


/**
 * Created by
 * User: hansolo
 * Date: 07.04.13
 * Time: 14:19
 */
public class Test extends Application {
    private HashMap<String, FxgVariable> properties;
    private PropertiesPane               propertiesPane;

    @Override public void init() {
        FxgVariable var1 = new FxgVariable();
        var1.setType("boolean");
        var1.setName("on");
        var1.setDefaultValue("true");

        FxgVariable var2 = new FxgVariable();
        var2.setType("double");
        var2.setName("value");
        var2.setDefaultValue("13.0");

        properties = new HashMap<>();
        properties.put("on", var1);
        properties.put("value", var2);

        propertiesPane = new PropertiesPane();
        propertiesPane.setPropertiesMap(properties);
    }

    @Override public void start(Stage stage) {
        propertiesPane.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
