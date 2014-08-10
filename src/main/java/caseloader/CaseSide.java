package caseloader;

import caseloader.credentials.Credentials;
import org.json.JSONObject;
import util.JsonUtils;

public class CaseSide {
    abstract class Keys {
        public static final String ADDRESS = "Address";
        public static final String INN = "Inn";
        public static final String IS_PHYSICAL = "IsPhysical";
        public static final String NAME = "Name";
        public static final String OGRN = "Ogrn";
        public static final String OKPO = "Okpo";
        public static final String SIDE_TYPE = "SideType";
        public static final String SUBJECT_CATEGORY = "SubjectCategory";
        public static final String TYPE = "Type";
    }

    enum SideType {
        PLAINTIFF,
        DEFENDER;

        private static final SideType[] allValues = values();
        public static SideType fromOrdinal(int n) {return allValues[n];}
    }

    private CaseSide() {
    }

    private String address = null;
    private String inn = null;
    private Boolean isPhysical = null;
    private String name = null;
    private String ogrn = null;
    private String okpo = null;
    private SideType sideType = null;
    private Object subjectCategory = null;
    private Integer type = null;
    private Credentials credentials = null;

    public String getAddress() {
        return address;
    }

    public String getInn() {
        return inn;
    }

    public Boolean isPhysical() {
        return isPhysical;
    }

    public String getName() {
        return name;
    }

    public String getOgrn() {
        return ogrn;
    }

    public String getOkpo() {
        return okpo;
    }

    public SideType getSideType() {
        return sideType;
    }

    public Object getSubjectCategory() {
        return subjectCategory;
    }

    public Integer getType() {
        return type;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public static CaseSide fromJSON(JSONObject obj) {
        CaseSide res = new CaseSide();

        res.address = JsonUtils.getString(obj, Keys.ADDRESS);
        res.inn = JsonUtils.getString(obj, Keys.INN);
        res.isPhysical = JsonUtils.getBoolean(obj, Keys.IS_PHYSICAL);
        res.name = JsonUtils.getString(obj, Keys.NAME);
        res.ogrn = JsonUtils.getString(obj, Keys.OGRN);
        res.okpo = JsonUtils.getString(obj, Keys.OKPO);
        res.sideType = SideType.fromOrdinal(JsonUtils.getInteger(obj, Keys.SIDE_TYPE));
        res.subjectCategory = JsonUtils.getObject(obj, Keys.SUBJECT_CATEGORY);
        res.type = JsonUtils.getInteger(obj, Keys.TYPE);

        return res;
    }
}
