package eventsystem;

class EventContainer<T> {
    private final T handler;
    private final Thread thread;

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