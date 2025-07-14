package com.mdbcounter.service;

import com.mdbcounter.model.ColumnCount;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

public class ExcelExporter {
    private final String totalSheetName;
    private final String dateFormat;
    private final boolean autoSizeColumn;

    private ExcelExporter(Builder builder) {
        this.totalSheetName = builder.totalSheetName;
        this.dateFormat = builder.dateFormat;
        this.autoSizeColumn = builder.autoSizeColumn;
    }

    public static class Builder {
        private String totalSheetName = "총계";
        private String dateFormat = "yyyyMMdd-HHmm";
        private boolean autoSizeColumn = true;

        public Builder totalSheetName(String name) {
            this.totalSheetName = name;
            return this;
        }
        public Builder dateFormat(String format) {
            this.dateFormat = format;
            return this;
        }
        public Builder autoSizeColumn(boolean auto) {
            this.autoSizeColumn = auto;
            return this;
        }
        public ExcelExporter build() {
            return new ExcelExporter(this);
        }
    }

    public void export(Map<String, Map<String, List<ColumnCount>>> fileTableMap, String filePath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            long startTime = System.currentTimeMillis();

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            // 1. 총계 시트 생성
            Sheet totalSheet = workbook.createSheet(totalSheetName);
            LinkedHashMap<String, Integer> columnTotalMap = collectColumnTotals(fileTableMap);
            createHeaderRow(totalSheet, columnTotalMap, headerStyle);
            createTotalRow(totalSheet, columnTotalMap, dataStyle);
            if (autoSizeColumn) autoSizeColumns(totalSheet, columnTotalMap.size());

            // 2. 파일별 시트 생성
            for (Map.Entry<String, Map<String, List<ColumnCount>>> fileEntry : fileTableMap.entrySet()) {
                String sheetName = sanitizeSheetName(fileEntry.getKey());
                Map<String, List<ColumnCount>> tableMap = fileEntry.getValue();
                Sheet sheet = workbook.createSheet(sheetName);
                int rowNum = 0;
                for (Map.Entry<String, List<ColumnCount>> entry : tableMap.entrySet()) {
                    String tableName = entry.getKey();
                    List<ColumnCount> columns = entry.getValue();
                    rowNum = createTableBlock(sheet, rowNum, tableName, columns, headerStyle, dataStyle);
                }
                if (autoSizeColumn && !tableMap.isEmpty()) {
                    int colCount = tableMap.values().iterator().next().size();
                    autoSizeColumns(sheet, colCount);
                }
            }
            try (FileOutputStream out = new FileOutputStream(filePath)) {
                workbook.write(out);
            }
            long endTime = System.currentTimeMillis();
            long timeElapsed = endTime - startTime;
            System.out.println("엑셀 저장 시간:"+timeElapsed/1000+"sec");
        }
    }

    // ====== 엑셀 관련 private 으로 정리 ======
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    private LinkedHashMap<String, Integer> collectColumnTotals(Map<String, Map<String, List<ColumnCount>>> fileTableMap) {
        LinkedHashMap<String, Integer> columnTotalMap = new LinkedHashMap<>();
        for (Map<String, List<ColumnCount>> tableMap : fileTableMap.values()) {
            for (List<ColumnCount> columns : tableMap.values()) {
                for (ColumnCount cc : columns) {
                    columnTotalMap.putIfAbsent(cc.getColumnName(), 0);
                    columnTotalMap.put(cc.getColumnName(), columnTotalMap.get(cc.getColumnName()) + cc.getCount());
                }
            }
        }
        return columnTotalMap;
    }
    private void createHeaderRow(Sheet sheet, LinkedHashMap<String, Integer> columnTotalMap, CellStyle style) {
        Row headerRow = sheet.createRow(0);
        int colIdx = 0;
        for (String col : columnTotalMap.keySet()) {
            Cell cell = headerRow.createCell(colIdx++);
            cell.setCellValue(col);
            cell.setCellStyle(style);
        }
    }
    private void createTotalRow(Sheet sheet, LinkedHashMap<String, Integer> columnTotalMap, CellStyle style) {
        Row totalRow = sheet.createRow(1);
        int colIdx = 0;
        for (String col : columnTotalMap.keySet()) {
            Cell cell = totalRow.createCell(colIdx++);
            cell.setCellValue(columnTotalMap.get(col));
            cell.setCellStyle(style);
        }
    }
    private int createTableBlock(Sheet sheet, int rowNum, String tableName, List<ColumnCount> columns, CellStyle headerStyle, CellStyle dataStyle) {
        Row tableRow = sheet.createRow(rowNum++);
        Cell tableCell = tableRow.createCell(0);
        tableCell.setCellValue("[" + tableName + "]");
        tableCell.setCellStyle(headerStyle);
        Row columnRow = sheet.createRow(rowNum++);
        for (int i = 0; i < columns.size(); i++) {
            Cell cell = columnRow.createCell(i);
            cell.setCellValue(columns.get(i).getColumnName());
            cell.setCellStyle(headerStyle);
        }
        Row countRow = sheet.createRow(rowNum++);
        for (int i = 0; i < columns.size(); i++) {
            Cell cell = countRow.createCell(i);
            cell.setCellValue(columns.get(i).getCount());
            cell.setCellStyle(dataStyle);
        }
        return rowNum + 1; // 한 줄 띄우기
    }
    private void autoSizeColumns(Sheet sheet, int colCount) {
        for (int i = 0; i < colCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    // sheet에 못쓰는 문자들 정리
    private String sanitizeSheetName(String name) {
        String sanitized = name.replaceAll("[\\\\/?*\\[\\]]", "_");
        return sanitized.length() > 31 ? sanitized.substring(0, 31) : sanitized;
    }
} 