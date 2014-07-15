package gui.casestable;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class CasesCellFactory<S, T> implements Callback<TableColumn<S, T>, TableCell<S, T>> {
    public CasesCellFactory() {

    }

    @Override
    public TableCell<S, T> call(TableColumn<S, T> p) {
        TableCell<S, T> cell = new TableCell<S, T>() {
            @Override
            protected void updateItem(Object item, boolean empty) {

            }
        };
        return cell;
    }
}