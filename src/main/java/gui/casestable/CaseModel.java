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


    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public String getCreatedDate() {
        return createdDate.get();
    }

    public StringProperty createdDateProperty() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate.set(createdDate);
    }

    public String getPlaintiff() {
        return plaintiff.get();
    }

    public StringProperty plaintiffProperty() {
        return plaintiff;
    }

    public void setPlaintiff(String plaintiff) {
        this.plaintiff.set(plaintiff);
    }

    public String getDefendant() {
        return defendant.get();
    }

    public StringProperty defendantProperty() {
        return defendant;
    }

    public void setDefendant(String defendant) {
        this.defendant.set(defendant);
    }

    public double getCost() {
        return cost.get();
    }

    public DoubleProperty costProperty() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost.set(cost);
    }
}
