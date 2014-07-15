package gui.casestable;

import javafx.beans.property.*;

import java.time.LocalDate;

public class Case {
    private final IntegerProperty id;
    private final ObjectProperty<LocalDate> createdDate;

    private final StringProperty plaintiff;
    private final StringProperty defendant;

    public Case() {
        this(0, null, null, null);
    }

    public Case(int id, LocalDate createdDate, String plaintiff, String defendant) {
        this.id = new SimpleIntegerProperty(id);
        this.createdDate = new SimpleObjectProperty<>(createdDate);
        
        this.plaintiff = new SimpleStringProperty(plaintiff);
        this.defendant = new SimpleStringProperty(defendant);
    }

    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public void setId(int value) {
        id.set(value);
    }

    public LocalDate getCreatedDate() {
        return createdDate.get();
    }

    public ObjectProperty<LocalDate> createdDateProperty() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate date) {
        createdDate.set(date);
    }

    public String getPlaintiff() {
        return plaintiff.get();
    }

    public StringProperty plaintiffProperty() {
        return plaintiff;
    }

    public void setPlaintiff(String date) {
        plaintiff.set(date);
    }

    public String getDefendant() {
        return defendant.get();
    }

    public StringProperty defendantProperty() {
        return defendant;
    }

    public void setDefendant(String date) {
        defendant.set(date);
    }
}
