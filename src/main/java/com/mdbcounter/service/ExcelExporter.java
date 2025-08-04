package com.mdbcounter.service;

import com.mdbcounter.model.ComparisonResult;
import com.mdbcounter.model.TableCount;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook; // .xlsx 용
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ExcelExporter {
    private final String sheetName;

    private ExcelExporter(Builder builder) {
        this.sheetName = builder.sheetName;
    }

    public static class Builder {
        private String sheetName = "총계";
        public Builder sheetName(String name) {
            this.sheetName = name;
            return this;
        }
        public ExcelExporter build() {
            return new ExcelExporter(this);
        }
    }

    // 테이블 총 cnt 반환
    public void exportCntTable(List<TableCount> data, String filePath) throws IOException {
        long startTime = System.currentTimeMillis();
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(sheetName);
            // 스타일 생성
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            // 헤더: 테이블명, 데이터 개수
            Row headerRow = sheet.createRow(0);
            String[] headers = {"테이블명", "데이터 개수"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            // 데이터
            int rowIdx = 1;
            for (TableCount cc : data) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(cc.getTableName());
                row.createCell(1).setCellValue(cc.getCount());
                for (int i = 0; i < 2; i++) row.getCell(i).setCellStyle(dataStyle);
            }
            // 컬럼 너비 자동조정
            for (int i = 0; i < 2; i++) sheet.autoSizeColumn(i);
            try (FileOutputStream out = new FileOutputStream(filePath)) {
                workbook.write(out);
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("엑셀 저장 시간: " + (endTime - startTime) / 1000.0 + " sec");
    }

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

    /**
     * MDB와 DB 비교 결과를 엑셀로 저장
     */
    public void exportComparisonResult(ComparisonResult result, String filePath) throws IOException {

        try (Workbook workbook = new XSSFWorkbook()) {

            // 1. 없는 테이블 시트
            Sheet missingTablesSheet = workbook.createSheet("없는 테이블");
            createMissingTablesSheet(missingTablesSheet, result.getMissingTables());

            // 2. 없는 키 시트
            Sheet missingKeysSheet = workbook.createSheet("R_stream 없는 테이블");
            createMissingKeysSheet(missingKeysSheet, result.getMissingKeys());

            // 3. 개수 비교 시트
            Sheet countComparisonSheet = workbook.createSheet("개수 비교");
            createCountComparisonSheet(countComparisonSheet, result.getCountComparisons());

            try (FileOutputStream out = new FileOutputStream(filePath)) {
                workbook.write(out);
            }
        }

    }

    private void createMissingTablesSheet(Sheet sheet, List<ComparisonResult.MissingTableInfo> missingTables) {
        CellStyle headerStyle = createHeaderStyle(sheet.getWorkbook());
        CellStyle dataStyle = createDataStyle(sheet.getWorkbook());
        // 헤더: 파일명, 테이블명
        Row headerRow = sheet.createRow(0);
        String[] headers = {"파일명", "테이블명"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        // 데이터
        int rowIdx = 1;
        for (ComparisonResult.MissingTableInfo info : missingTables) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(info.getMdbFileName());
            row.createCell(1).setCellValue(info.getTableName());
            for (int i = 0; i < headers.length; i++) row.getCell(i).setCellStyle(dataStyle);
        }
        // 컬럼 너비 자동조정
        for (int i = 0; i < 2; i++) sheet.autoSizeColumn(i);
    }

    private void createMissingKeysSheet(Sheet sheet, List<ComparisonResult.MissingKeyInfo> missingKeys) {
        CellStyle headerStyle = createHeaderStyle(sheet.getWorkbook());
        CellStyle dataStyle = createDataStyle(sheet.getWorkbook());
        // 헤더: 파일명, 테이블명, 키값
        Row headerRow = sheet.createRow(0);
        String[] headers = {"파일명", "테이블명", "키값"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        // 파일 명 / 테이블 명 / 키 값
        int rowIdx = 1;
        for (ComparisonResult.MissingKeyInfo info : missingKeys) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(info.getMdbFileName());
            row.createCell(1).setCellValue(info.getTableName());
            row.createCell(2).setCellValue(info.getRStreamValue());
            for (int i = 0; i < headers.length; i++) row.getCell(i).setCellStyle(dataStyle);
        }
        // 컬럼 너비 자동조정
        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

    }

    private void createCountComparisonSheet(Sheet sheet, List<ComparisonResult.CountComparisonInfo> countComparisons) {
        CellStyle headerStyle = createHeaderStyle(sheet.getWorkbook());
        CellStyle dataStyle = createDataStyle(sheet.getWorkbook());
        // 헤더: 파일명, 테이블명, R_stream 값, MDB 개수, DB 개수, 차이
        Row headerRow = sheet.createRow(0);
        String[] headers = {"파일명", "테이블명", "R_stream 값", "MDB 개수", "DB 개수", "차이"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        // 데이터
        int rowIdx = 1;
        for (ComparisonResult.CountComparisonInfo info : countComparisons) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(info.getMdbFileName());
            row.createCell(1).setCellValue(info.getTableName());
            row.createCell(2).setCellValue(info.getRStreamValue());
            row.createCell(3).setCellValue(info.getMdbCount());
            row.createCell(4).setCellValue(info.getDbCount());
            row.createCell(5).setCellValue(info.getDifference());
            for (int i = 0; i < headers.length; i++) row.getCell(i).setCellStyle(dataStyle);
        }
        // 컬럼 너비 자동조정
        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
    }
} 