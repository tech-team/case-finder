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

        CasesData casesData = new CasesData(kadData.getEntries().size());
        casesData.setTotalCount(kadData.getTotalCount());

        for (KadDataEntry entry : kadData.getEntries()) {
            for (KadResponseSide defendant : entry.getItem().getDefendants()) {
                CredentialsSearchRequest credentialsSearchRequest =
                        new CredentialsSearchRequest(defendant.getName(),
                                                     defendant.getAddress(),
                                                     defendant.getInn(),
                                                     defendant.getOgrn());
                Credentials defendantCredentials = credentialsLoader.retrieveCredentials(credentialsSearchRequest);
                defendant.setCredentials(defendantCredentials);
            }
            CaseInfo caseInfo = new CaseInfo(entry);
            casesData.addCase(caseInfo);
        }
        casesLoaded.fire(casesData);
    }
}
