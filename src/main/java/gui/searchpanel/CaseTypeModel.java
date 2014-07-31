package gui.searchpanel;

import caseloader.CaseSearchRequest;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import util.ResourceControl;

import java.util.ResourceBundle;

public class CaseTypeModel {
    private StringProperty str = new SimpleStringProperty();
    private ObjectProperty<CaseSearchRequest.CaseType> type = new SimpleObjectProperty<>();

    private static ObservableList<CaseTypeModel> collection = FXCollections.observableArrayList();

    static {
        ResourceBundle res = ResourceBundle.getBundle("properties.export_strings", new ResourceControl("UTF-8"));

        CaseTypeModel anyModel = new CaseTypeModel(res.getString("any"), null);
        collection.add(anyModel);

        for (CaseSearchRequest.CaseType type: CaseSearchRequest.CaseType.values()) {
            CaseTypeModel model = new CaseTypeModel(res.getString(type.toString()), type);
            collection.add(model);
        }
    }

    private CaseTypeModel() {}

    private CaseTypeModel(String str, CaseSearchRequest.CaseType type) {
        this.str.setValue(str);
        this.type.setValue(type);
    }

    public static ObservableList<CaseTypeModel> getCollection() {
        return collection;
    }

    public CaseSearchRequest.CaseType getType() {
        return type.get();
    }

    public String getStr() {
        return str.get();
    }

    @Override
    public String toString() {
        return getStr();
    }
}
