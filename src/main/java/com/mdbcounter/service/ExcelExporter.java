package com.mdbcounter.service;

import com.mdbcounter.logTest;
import com.mdbcounter.model.ComparisonResult;
import com.mdbcounter.model.TableCount;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook; // .xlsx 용
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ExcelExporter {
    private final String sheetName;
    private static final Logger log = LoggerFactory.getLogger(ExcelExporter.class);
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
        log.info("엑셀 저장 시간: " + (endTime - startTime) / 1000.0 + " sec");
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
            try {
                // 1. 없는 테이블 시트 생성
                Sheet missingTablesSheet = workbook.createSheet("없는 테이블");
                createMissingTablesSheet(missingTablesSheet, result.getMissingTables());
            } catch (Exception e) {
                throw new IOException("❌ 없는 테이블 시트 생성 중 오류 발생", e);
            }

            try {
                // 2. R_stream 없는 테이블 시트 생성
                Sheet missingKeysSheet = workbook.createSheet("R_stream 없는 테이블");
                createMissingKeysSheet(missingKeysSheet, result.getMissingKeys());
            } catch (Exception e) {
                throw new IOException("❌ R_stream 없는 테이블 시트 생성 중 오류 발생", e);
            }

            try {
                // 3. 개수 비교 시트 생성
                Sheet compareCntheet = workbook.createSheet("개수 비교");
                createcompareCntheet(compareCntheet, result.getcompareCnt());
            } catch (Exception e) {
                throw new IOException("❌ 개수 비교 시트 생성 중 오류 발생", e);
            }

            try (FileOutputStream out = new FileOutputStream(filePath)) {
                workbook.write(out);
            } catch (Exception e) {
                throw new IOException("❌ 엑셀 파일 저장 중 오류 발생", e);
            }

        } catch (IOException e) {
            // 로깅 도구가 있다면 log.error 사용
            log.error("엑셀 저장 실패: " + e.getMessage());
            throw e; // 다시 던져서 상위에서 처리할 수 있도록 함
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

    private void createcompareCntheet(Sheet sheet, List<ComparisonResult.CompareCntInfo> compareCnt) {
        CellStyle headerStyle = createHeaderStyle(sheet.getWorkbook());
        CellStyle dataStyle = createDataStyle(sheet.getWorkbook());
        // 헤더: 파일명, 테이블명, R_stream 값, MDB 개수, DB 개수, 차이, MDB 총계, DB 총계, 차이, 비율
        Row headerRow = sheet.createRow(0);
        String[] headers = {"파일명", "테이블명", "R_stream 값", "MDB 개수", "DB 개수", "차이"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        String[] calHeaders = {"MDB 총계", "DB 총계", "차이", "비율"};
        for (int i = 0; i < calHeaders.length; i++) {
            Cell cell = headerRow.createCell(i+headers.length);
            cell.setCellValue(calHeaders[i]);
            cell.setCellStyle(headerStyle);
        }

        // 데이터가 없는 경우 처리
        if (compareCnt.isEmpty()) {
            // 컬럼 너비 자동조정
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            return;
        }

        // 데이터 넣기.
        int dataRowIdx = 1;
        for (ComparisonResult.CompareCntInfo info : compareCnt) {
            Row row = sheet.createRow(dataRowIdx++);
            row.createCell(0).setCellValue(info.getMdbFileName());
            row.createCell(1).setCellValue(info.getTableName());
            row.createCell(2).setCellValue(info.getRStreamValue());
            row.createCell(3).setCellValue(info.getMdbCount());
            row.createCell(4).setCellValue(info.getDbCount());
            row.createCell(5).setCellValue(info.getDifference());

            for (int i = 0; i < 6; i++) {
                row.getCell(i).setCellStyle(dataStyle);
            }
        }

        // 컬럼 너비 자동조정
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

       // 함수 행 선택 -> 추가에서 선택으로 변경해버렸슈~
        Row functionRow = sheet.getRow(1);

        // MDB 총계 함수 (D열 합계)
        Cell mdbTotalCell = functionRow.createCell(6);
        mdbTotalCell.setCellFormula("SUM(D:D)");
        mdbTotalCell.setCellStyle(dataStyle);

        // DB 총계 함수 (E열 합계)
        Cell dbTotalCell = functionRow.createCell(7);
        dbTotalCell.setCellFormula("SUM(E:E)");
        dbTotalCell.setCellStyle(dataStyle);

        // 차이 총계 함수 (F열 합계)
        Cell diffTotalCell = functionRow.createCell(8);
        diffTotalCell.setCellFormula("SUM(F:F)");
        diffTotalCell.setCellStyle(dataStyle);

        // 비율 계산 함수 (차이/MDB 총계)
        Cell ratioCell = functionRow.createCell(9);
        ratioCell.setCellFormula("ROUND(IF(G2=0,0,I2/G2)*100,2) & \"%\"");
        ratioCell.setCellStyle(dataStyle);

        // 컬럼 너비 자동조정
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        makeCondition(sheet, "F2", ">", 0, "F2", "F" + (dataRowIdx-1),"CORAL", "BLACK" );
        makeCondition(sheet, "F2", "=", 0, "F2", "F" + (dataRowIdx-1),"LEMON_CHIFFON", "BLACK" );
        makeCondition(sheet, "F2", "<", 0, "F2", "F" + (dataRowIdx-1),"LIGHT_GREEN", "BLACK" );

    }

    private void makeCondition(Sheet sheet, String rowName, String condition, int conditionNum, String startCell, String endCell,
                               String backColor, String textColor) {
        SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();

        String rule = rowName + condition + conditionNum;
        ConditionalFormattingRule greaterThanZeroRule = sheetCF.createConditionalFormattingRule(rule);

        PatternFormatting fill = greaterThanZeroRule.createPatternFormatting();
        fill.setFillBackgroundColor(getColorIndex(backColor));

        FontFormatting font = greaterThanZeroRule.createFontFormatting();
        font.setFontColorIndex(getColorIndex(textColor));

        CellRangeAddress[] regions = {
                CellRangeAddress.valueOf(startCell + ":" + endCell)
        };
        sheetCF.addConditionalFormatting(regions, greaterThanZeroRule);
    }

    private short getColorIndex(String colorName) {
        try {
            return IndexedColors.valueOf(colorName.toUpperCase()).getIndex();
        } catch (IllegalArgumentException e) {
            log.info("없는 색상의 엑셀 color 설정:{}",colorName);
            return IndexedColors.AUTOMATIC.getIndex(); // 기본값
        }
    }
}