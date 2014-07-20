package export;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public enum Extension {
    Excel("Excel Files", "*.xlsx", XSSFWorkbook.class),
    AncientExcel("Ancient Excel Files", "*.xls", HSSFWorkbook.class);

    private final String name;
    private final String value;
    private final Class workbookClass;

    private Extension(String name, String value, Class workbookClass) {
        this.name = name;
        this.value = value;
        this.workbookClass = workbookClass;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getStarlessValue() {
        return value.substring(1);
    }

    public Class getWorkbookClass() {
        return workbookClass;
    }

    public static Extension fromName(String name) {
        if (name.equals(Excel.name))
            return Excel;
        else
            return AncientExcel;
    }
}
