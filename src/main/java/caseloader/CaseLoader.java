package caseloader;

import caseloader.credentials.*;
import caseloader.kad.*;
import eventsystem.DataEvent;

public class CaseLoader implements Runnable {
    private KadLoader kadLoader = new KadLoader();
    private CredentialsLoader credentialsLoader = new CredentialsLoader();
    private KadSearchRequest request = null;

    private DataEvent<CasesData> casesLoaded = new DataEvent<>();

    public CaseLoader() {
    }

    public DataEvent<CasesData> casesLoaded() {
        return casesLoaded;
    }

    public void setKadRequest(KadSearchRequest request) {
        this.request = request;
    }

    @Override
    public void run() {
        System.out.println("--- Started CaseLoader ---");
        job();
        System.out.println("--- Finished CaseLoader ---");
    }

    private void job() {
        if (this.request == null) {
            throw new RuntimeException("request is null");
        }
        KadData kadData = kadLoader.retrieveKadData(this.request);

        CasesData casesData = new CasesData(kadData);
        casesLoaded.fire(casesData);
    }

    public static void main(String[] args) {
        CaseLoader cl = new CaseLoader();
        cl.setKadRequest(new KadSearchRequest());
        Thread th = new Thread(cl);
        th.start();
        try {
            th.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
