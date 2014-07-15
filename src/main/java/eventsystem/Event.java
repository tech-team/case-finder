package eventsystem;

import javafx.application.Platform;
import java.util.ArrayList;

public class Event {
    ArrayList<EventHandler> handlers = new ArrayList<>();

    public void on(EventHandler handler) {
        handlers.add(handler);
    }

    public void off(EventHandler handler) {
        handlers.remove(handler);
    }

    public void fire() {
        handlers.forEach(handler -> {
            if (Platform.isFxApplicationThread())
                handler.accept();
            else
                Platform.runLater(handler::accept);
        });
    }
}
