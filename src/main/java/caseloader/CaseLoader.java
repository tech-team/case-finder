package caseloader;

import caseloader.credentials.*;
import caseloader.kad.*;

public class CaseLoader implements Runnable {
    private KadLoader kadLoader = new KadLoader();
    private CredentialsLoader credentialsLoader = new CredentialsLoader();
    private KadSearchRequest request = null;

    public CaseLoader() {
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
        }


    }
}
