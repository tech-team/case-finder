package eventsystem;

import javafx.application.Platform;
import java.util.ArrayList;
import java.util.function.Consumer;

public class DataEvent<T> {
    ArrayList<EventContainer<Consumer<T>>> handlerContainers = new ArrayList<>();

    public void on(Consumer<T> handler) {
        handlerContainers.add(
                new EventContainer<>(handler, Thread.currentThread()));
    }

    public void off(EventHandler handler) {
        handlerContainers.removeIf(container -> container.getHandler() == handler);
    }

    public void fire(T data) {
        handlerContainers.forEach(handlerContainer -> {
            if (Thread.currentThread() == handlerContainer.getThread()
                    || Platform.isFxApplicationThread())
                handlerContainer.getHandler().accept(data);
            else
                Platform.runLater(() -> handlerContainer.getHandler().accept(data));
        });
    }
}
