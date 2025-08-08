package com.mdbcounter.service;

import com.mdbcounter.model.ComparisonResult;
import com.mdbcounter.model.TableCount;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ExcelExporterService {
    private static final Logger log = LoggerFactory.getLogger(ExcelExporterService.class);

    private static final String DATE_FORMAT = "yyyyMMdd_HHmm";
    // 헤더 상수
    private static final String[] COUNT_TABLE_HEADERS = {"테이블명", "데이터 개수"};
    private static final String[] MISSING_TABLE_HEADERS = {"파일명", "테이블명"};
    private static final String[] MISSING_KEY_HEADERS = {"파일명", "테이블명", "키값"};
    private static final String[] COMPARE_CNT_HEADERS = {"파일명", "테이블명", "R_stream 값", "MDB 개수", "DB 개수", "차이"};
    private static final String[] CALCULATE_HEADERS = {" MDB 총계 ", " DB 총계 ", "차이 총계", "누락 비율"};

    // 스타일 상수
    private static final short HEADER_FONT_SIZE = 12;
    private static final int DATA_COLUMN_COUNT = 6;

    // 시트명 상수
    private static final String COUNT_TABLE_SHEET = "테이블 총계";
    private static final String MISSING_TABLES_SHEET = "없는 테이블";
    private static final String MISSING_KEYS_SHEET = "R_stream 없는 테이블";
    private static final String COMPARE_CNT_SHEET = "개수 비교";

    /**
     * 테이블 개수 데이터를 엑셀로 저장
     */
    public void exportCntTable(List<TableCount> data, String filePath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            createCntSheet(workbook, data);
            saveWorkbook(workbook, filePath);

        } catch (Exception e) {
            throw new IOException("테이블 개수 엑셀 저장 실패: " + e.getMessage(), e);
        }
    }

    private void createCntSheet (Workbook workbook, List<TableCount> data) throws IOException{
        try {
            Sheet sheet = workbook.createSheet(COUNT_TABLE_SHEET);

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            // 헤더 생성
            createHeaderRow(sheet, COUNT_TABLE_HEADERS, headerStyle);

            // 데이터 입력
            int rowIdx = 1;
            for (TableCount cc : data) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(cc.getTableName());
                row.createCell(1).setCellValue(cc.getCount());
                applyCellStyles(row, COUNT_TABLE_HEADERS.length, dataStyle);
            }

            autoSizeColumns(sheet, COUNT_TABLE_HEADERS.length);
        } catch (Exception e) {
            throw new IOException("없는 테이블 시트 저장 실패: " + e.getMessage(), e);
        }
    }

    /**
     * MDB와 DB 비교 결과를 엑셀로 저장
     */
    public void exportComparisonResult(ComparisonResult result, String filePath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            createMissingTablesSheet(workbook, result.getMissingTables());
            createMissingKeysSheet(workbook, result.getMissingKeys());
            createCompareCntSheet(workbook, result.getcompareCnt());

            saveWorkbook(workbook, filePath);

        } catch (Exception e) {
            throw new IOException("비교 결과 엑셀 저장 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 없는 테이블 시트 생성
     */
    private void createMissingTablesSheet(Workbook workbook, List<ComparisonResult.MissingTableInfo> missingTables) throws IOException {

        try {
            Sheet sheet = workbook.createSheet(MISSING_TABLES_SHEET);

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            createHeaderRow(sheet, MISSING_TABLE_HEADERS, headerStyle);

            int rowIdx = 1;
            for (ComparisonResult.MissingTableInfo info : missingTables) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(info.getMdbFileName());
                row.createCell(1).setCellValue(info.getTableName());
                applyCellStyles(row, MISSING_TABLE_HEADERS.length, dataStyle);
            }

            autoSizeColumns(sheet, MISSING_TABLE_HEADERS.length);
        } catch (Exception e) {
            throw new IOException("없는 테이블 시트 저장 실패: " + e.getMessage(), e);
        }
    }

    /**
     * R_stream 없는 테이블 시트 생성
     */
    private void createMissingKeysSheet(Workbook workbook, List<ComparisonResult.MissingKeyInfo> missingKeys) throws IOException {
        try {
            Sheet sheet = workbook.createSheet(MISSING_KEYS_SHEET);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            createHeaderRow(sheet, MISSING_KEY_HEADERS, headerStyle);

            int rowIdx = 1;
            for (ComparisonResult.MissingKeyInfo info : missingKeys) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(info.getMdbFileName());
                row.createCell(1).setCellValue(info.getTableName());
                row.createCell(2).setCellValue(info.getRStreamValue());
                applyCellStyles(row, MISSING_KEY_HEADERS.length, dataStyle);
            }
            autoSizeColumns(sheet, MISSING_KEY_HEADERS.length);
        } catch (Exception e) {
            throw new IOException("R_stream 없는 테이블 시트 저장 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 개수 비교 시트 생성
     */
    private void createCompareCntSheet(Workbook workbook, List<ComparisonResult.CompareCntInfo> compareCnt) throws IOException {
        try {
            Sheet sheet = workbook.createSheet(COMPARE_CNT_SHEET);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            // 기본 헤더 + 계산 헤더 생성
            createCompareCntHeaders(sheet, headerStyle);

            if (compareCnt.isEmpty()) {
                autoSizeColumns(sheet, COMPARE_CNT_HEADERS.length + CALCULATE_HEADERS.length);
                return;
            }

            // 데이터 입력
            int dataRowIdx = 1;
            for (ComparisonResult.CompareCntInfo info : compareCnt) {
                Row row = sheet.createRow(dataRowIdx++);
                row.createCell(0).setCellValue(info.getMdbFileName());
                row.createCell(1).setCellValue(info.getTableName());
                row.createCell(2).setCellValue(info.getRStreamValue());
                row.createCell(3).setCellValue(info.getMdbCount());
                row.createCell(4).setCellValue(info.getDbCount());
                row.createCell(5).setCellValue(info.getDifference());

                applyCellStyles(row, DATA_COLUMN_COUNT, dataStyle);
            }

            // 합계 함수 추가
            addSummaryFormulas(sheet, dataStyle);

            // 조건부 서식 적용
            applyConditionalFormatting(sheet, dataRowIdx);

            autoSizeColumns(sheet, COMPARE_CNT_HEADERS.length + CALCULATE_HEADERS.length);
        } catch (Exception e) {
            log.error("비교 결과 엑셀 저장 실패: {}", e.getMessage());
            throw new IOException("비교 결과 엑셀 저장 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 비교 개수 시트의 헤더 생성
     */
    private void createCompareCntHeaders(Sheet sheet, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);

        // 기본 헤더
        for (int i = 0; i < COMPARE_CNT_HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(COMPARE_CNT_HEADERS[i]);
            cell.setCellStyle(headerStyle);
        }

        // 계산 헤더
        for (int i = 0; i < CALCULATE_HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i + COMPARE_CNT_HEADERS.length);
            cell.setCellValue(CALCULATE_HEADERS[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    /**
     * 보기 편하게 함수 로직 추가
     */
    private void addSummaryFormulas(Sheet sheet, CellStyle dataStyle) {
        Row functionRow = sheet.getRow(1);
        if (functionRow == null) return;

        // MDB 총계 (D열 합계)
        Cell mdbTotalCell = functionRow.createCell(6);
        mdbTotalCell.setCellFormula("SUM(D:D)");
        mdbTotalCell.setCellStyle(dataStyle);

        // DB 총계 (E열 합계)
        Cell dbTotalCell = functionRow.createCell(7);
        dbTotalCell.setCellFormula("SUM(E:E)");
        dbTotalCell.setCellStyle(dataStyle);

        // 차이 총계 (F열 합계)
        Cell diffTotalCell = functionRow.createCell(8);
        diffTotalCell.setCellFormula("SUM(F:F)");
        diffTotalCell.setCellStyle(dataStyle);

        // 비율 계산 (차이/MDB 총계)
        Cell ratioCell = functionRow.createCell(9);
        ratioCell.setCellFormula("ROUND(IF(G2=0,0,I2/G2)*100,2) & \"%\"");
        ratioCell.setCellStyle(dataStyle);
    }

    /**
     * 조건부 서식
     */
    private void applyConditionalFormatting(Sheet sheet, int lastRowIdx) {
        String endCell = "F" + (lastRowIdx - 1);

        // 양수: CORAL 배경
        applyConditionalFormattingRule(sheet, "F2>0", "F2:" + endCell, "CORAL", "BLACK");
        // 0: LEMON_CHIFFON 배경
        applyConditionalFormattingRule(sheet, "F2=0", "F2:" + endCell, "LEMON_CHIFFON", "BLACK");
        // 음수: LIGHT_GREEN 배경
        applyConditionalFormattingRule(sheet, "F2<0", "F2:" + endCell, "LIGHT_GREEN", "BLACK");
    }

    /**
     * 조건부 서식 규칙 적용
     */
    private void applyConditionalFormattingRule(Sheet sheet, String rule, String range,
                                                String backColor, String textColor) {
        SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();
        ConditionalFormattingRule formatRule = sheetCF.createConditionalFormattingRule(rule);

        PatternFormatting fill = formatRule.createPatternFormatting();
        fill.setFillBackgroundColor(getColorIndex(backColor));

        FontFormatting font = formatRule.createFontFormatting();
        font.setFontColorIndex(getColorIndex(textColor));

        CellRangeAddress[] regions = {CellRangeAddress.valueOf(range)};
        sheetCF.addConditionalFormatting(regions, formatRule);
    }

    /**
     * 헤더 행
     */
    private void createHeaderRow(Sheet sheet, String[] headers, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    /**
     * 워크북 저장
     */
    private void saveWorkbook(Workbook workbook, String filePath) throws IOException {
        try (FileOutputStream out = new FileOutputStream(filePath)) {
            workbook.write(out);
        }
    }

    public static void exportTableCntExcel(List<TableCount> tableCounts, String excelPath) {
        try {
            ExcelExporterService exporter = new ExcelExporterService();
            exporter.exportCntTable(tableCounts, excelPath);
            log.info("엑셀 파일이 성공적으로 저장되었습니다: " + excelPath);
        } catch (Exception e) {
            log.error("엑셀 저장 중 오류: " + e.getMessage());
        }

    }

    public static void exportComparisonToExcel(ComparisonResult result, String excelPath) {
        try {
            ExcelExporterService exporter = new ExcelExporterService();
            exporter.exportComparisonResult(result, excelPath);
            log.info("비교 결과 엑셀 파일이 성공적으로 저장되었습니다: " + excelPath);
        } catch (Exception e) {
            log.error("엑셀 저장 중 오류: " + e.getMessage());
        }
    }

    /**
     * 테이블 개수용 엑셀, 경로 받아서 파일명 생성.
     * @param excelDir
     * @return
     */
    public static String getTableCntExcelPath(File excelDir) {
        String date = new SimpleDateFormat(DATE_FORMAT).format(new Date());
        return new File(excelDir, date + "-mdb테이블총계.xlsx").getAbsolutePath();
    }

    /**
     * 비교 결과용 엑셀, 경로 받아서 파일명 생성.
     * @param excelDir
     * @return
     */
    public static String getCompareExcelPath(File excelDir) {
        String date = new SimpleDateFormat(DATE_FORMAT).format(new Date());
        return new File(excelDir, date + "-mdb_db_비교결과.xlsx").getAbsolutePath();
    }

    /** 디자인 관련 =======================================================================

    /**
     * 행의 셀들에 스타일
     */
    private void applyCellStyles(Row row, int cellCount, CellStyle style) {
        for (int i = 0; i < cellCount; i++) {
            Cell cell = row.getCell(i);
            if (cell != null) {
                cell.setCellStyle(style);
            }
        }
    }

    /**
     * 컬럼 너비 자동 조정
     */
    private void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }
    /**
     * 헤더 스타일 생성
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints(HEADER_FONT_SIZE);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        setBorders(style);
        return style;
    }

    /**
     * 데이터 스타일 생성
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        setBorders(style);
        return style;
    }

    /**
     * 셀 테두리 설정 (공통 로직)
     */
    private void setBorders(CellStyle style) {
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    /**
     * 색상 인덱스 반환
     */
    private short getColorIndex(String colorName) {
        try {
            return IndexedColors.valueOf(colorName.toUpperCase()).getIndex();
        } catch (IllegalArgumentException e) {
            log.warn("지원하지 않는 색상: {}, 기본값으로 설정", colorName);
            return IndexedColors.AUTOMATIC.getIndex();
        }
    }
}