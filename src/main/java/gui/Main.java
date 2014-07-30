package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.prefs.Preferences;

public class Main extends Application {
    public static Application instance;
    private static final String MAXIMIZED_PROPERTY = "maximized";
    private static final String WIDTH_PROPERTY = "width";
    private static final String HEIGHT_PROPERTY = "height";

    public Main() {
        instance = this;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Preferences prefs = Preferences.userNodeForPackage(Main.class);

        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/fxml/main.fxml"));
        Parent root = loader.load();

        double width = prefs.getDouble(WIDTH_PROPERTY, 1024);
        double height = prefs.getDouble(HEIGHT_PROPERTY, 768);

        Scene scene = new Scene(root, width, height);
        scene.getStylesheets().add("css/styles.css");

        primaryStage.setTitle("Case Finder");
        primaryStage.setScene(scene);

        MainController controller = loader.getController();
        controller.setStage(primaryStage);
        primaryStage.setOnCloseRequest((WindowEvent event) -> {
            controller.onClose(event);

            if (!event.isConsumed()) {
                prefs.putBoolean(MAXIMIZED_PROPERTY, primaryStage.isMaximized());
                prefs.putDouble(WIDTH_PROPERTY, primaryStage.getWidth());
                prefs.putDouble(HEIGHT_PROPERTY, primaryStage.getHeight());
            }
        });

        primaryStage.setMaximized(prefs.getBoolean(MAXIMIZED_PROPERTY, true));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
