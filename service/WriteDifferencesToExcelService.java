package jsoncomparison.service;

import jsoncomparison.domain.DifferencesVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static jsoncomparison.constants.JsonCompareConstants.*;

@Service
@Slf4j
public class WriteDifferencesToExcelService {

    /*
    write all the differences into the Excel file and save it
     */
    public byte[] writeDifferencesToExcel(List<DifferencesVO> differences) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(WORKBOOK_NAME);

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue(UNIQUE_ID);
            headerRow.createCell(1).setCellValue(FIELD);
            headerRow.createCell(2).setCellValue(DIFFERENCES);
            headerRow.createCell(3).setCellValue(DATABASE_1_VALUE);
            headerRow.createCell(4).setCellValue(DATABASE_2_VALUE);

            int rowNumber = 1;
            for (DifferencesVO difference : differences) {
                Row row = sheet.createRow(rowNumber++);
                row.createCell(0).setCellValue(difference.getUniqueNumber());
                row.createCell(1).setCellValue(difference.getJsonTag());
                row.createCell(2).setCellValue(difference.getDescription());
                row.createCell(3).setCellValue(difference.getDatabase1Value());
                row.createCell(4).setCellValue(difference.getDatabase2Value());
            }
            workbook.write(outputStream);
            log.info("Differences saved to Excel file...");
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Error while creating workbook or writing to Excel: {}", e.getMessage(), e);
            throw new IOException(e.getMessage(),e);
        }
    }
    }








