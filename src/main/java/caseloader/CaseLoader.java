package caseloader;

import caseloader.kad.*;
import eventsystem.DataEvent;

import java.util.List;

public class CaseLoader<OutputType extends util.Appendable<CaseInfo>> implements Runnable {
    private KadLoader<OutputType> kadLoader = new KadLoader<>();
    private KadSearchRequest request = null;

    public DataEvent<OutputType> casesLoaded = new DataEvent<>();
    private OutputType outputContainer = null;
    private int minCost;
    private int searchLimit;

    public CaseLoader() {
    }

    public void setKadRequest(KadSearchRequest request) {
        this.request = request;
    }

    @Override
    public void run() {
        System.out.println("--- Started CaseLoader ---");

        if (this.request == null) {
            throw new RuntimeException("request is null");
        }
        kadLoader.retrieveData(this.request, this.outputContainer);
        casesLoaded.fire(this.outputContainer);

        System.out.println("--- Finished CaseLoader ---");
    }

    public void setOutputContainer(OutputType outputContainer) {
        this.outputContainer = outputContainer;
    }

    public OutputType getOutputContainer() {
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

        CasesLoadedHandler<CasesData> handler = new CasesLoadedHandler<>(cl);

        Thread th = new Thread(cl);
        th.start();
        try {
            th.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class CasesLoadedHandler<OutputType extends util.Appendable<CaseInfo>> {

    CasesLoadedHandler(CaseLoader<OutputType> cl) {
        cl.casesLoaded.on((data) -> {
            List<CaseInfo> cases = (List<CaseInfo>) data.getCollection();
            System.out.println("Cases loaded successfully");
        });
    }
}
