package eventsystem;

import javafx.application.Platform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EventSource {
    Map<Event, ArrayList<EventHandler>> eventMap = new HashMap<>();

    public void on(Event event, EventHandler handler) {
        ArrayList<EventHandler> handlers = eventMap.get(event);

        if (handlers == null) {
            handlers = new ArrayList<EventHandler>();
            eventMap.put(event, handlers);
        }

        handlers.add(handler);
    }

    public void off(Event event, EventHandler handler) {
        ArrayList<EventHandler> handlers = eventMap.get(event);

        if (handlers != null) {
            handlers.remove(handler);
        }
    }

    public void fire(Event eventName) {
        ArrayList<EventHandler> handlers = eventMap.get(eventName);

        if (handlers != null) {
            handlers.forEach(handler -> {
                if (Platform.isFxApplicationThread())
                    handler.accept();
                else
                    Platform.runLater(handler::accept);
            });
        }
    }
}
