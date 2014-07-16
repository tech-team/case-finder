package eventsystem;

import javafx.application.Platform;
import java.util.ArrayList;
import java.util.function.Consumer;

public class DataEvent<T> {
    ArrayList<Consumer<T>> handlers = new ArrayList<>();

    public void on(Consumer<T> handler) {
        handlers.add(handler);
    }

    public void off(Consumer<T> handler) {
        handlers.remove(handler);
    }

    public void fire(T data) {
        handlers.forEach(handler -> {
            if (Platform.isFxApplicationThread())
                handler.accept(data);
            else
                Platform.runLater(() -> handler.accept(data));
        });
    }
}
