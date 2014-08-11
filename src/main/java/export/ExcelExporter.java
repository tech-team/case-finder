package export;

import caseloader.CaseSearchRequest;
import gui.casestable.CaseModel;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import util.HypertextNode;
import util.HypertextParser;
import util.ResourceControl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ExcelExporter {
    private enum CellType {
        TEXT, BOLD, LINK
    }

    private Extension extension = null;
    Workbook wb = null;
    CreationHelper ch = null;
    Map<CellType, CellStyle> styles = null;

    public ExcelExporter(Extension extension) throws UnsupportedExtensionException {
        this.extension = extension;

        try {
            wb = (Workbook) extension.getWorkbookClass().newInstance();
            styles = createStyles(wb);
            ch = wb.getCreationHelper();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new UnsupportedExtensionException(e);
        }
    }

    public void export(CaseSearchRequest request, ObservableList<CaseModel> data, String fileName) throws ExportException {
        //change fileName's extension to selected extension
        fileName = fileName.replaceFirst(
                "\\.xlsx?$",
                extension.getStarlessValue());

        ResourceBundle res = ResourceBundle.getBundle("properties.export_strings", new ResourceControl("UTF-8"));

        saveData(data, res);
        saveRequest(request, res);

        //save to file
        try (FileOutputStream out = new FileOutputStream(fileName)) {
            wb.write(out);
        }
        catch (IOException e) {
            throw new ExportException(e);
        }
    }

    private void saveData(ObservableList<CaseModel> data, ResourceBundle res) throws ExportException {
        Sheet sheet = wb.createSheet(res.getString("dataSheetName"));
        PrintSetup printSetup = sheet.getPrintSetup();
        printSetup.setLandscape(true);
        sheet.setFitToPage(true);
        sheet.setHorizontallyCenter(true);

        int rowId = 0;

        //title row
        Row titleRow = sheet.createRow(rowId++);

        //get model titles
        int cellId = 0;
        for (Map.Entry<String, String> entry: CaseModel.FIELD_NAMES.entrySet()) {
            Cell titleCell = titleRow.createCell(cellId++);
            titleCell.setCellStyle(styles.get(CellType.BOLD));
            titleCell.setCellValue(entry.getValue());
        }

        for (int i = titleRow.getFirstCellNum(); i < titleRow.getLastCellNum(); ++i)
            sheet.autoSizeColumn(i);


        for (CaseModel caseModel: data) {
            cellId = 0;

            Row row = sheet.createRow(rowId++);
            int linesCount = 1;

            try {
                for (Map.Entry<String, String> entry : CaseModel.FIELD_NAMES.entrySet()) {
                    Cell cell = row.createCell(cellId++);

                    Field field = CaseModel.class.getDeclaredField(entry.getKey());
                    Object obj = field.get(caseModel);

                    if (obj instanceof StringProperty) {
                        String value = ((StringProperty) obj).get();
                        fillStringCell(cell, value);

                        int breaksCount = StringUtils.countMatches(value, "\n");
                        if (breaksCount + 1 > linesCount)
                            linesCount = breaksCount + 1;

                    } else if (obj instanceof IntegerProperty) {
                        int value = ((IntegerProperty) obj).get();
                        cell.setCellValue(value);
                    } else if (obj instanceof DoubleProperty) {
                        double value = ((DoubleProperty) obj).get();
                        cell.setCellValue(value);
                    }
                }

                row.setHeightInPoints(linesCount * sheet.getDefaultRowHeightInPoints());
            }
            catch (NoSuchFieldException|IllegalAccessException e) {
                throw new ExportException(e);
            }
        }
    }

    private void fillStringCell(Cell cell, String value) {
        List<HypertextNode> nodes = HypertextParser.parse(value);

        if (nodes.size() > 1) //multiple links in one cell are not supported by Excel unfortunately
            cell.setCellValue(value);
        else if (nodes.size() == 1) {
            HypertextNode node = nodes.get(0);

            if (node.getType() == HypertextNode.Type.LINK) {
                Hyperlink link = ch.createHyperlink(Hyperlink.LINK_URL);
                link.setAddress(value);

                cell.setCellValue(value);
                cell.setHyperlink(link);
                cell.setCellStyle(styles.get(CellType.LINK));
            }
            else {
                cell.setCellValue(value);
                cell.setCellStyle(styles.get(CellType.TEXT));
            }
        }
    }

    private void saveRequest(CaseSearchRequest request, ResourceBundle res) {
        Sheet sheet = wb.createSheet(res.getString("requestSheetName"));
        PrintSetup printSetup = sheet.getPrintSetup();
        printSetup.setLandscape(true);
        sheet.setFitToPage(true);
        sheet.setHorizontallyCenter(true);

        int rowId = 0;

        String[] courts = request.getCourtsNames();
        String courtsString = StringUtils.join(courts, ", ");
        createKeyValueRow(sheet, styles, rowId++,
                res.getString("requestCourt"), courtsString);

        CaseSearchRequest.CaseType caseType = request.getCaseType();
        String caseTypeStr = "";
        if (caseType != null)
            caseTypeStr = res.getString(caseType.toString());

        createKeyValueRow(sheet, styles, rowId++,
                res.getString("requestType"), caseTypeStr);

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

        String minCost = Long.toString(request.getMinCost());
        createKeyValueRow(sheet, styles, rowId++,
                res.getString("requestMinimalCost"), minCost);

        String searchLimit = Integer.toString(request.getSearchLimit());
        createKeyValueRow(sheet, styles, rowId,
                res.getString("requestSearchLimit"), searchLimit);

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createKeyValueRow(Sheet sheet, Map<CellType, CellStyle> styles, int rowId, String key, String value) {
        Row row = sheet.createRow(rowId);
        Cell keyCell = row.createCell(0);
        keyCell.setCellValue(key);
        keyCell.setCellStyle(styles.get(CellType.BOLD));

        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);
    }

    private Map<CellType, CellStyle> createStyles(Workbook wb) {
        Map<CellType, CellStyle> styles = new HashMap<>();

        CellStyle textStyle = wb.createCellStyle();
        textStyle.setWrapText(true);
        styles.put(CellType.TEXT, textStyle);
        
        CellStyle boldStyle = wb.createCellStyle();
        boldStyle.setWrapText(true);
        Font boldFont = wb.createFont();
        boldFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        boldStyle.setFont(boldFont);
        styles.put(CellType.BOLD, boldStyle);

        CellStyle linkStyle = wb.createCellStyle();
        linkStyle.setWrapText(true);
        Font linkFont = wb.createFont();
        linkFont.setUnderline(Font.U_SINGLE);
        linkFont.setColor(IndexedColors.BLUE.getIndex());
        linkStyle.setFont(linkFont);
        styles.put(CellType.LINK, linkStyle);

        return styles;
    }
}
