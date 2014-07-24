package gui.casestable;

import javafx.beans.property.*;
import util.ResourceControl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class CaseModel {
    public final static Map<String, String> FIELD_NAMES = new LinkedHashMap<>();

    public static void loadTitles() throws CaseFieldNamesMismatchException {
        ResourceBundle res = ResourceBundle.getBundle("properties.case_field_names", new ResourceControl("UTF-8"));

        Field[] fields = CaseModel.class.getDeclaredFields();

        for (Field field: fields) {
            String key = field.getName();

            if (!Modifier.isStatic(field.getModifiers())) {
                try {
                    String value = res.getString(key);
                    FIELD_NAMES.put(key, value);
                }
                catch (MissingResourceException e) {
                    throw new CaseFieldNamesMismatchException("No such property for field: " + key, e);
                }
            }
        }
    }

    public final IntegerProperty id;
    public final StringProperty createdDate;
    public final StringProperty plaintiff;
    public final StringProperty defendant;
    public final DoubleProperty cost;

    public CaseModel() {
        this(null, null, null, null, null);
    }

    public CaseModel(Integer id, String createdDate, String plaintiff, String defendant, Double cost) {
        this.id = new SimpleIntegerProperty(id);
        this.createdDate = new SimpleStringProperty(createdDate);
        
        this.plaintiff = new SimpleStringProperty(plaintiff);
        this.defendant = new SimpleStringProperty(defendant);
        
        this.cost = new SimpleDoubleProperty(cost);
    }
}
