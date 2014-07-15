package gui.casestable;

import javafx.beans.property.*;

import java.time.LocalDate;

public class Case {
    private final IntegerProperty id;
    private final ObjectProperty<LocalDate> createdDate;

    private final StringProperty plaintiff;
    private final StringProperty defendant;
    
    private final DoubleProperty cost;

    public Case() {
        this(null, null, null, null, null);
    }

    public Case(Integer id, LocalDate createdDate, String plaintiff, String defendant, Double cost) {
        this.id = new SimpleIntegerProperty(id);
        this.createdDate = new SimpleObjectProperty<>(createdDate);
        
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

    public void setId(Integer value) {
        id.set(value);
    }

    public LocalDate getCreatedDate() {
        return createdDate.get();
    }

    public ObjectProperty<LocalDate> createdDateProperty() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate value) {
        this.createdDate.set(value);
    }

    public String getPlaintiff() {
        return plaintiff.get();
    }

    public StringProperty plaintiffProperty() {
        return plaintiff;
    }

    public void setPlaintiff(String value) {
        plaintiff.set(value);
    }

    public String getDefendant() {
        return defendant.get();
    }

    public StringProperty defendantProperty() {
        return defendant;
    }

    public void setDefendant(String value) {
        defendant.set(value);
    }

    public Double getCost() {
        return cost.get();
    }

    public DoubleProperty costProperty() {
        return cost;
    }

    public void setCost(Double value) {
        cost.set(value);
    }
}
