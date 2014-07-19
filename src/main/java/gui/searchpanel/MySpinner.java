package gui.searchpanel;

import gui.Main;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class MySpinner extends HBox {
    public MySpinner() {
        super();
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/fxml/my_spinner.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
