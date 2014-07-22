package export;

import gui.casestable.CaseModel;
import javafx.collections.ObservableList;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

public class ExcelExporter {
    /**
     * see http://poi.apache.org/spreadsheet/examples.html
     */
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
        Sheet sheet = wb.createSheet("Отчёт CaseFinder");
        PrintSetup printSetup = sheet.getPrintSetup();
        printSetup.setLandscape(true);
        sheet.setFitToPage(true);
        sheet.setHorizontallyCenter(true);

        //title row
        Row titleRow = sheet.createRow(0);
        titleRow.setHeightInPoints(45);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Здесь будут описаны параметры запроса");
        //titleCell.setCellStyle(styles.get("title"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("$A$1:$L$1"));

        //header row
        Row headerRow = sheet.createRow(1);
        headerRow.setHeightInPoints(40);

        //get model titles
        int i = 0;
        for (Map.Entry<String, String> entry: CaseModel.FIELD_NAMES.entrySet()) {
            Cell headerCell = headerRow.createCell(i);
            headerCell.setCellValue(entry.getValue());
            //headerCell.setCellStyle(styles.get("header"));
            ++i;
        }

        //save to file
        FileOutputStream out = new FileOutputStream(fileName);
        wb.write(out);
        out.close();
    }
}
