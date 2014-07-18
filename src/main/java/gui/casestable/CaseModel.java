package gui.casestable;

import javafx.beans.property.*;

import java.time.LocalDate;

public class CaseModel {
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
