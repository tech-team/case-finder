package caseloader;

import caseloader.errors.CaseLoaderError;
import caseloader.errors.ErrorReason;
import caseloader.kad.KadLoader;
import eventsystem.DataEvent;
import eventsystem.Event;
import exceptions.DataRetrievingError;
import util.MyLogger;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CaseLoader<CaseContainerType extends util.Appendable<CaseInfo>> {
    private KadLoader<CaseContainerType> kadLoader = new KadLoader<>();

    private Thread thread = null;
    private Logger logger = MyLogger.getLogger(this.getClass().toString());

    public CaseLoader() {
    }

    public CaseLoaderEvents<CaseContainerType> events() {
        return CaseLoaderEvents.instance();
    }

    public void retrieveDataAsync(CaseSearchRequest request, CaseContainerType outputContainer) {
        if (request == null) {
            throw new RuntimeException("Request is null");
        }
        if (outputContainer == null) {
            throw new RuntimeException("Output container is null");
        }

        thread = new Thread(() -> {
            logger.info("====================================");
            logger.info("Started CaseLoader");
            try {
                CaseContainerType data = kadLoader.retrieveData(request, outputContainer);
                if (data == null) {
                    CaseLoaderError error = new CaseLoaderError(ErrorReason.KAD_PAGE_ERROR, "Error retrieving Kad pages. Try again later");
                    events().onError.fire(error);
                    logger.info("CaseLoader finished with error: " + error.getReason());
                    return;
                } else {
                    logger.info("Finished CaseLoader");
                }
            } catch (InterruptedException ignored) {
                logger.info("CaseLoader stopped");
            } catch (DataRetrievingError e) {
                logger.log(Level.SEVERE, "Exception happened", e);
                events().onError.fire(new CaseLoaderError(ErrorReason.UNEXPECTED_ERROR, "Unexpected error happened"));
            } finally {
                logger.info("====================================");
            }
            CaseLoaderEvents.instance().casesLoaded.fire(outputContainer);
        });
        thread.start();
    }

    public void waitForRetrieval() throws InterruptedException {
        thread.join();
    }

    public void stopExecution() {
        kadLoader.stopExecution();
        thread.interrupt();
    }




    @SuppressWarnings("UnusedDeclaration")
    public static void main(String[] args) throws InterruptedException {
        MyLogger.getGlobal().log(Level.INFO, "MAIN BEGIN");
        long beginTime = System.currentTimeMillis();

        CaseLoader<CasesData> cl = new CaseLoader<>();

        cl.events().casesLoaded.on((data) -> {
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
