package export;

import caseloader.CaseSearchRequest;
import gui.casestable.CaseModel;
import javafx.collections.ObservableList;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
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
        REQUEST, TITLE, NORMAL
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
            titleCell.setCellStyle(styles.get(CellType.TITLE));
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

        //request row
        Row requestRow = sheet.createRow(rowId++);
        requestRow.setHeightInPoints(45);
        Cell requestCell = requestRow.createCell(0);
        requestCell.setCellValue(res.getString("requestTemplate"));
        requestCell.setCellStyle(styles.get(CellType.REQUEST));
        sheet.addMergedRegion(CellRangeAddress.valueOf("$A$1:$L$1"));

        /*request.getCourts();
        request.getCaseType();
        request.isWithVKSInstances()
        request.getDateFrom();
        request.getDateTo()
        request.getMinCost()
        request.getSearchLimit()*/
    }

    private static Map<CellType, CellStyle> createStyles(Workbook wb){
        Map<CellType, CellStyle> styles = new HashMap<>();

        CellStyle style;
        Font titleFont = wb.createFont();
        titleFont.setFontHeightInPoints((short)18);
        titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setFont(titleFont);
        styles.put(CellType.REQUEST, style);

        Font monthFont = wb.createFont();
        monthFont.setFontHeightInPoints((short)11);
        monthFont.setColor(IndexedColors.WHITE.getIndex());
        style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setFont(monthFont);
        style.setWrapText(true);
        styles.put(CellType.TITLE, style);

        style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setWrapText(true);
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderTop(CellStyle.BORDER_THIN);
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        styles.put(CellType.NORMAL, style);

        /*style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setDataFormat(wb.createDataFormat().getFormat("0.00"));
        styles.put("formula", style);

        style = wb.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setDataFormat(wb.createDataFormat().getFormat("0.00"));
        styles.put("formula_2", style);*/

        return styles;
    }
}
