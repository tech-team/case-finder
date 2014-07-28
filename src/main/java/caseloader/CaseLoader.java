package caseloader;

import caseloader.kad.*;
import eventsystem.DataEvent;
import exceptions.DataRetrievingError;
import proxy.ProxyList;
import proxy.ProxyUpdater;
import util.MyLogger;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CaseLoader<CaseContainerType extends util.Appendable<CaseInfo>> {
    private KadLoader<CaseContainerType> kadLoader = new KadLoader<>();
    public DataEvent<CaseContainerType> casesLoaded = new DataEvent<>();

    private Logger logger = MyLogger.getLogger(this.getClass().toString());

    public CaseLoader() {
    }

    public Thread retrieveDataAsync(CaseSearchRequest request, CaseContainerType outputContainer) {
        if (request == null) {
            throw new RuntimeException("Request is null");
        }
        if (outputContainer == null) {
            throw new RuntimeException("Output container is null");
        }

        return new Thread(() -> {
            logger.info("Started CaseLoader");

            try {
                kadLoader.retrieveData(request, outputContainer);
            } catch (IOException | DataRetrievingError e) {
                throw new RuntimeException(e);
            }
            casesLoaded.fire(outputContainer);

            logger.info("Finished CaseLoader");
        });
    }




    public static void main(String[] args) throws InterruptedException {
        MyLogger.getGlobal().log(Level.INFO, "MAIN BEGIN");
        long beginTime = System.currentTimeMillis();

        CaseLoader<CasesData> cl = new CaseLoader<>();
        CasesLoadedHandler handler = new CasesLoadedHandler<>(cl);

        CasesData data = new CasesData();
        Thread th = cl.retrieveDataAsync(new CaseSearchRequest(), data);
        th.start();
        try {
            th.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long time = System.currentTimeMillis() - beginTime;
        int s = (int) (time / 1000) % 60 ;
        int m = (int) ((time / (1000*60)) % 60);
        int h   = (int) ((time / (1000*60*60)) % 24);
        MyLogger.getGlobal().log(Level.INFO, "MAIN ENDED");
        MyLogger.getGlobal().log(Level.INFO, "Time elapsed: time=" + time);
        MyLogger.getGlobal().log(Level.INFO, "Time elapsed: h=" + h + " m=" + m + " s=" + s);
    }
}

class CasesLoadedHandler<CaseContainerType extends util.Appendable<CaseInfo>> {

    CasesLoadedHandler(CaseLoader<CaseContainerType> cl) {
        cl.casesLoaded.on((data) -> {
            List<CaseInfo> cases = (List<CaseInfo>) data.getCollection();
            System.out.println("Cases loaded successfully");
        });
    }
}
