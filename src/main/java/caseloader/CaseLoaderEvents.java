package caseloader;

import caseloader.errors.CaseLoaderError;
import eventsystem.DataEvent;
import eventsystem.Event;

public class CaseLoaderEvents <CaseContainerType extends util.Appendable<CaseInfo>> {
    private static CaseLoaderEvents instance = null;

    public final DataEvent<Integer> totalCasesCountObtained = new DataEvent<>();
    public final Event caseProcessed = new Event();
    public final DataEvent<CaseContainerType> casesLoaded = new DataEvent<>();
    public final DataEvent<CaseLoaderError> onError = new DataEvent<>();

    private CaseLoaderEvents() {
    }

    public void offSubscribers() {
        totalCasesCountObtained.off();
        caseProcessed.off();
        casesLoaded.off();
        onError.off();
    }


    @SuppressWarnings("unchecked")
    public static <CaseContainerType extends util.Appendable<CaseInfo>> CaseLoaderEvents<CaseContainerType> instance() {
        if (instance == null) {
            instance = new CaseLoaderEvents<>();
        }
        return (CaseLoaderEvents<CaseContainerType>) instance;
    }
}
