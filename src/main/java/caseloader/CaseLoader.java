package caseloader;

import caseloader.kad.*;
import eventsystem.DataEvent;

import java.util.List;
import java.util.Set;

public class CaseLoader<CaseContainerType extends util.Appendable<CaseInfo>> {
    private KadLoader<CaseContainerType> kadLoader = new KadLoader<>();
    private KadSearchRequest request = null;
    private CaseContainerType outputContainer = null;
    private int minCost;
    private int searchLimit;

    public DataEvent<CaseContainerType> casesLoaded = new DataEvent<>();
    public DataEvent<Set<String>> courtsLoaded = new DataEvent<>();

    public CaseLoader() {
    }

    public void setKadRequest(KadSearchRequest request) {
        this.request = request;
    }

    public Thread retrieveDataAsync() {
        return new Thread(() -> {
            System.out.println("--- Started CaseLoader ---");

            if (this.request == null) {
                throw new RuntimeException("request is null");
            }
            kadLoader.retrieveData(this.request, this.minCost, this.searchLimit, this.outputContainer);
            casesLoaded.fire(this.outputContainer);

            System.out.println("--- Finished CaseLoader ---");
        });
    }

    public Thread retrieveCourtsAsync() {
        return new Thread(() -> {
            System.out.println("--- Retrieving courts list ---");
            Set<String> courts = kadLoader.retrieveCourts();
            courtsLoaded.fire(courts);
            System.out.println("--- Finished retrieving courts list ---");
        });
    }

    public String getCourtCode(String courtName) {
        return kadLoader.courtCode(courtName);
    }

    public void setOutputContainer(CaseContainerType outputContainer) {
        this.outputContainer = outputContainer;
    }

    public CaseContainerType getOutputContainer() {
        return outputContainer;
    }

    public void setMinCost(int minCost) {
        this.minCost = minCost;
    }

    public int getMinCost() {
        return minCost;
    }

    public void setSearchLimit(int searchLimit) {
        this.searchLimit = searchLimit;
    }

    public int getSearchLimit() {
        return searchLimit;
    }



    public static void main(String[] args) {
        CaseLoader<CasesData> cl = new CaseLoader<>();
        cl.setKadRequest(new KadSearchRequest());

        new CasesLoadedHandler<>(cl);

        Thread th = cl.retrieveDataAsync();
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
