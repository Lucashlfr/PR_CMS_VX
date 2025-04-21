package com.messdiener.cms.v3.shared.imports;

import com.messdiener.cms.v3.app.entities.table.CMSCell;
import com.messdiener.cms.v3.app.entities.table.CMSRow;
import com.messdiener.cms.v3.utils.time.CMSDate;
import com.messdiener.cms.v3.utils.time.DateUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class ExcelImport {

    public static List<CMSRow> readExcel(File file) throws IOException {
        List<CMSRow> rows = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0); // Erste Tabelle auslesen

            for (Row row : sheet) {
                List<CMSCell> cells = new ArrayList<>();
                for (Cell cell : row) {

                    String cellValue = getCellValue(cell);
                    if(isValidDate(cellValue)) {
                        CMSDate cmsDate = CMSDate.convert(cellValue, DateUtils.DateType.SIMPLE_GERMAN);
                        cellValue = cmsDate.convertTo(DateUtils.DateType.GERMAN);
                    }
                    cells.add(new CMSCell(cellValue));
                    if(cells.size() == 5)break;
                }

                if(cells.size() < 5)continue;
                if(cells.getFirst().getContent().isEmpty())continue;
                if(!new CMSRow(cells).check())continue;

                rows.add(new CMSRow(cells));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return rows;
    }

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    public static String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    Date date = cell.getDateCellValue();
                    if (cell.getCellStyle().getDataFormatString().contains("h") || cell.getCellStyle().getDataFormatString().contains("H")) {
                        yield timeFormat.format(date); // Uhrzeit
                    } else {
                        yield dateFormat.format(date); // Datum
                    }
                } else {
                    yield String.valueOf(cell.getNumericCellValue()); // Zahl
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            case BLANK -> "";
            default -> "UNKNOWN";
        };
    }

    public static File convertToFile(MultipartFile multipartFile) throws IOException {
        File tempFile = File.createTempFile("import", ".xlsx");
        multipartFile.transferTo(tempFile);
        return tempFile;
    }

    public static boolean isValidDate(String date) {
        String pattern = "E, dd.MM.yy";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.GERMAN);
        sdf.setLenient(false); // Strenge Prüfung aktivieren

        try {
            sdf.parse(date); // Versuch, das Datum zu parsen
            return true; // Gültiges Datum
        } catch (ParseException e) {
            return false; // Ungültiges Datum
        }
    }
}
