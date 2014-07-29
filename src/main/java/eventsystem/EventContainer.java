package eventsystem;

public class EventContainer<T> {
    private T handler;
    private Thread thread;

    public EventContainer(T handler, Thread thread) {
        this.thread = thread;
        this.handler = handler;
    }

    public T getHandler() {
        return handler;
    }

    public Thread getThread() {
        return thread;
    }
}