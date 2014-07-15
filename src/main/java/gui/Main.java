package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/fxml/main.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Case Finder");
        primaryStage.setScene(new Scene(root, 600, 400));

        MainController controller = loader.getController();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
