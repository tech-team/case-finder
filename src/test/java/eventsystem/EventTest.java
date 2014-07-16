package eventsystem;

import javafx.application.Application;
import javafx.stage.Stage;

public class EventTest extends Application {
    private static boolean jfxReady = false;

    @org.junit.Before
    public void setUp() throws Exception {
        Thread jfxThread = new Thread(() -> {
            //initialize JFX for Platform.runLater()
            Application.launch(EventTest.class); //will create another instance of EventSourceTest
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

        while (!handler.testEventOk || !handler.testDataEventOk) {
            Thread.sleep(10);
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        jfxReady = true;
    }
}

class Source {
    final public Event somethingHappened = new Event();
    final public DataEvent<String> dataReady = new DataEvent<>();

    public void someFunc() {
        //some code here
        System.out.println("firing somethingHappened");
        somethingHappened.fire();

        //some code here
        String data = "some data";
        System.out.println("firing dataReady with: " + data);
        dataReady.fire(data);
    }
}

class Handler {
    public boolean testEventOk = false;
    public boolean testDataEventOk = false;

    public Handler(Source source) {
        source.somethingHappened.on(() -> {
            System.out.println("something happened!");
            testEventOk = true;
        });

        source.dataReady.on((String data) -> {
            System.out.println("dataReady happened with: " + data);
            testDataEventOk = true;
        });
    }
}