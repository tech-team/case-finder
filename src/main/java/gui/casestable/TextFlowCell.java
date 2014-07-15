package gui.casestable;

import com.sun.deploy.uitoolkit.impl.fx.HostServicesFactory;
import gui.Main;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableCell;
import javafx.scene.text.TextFlow;

public class TextFlowCell<S, T> extends TableCell<S, T> {
    private final TextFlow textFlow;
    private ObservableValue<T> ov;

    public TextFlowCell() {
        this.textFlow = new TextFlow();

        setAlignment(Pos.CENTER);
        setGraphic(textFlow);
    }

    @Override public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        }
        else {
            setGraphic(textFlow);

            ov = getTableColumn().getCellObservableValue(getIndex());
            T value = ov.getValue();

            if (value instanceof String) {
                String str = (String) value;

                //find links and replace with Hyperlink components

                textFlow.getChildren().clear();
                Hyperlink text = new Hyperlink(str);
                text.setOnAction(actionEvent -> {
                    HostServicesFactory.getInstance(Main.instance)
                            .showDocument(str);
                });

                textFlow.getChildren().add(text);
            }
        }
    }
}
