package org.inventory.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.inventory.model.Product;
import org.inventory.model.Sale;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ExcelExporter {

    public void exportInventory(List<Product> products, String filePath) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Inventory");

            // Header
            Row header = sheet.createRow(0);
            String[] columns = { "ID", "SKU", "Name", "Category", "Stock", "Cost", "Price" };
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            // Data
            int rowNum = 1;
            for (Product p : products) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(p.getId());
                row.createCell(1).setCellValue(p.getSku());
                row.createCell(2).setCellValue(p.getName());
                row.createCell(3).setCellValue(p.getCategory());
                row.createCell(4).setCellValue(p.getStockQuantity());
                row.createCell(5).setCellValue(p.getCostPrice() != null ? p.getCostPrice().doubleValue() : 0);
                row.createCell(6).setCellValue(p.getSalesPrice() != null ? p.getSalesPrice().doubleValue() : 0);
            }

            // Auto size
            for (int i = 0; i < columns.length; i++)
                sheet.autoSizeColumn(i);

            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
