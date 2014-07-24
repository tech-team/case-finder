package export;

import caseloader.CaseSearchRequest;
import gui.casestable.CaseModel;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import util.ResourceControl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class ExcelExporter {
    /**
     * see http://poi.apache.org/spreadsheet/examples.html
     */
    private enum CellType {
        BOLD
    }

    public static void export(CaseSearchRequest request, ObservableList<CaseModel> data, String fileName, Extension extension) throws IOException, UnsupportedExtensionException {
        Workbook wb = null;
        try {
            wb = (Workbook) extension.getWorkbookClass().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new UnsupportedExtensionException(e);
        }

        //change fileName's extension to selected extension
        String actualFileName = fileName.replaceFirst(
                "\\.xlsx?$",
                extension.getStarlessValue());

        export(request, data, actualFileName, wb);
    }

    private static void export(CaseSearchRequest request, ObservableList<CaseModel> data, String fileName, Workbook wb) throws IOException {
        ResourceBundle res = ResourceBundle.getBundle("properties.export_strings", new ResourceControl("UTF-8"));

        Map<CellType, CellStyle> styles = createStyles(wb);

        saveData(wb, data, res, styles);
        saveRequest(wb, request, res, styles);

        //save to file
        FileOutputStream out = new FileOutputStream(fileName);
        wb.write(out);
        out.close();
    }

    private static void saveData(Workbook wb, ObservableList<CaseModel> data, ResourceBundle res, Map<CellType, CellStyle> styles) {
        Sheet sheet = wb.createSheet(res.getString("dataSheetName"));
        PrintSetup printSetup = sheet.getPrintSetup();
        printSetup.setLandscape(true);
        sheet.setFitToPage(true);
        sheet.setHorizontallyCenter(true);

        int rowId = 0;

        //title row
        Row titleRow = sheet.createRow(rowId++);
        titleRow.setHeightInPoints(40);

        //get model titles
        int i = 0;
        for (Map.Entry<String, String> entry: CaseModel.FIELD_NAMES.entrySet()) {
            Cell titleCell = titleRow.createCell(i);
            titleCell.setCellValue(entry.getValue());
            titleCell.setCellStyle(styles.get(CellType.BOLD));
            ++i;
        }
    }

    private static void saveRequest(Workbook wb, CaseSearchRequest request, ResourceBundle res, Map<CellType, CellStyle> styles) {
        Sheet sheet = wb.createSheet(res.getString("requestSheetName"));
        PrintSetup printSetup = sheet.getPrintSetup();
        printSetup.setLandscape(true);
        sheet.setFitToPage(true);
        sheet.setHorizontallyCenter(true);

        int rowId = 0;

        String[] courts = request.getCourts();
        String courtsString = StringUtils.join(courts, ", ");
        createKeyValueRow(sheet, styles, rowId++,
                res.getString("requestCourt"), courtsString);

        String caseType = res.getString(request.getCaseType().toString());
        createKeyValueRow(sheet, styles, rowId++,
                res.getString("requestType"), caseType);

        String withVKSInstances = request.isWithVKSInstances() ?
                res.getString("yes") : res.getString("no");
        createKeyValueRow(sheet, styles, rowId++,
                res.getString("requestWithVksInstances"), withVKSInstances);

        String dateFrom = request.getDateFrom();
        createKeyValueRow(sheet, styles, rowId++,
                res.getString("requestDateFrom"), dateFrom);

        String dateTo = request.getDateTo();
        createKeyValueRow(sheet, styles, rowId++,
                res.getString("requestDateTo"), dateTo);

        String minCost = Integer.toString(request.getMinCost());
        createKeyValueRow(sheet, styles, rowId++,
                res.getString("requestMinimalCost"), minCost);

        String searchLimit = Integer.toString(request.getSearchLimit());
        createKeyValueRow(sheet, styles, rowId,
                res.getString("requestSearchLimit"), searchLimit);
    }

    private static void createKeyValueRow(Sheet sheet, Map<CellType, CellStyle> styles, int rowId, String key, String value) {
        Row row = sheet.createRow(rowId);
        Cell keyCell = row.createCell(0);
        keyCell.setCellValue(key);
        keyCell.setCellStyle(styles.get(CellType.BOLD));

        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);
    }

    private static Map<CellType, CellStyle> createStyles(Workbook wb){
        Map<CellType, CellStyle> styles = new HashMap<>();

        CellStyle style;
        Font boldFont = wb.createFont();
        boldFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        style = wb.createCellStyle();
        style.setFont(boldFont);
        styles.put(CellType.BOLD, style);

        return styles;
    }
}
