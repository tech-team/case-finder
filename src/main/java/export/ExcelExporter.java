package export;

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

    public static void export(ObservableList<CaseModel> data, String fileName, Extension extension) throws IOException, UnsupportedExtensionException {
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

        export(data, actualFileName, wb);
    }

    private static void export(ObservableList<CaseModel> data, String fileName, Workbook wb) throws IOException {
        ResourceBundle res = ResourceBundle.getBundle("properties.export_strings", new ResourceControl("UTF-8"));

        Map<CellType, CellStyle> styles = createStyles(wb);

        Sheet sheet = wb.createSheet(res.getString("sheetName"));
        PrintSetup printSetup = sheet.getPrintSetup();
        printSetup.setLandscape(true);
        sheet.setFitToPage(true);
        sheet.setHorizontallyCenter(true);

        //request row
        Row requestRow = sheet.createRow(0);
        requestRow.setHeightInPoints(45);
        Cell requestCell = requestRow.createCell(0);
        requestCell.setCellValue(res.getString("requestTemplate"));
        requestCell.setCellStyle(styles.get(CellType.REQUEST));
        sheet.addMergedRegion(CellRangeAddress.valueOf("$A$1:$L$1"));

        //title row
        Row titleRow = sheet.createRow(1);
        titleRow.setHeightInPoints(40);

        //get model titles
        int i = 0;
        for (Map.Entry<String, String> entry: CaseModel.FIELD_NAMES.entrySet()) {
            Cell titleCell = titleRow.createCell(i);
            titleCell.setCellValue(entry.getValue());
            titleCell.setCellStyle(styles.get(CellType.TITLE));
            ++i;
        }

        //save to file
        FileOutputStream out = new FileOutputStream(fileName);
        wb.write(out);
        out.close();
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
