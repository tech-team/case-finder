package caseloader;

import caseloader.kad.*;
import eventsystem.DataEvent;
import exceptions.DataRetrievingError;
import proxy.ProxyList;
import proxy.ProxyUpdater;

import java.io.IOException;
import java.util.List;

public class CaseLoader<CaseContainerType extends util.Appendable<CaseInfo>> {
    private KadLoader<CaseContainerType> kadLoader = new KadLoader<>();
    public DataEvent<CaseContainerType> casesLoaded = new DataEvent<>();

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
            System.out.println("--- Started CaseLoader ---");
            try {
                kadLoader.retrieveData(request, outputContainer);
            } catch (IOException | DataRetrievingError e) {
                throw new RuntimeException(e);
            }
            casesLoaded.fire(outputContainer);

            System.out.println("--- Finished CaseLoader ---");
        });
    }




    public static void main(String[] args) throws InterruptedException {
        System.out.println("MAIN BEGIN");
        long beginTime = System.currentTimeMillis();
        System.out.println(beginTime);

        ProxyUpdater proxyUpdater = new ProxyUpdater();
        (new Thread(proxyUpdater)).start();

        while (!ProxyList.proxiesLoaded()) {
            Thread.sleep(100);
        }

        CaseLoader<CasesData> cl = new CaseLoader<>();
        CasesLoadedHandler handler = new CasesLoadedHandler<>(cl);

        CasesData data = new CasesData();
        Thread th = cl.retrieveDataAsync(new CaseSearchRequest(), data);
        th.start();
        try {
            th.join();
            proxyUpdater.stop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long time = System.currentTimeMillis() - beginTime;
        int s = (int) (time / 1000) % 60 ;
        int m = (int) ((time / (1000*60)) % 60);
        int h   = (int) ((time / (1000*60*60)) % 24);
        System.out.println("MAIN END");
        System.out.println("Time elapsed: time=" + time);
        System.out.println("Time elapsed: h=" + h + " m=" + m + " s=" + s);
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
