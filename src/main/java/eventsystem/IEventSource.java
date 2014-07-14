package eventsystem;

//created to overcome need to subclass from EventSource
public interface IEventSource {
    EventSource getEventSource();
}
