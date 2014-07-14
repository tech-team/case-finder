package eventsystem;

import javafx.application.Application;
import javafx.stage.Stage;

public class EventSourceTest extends Application {
    private static boolean jfxReady = false;

    @org.junit.Before
    public void setUp() throws Exception {
        Thread jfxThread = new Thread(() -> {
            //initialize JFX for Platform.runLater()
            Application.launch(EventSourceTest.class); //will create another instance of EventSourceTest
        });
        jfxThread.setDaemon(true);
        jfxThread.start();

        while (!jfxReady) {
            Thread.sleep(10);
        }
    }

    @org.junit.Test(timeout = 5000)
    public void testFire() throws Exception {
        Source source = new Source();
        Handler handler = new Handler(source);

        Thread worker = new Thread(source::someFunc);
        worker.start();

        while (!handler.testOk) {
            Thread.sleep(10);
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        jfxReady = true;
    }
}

class Source implements IEventSource {
    private EventSource es = new EventSource();

    final public Event somethingHappened = new Event();

    public void someFunc() {
        //some code here
        System.out.println("firing");
        es.fire(somethingHappened);
    }

    @Override
    public EventSource getEventSource() {
        return es;
    }
}

class Handler {
    public boolean testOk = false;

    public Handler(Source source) {
        source.getEventSource().on(source.somethingHappened, () -> {
            System.out.println("something happened!");
            testOk = true;
        });
    }
}