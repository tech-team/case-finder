package gui.casestable;

import javafx.beans.property.*;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class CaseModel {
    public final static Map<String, String> FIELD_NAMES = new HashMap<>();

    public static void loadTitles() throws CaseFieldNamesMismatchException {
        ResourceBundle res = ResourceBundle.getBundle("properties.case_field_names");

        Enumeration<String> nameItr = res.getKeys();
        while(nameItr.hasMoreElements()) {
            String key = nameItr.nextElement();
            FIELD_NAMES.put(key, res.getString(key));

            try {
                Field field = CaseModel.class.getField(key);
            }
            catch (NoSuchFieldException e) {
                throw new CaseFieldNamesMismatchException("No such field: " + key, e);
            }
        }

        if (FIELD_NAMES.size() != CaseModel.class.getDeclaredFields().length - 1) //without FIELD_NAMES
            throw new CaseFieldNamesMismatchException("CaseModel.fields.size() != res.size()");
    }

    public final IntegerProperty id;
    public final ObjectProperty<LocalDate> createdDate;
    public final StringProperty plaintiff;
    public final StringProperty defendant;
    public final DoubleProperty cost;

    public CaseModel() {
        this(null, null, null, null, null);
    }

    public CaseModel(Integer id, LocalDate createdDate, String plaintiff, String defendant, Double cost) {
        this.id = new SimpleIntegerProperty(id);
        this.createdDate = new SimpleObjectProperty<>(createdDate);
        
        this.plaintiff = new SimpleStringProperty(plaintiff);
        this.defendant = new SimpleStringProperty(defendant);
        
        this.cost = new SimpleDoubleProperty(cost);
    }
}
