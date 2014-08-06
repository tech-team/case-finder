package caseloader;

import caseloader.kad.*;
import eventsystem.DataEvent;
import eventsystem.Event;
import exceptions.DataRetrievingError;
import util.MyLogger;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CaseLoader<CaseContainerType extends util.Appendable<CaseInfo>> {
    public final DataEvent<Integer> totalCasesCountObtained = new DataEvent<>();
    public final Event caseProcessed = new Event();
    public final DataEvent<CaseContainerType> casesLoaded = new DataEvent<>();

    private KadLoader<CaseContainerType> kadLoader = new KadLoader<>(totalCasesCountObtained, caseProcessed);

    private Thread thread = null;
    private Logger logger = MyLogger.getLogger(this.getClass().toString());

    public CaseLoader() {
    }

    public void retrieveDataAsync(CaseSearchRequest request, CaseContainerType outputContainer) {
        if (request == null) {
            throw new RuntimeException("Request is null");
        }
        if (outputContainer == null) {
            throw new RuntimeException("Output container is null");
        }

        thread = new Thread(() -> {
            logger.info("Started CaseLoader");
            try {
                kadLoader.retrieveData(request, outputContainer);
            } catch (IOException | DataRetrievingError e) {
                throw new RuntimeException(e);
            }
            logger.info("Finished CaseLoader");

            casesLoaded.fire(outputContainer);
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void waitForRetrieval() throws InterruptedException {
        thread.join();
    }

    public void stopExecution() {
        kadLoader.stopExecution();
        thread.interrupt();
    }




    public static void main(String[] args) throws InterruptedException {
        MyLogger.getGlobal().log(Level.INFO, "MAIN BEGIN");
        long beginTime = System.currentTimeMillis();

        CaseLoader<CasesData> cl = new CaseLoader<>();

        cl.casesLoaded.on((data) -> {
            List<CaseInfo> cases = data.getCollection();
            System.out.println("Cases loaded successfully");
        });

        CasesData data = new CasesData();
        CaseSearchRequest req = new CaseSearchRequest(null, null, null, null, null, 0, 100);
        cl.retrieveDataAsync(req, data);

        cl.waitForRetrieval();

        long time = System.currentTimeMillis() - beginTime;
        int s = (int) (time / 1000) % 60 ;
        int m = (int) ((time / (1000*60)) % 60);
        int h   = (int) ((time / (1000*60*60)) % 24);
        MyLogger.getGlobal().log(Level.INFO, "MAIN ENDED");
        MyLogger.getGlobal().log(Level.INFO, "Time elapsed: time=" + time);
        MyLogger.getGlobal().log(Level.INFO, "Time elapsed: h=" + h + " m=" + m + " s=" + s);
        MyLogger.close();
    }
}
