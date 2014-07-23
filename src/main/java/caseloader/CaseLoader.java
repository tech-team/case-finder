package caseloader;

import caseloader.kad.*;
import eventsystem.DataEvent;

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
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            casesLoaded.fire(outputContainer);

            System.out.println("--- Finished CaseLoader ---");
        });
    }




    public static void main(String[] args) {
        CaseLoader<CasesData> cl = new CaseLoader<>();
        new CasesLoadedHandler<>(cl);

        Thread th = cl.retrieveDataAsync(new CaseSearchRequest(), new CasesData());
        th.start();
        try {
            th.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
