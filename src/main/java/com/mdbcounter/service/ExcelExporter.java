package com.mdbcounter.service;

import com.mdbcounter.model.ColumnCount;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;

/**
 * MDB 파일의 테이블별 컬럼 데이터 개수를 Excel 파일로 내보내는 클래스
 * 여러 MDB 파일을 처리할 때, 각 파일명별로 시트를 생성합니다.
 */
public class ExcelExporter {
    /**
     * 여러 파일의 결과를 받아 파일명별로 시트를 생성하고, 각 시트에 테이블별 컬럼 카운트 구조를 출력합니다.
     * @param fileTableMap 파일명(시트명)별 테이블별 컬럼카운트 Map
     * @param filePath 저장할 Excel 파일 경로
     * @throws IOException 파일 저장 중 오류 발생 시
     */
    public void export(Map<String, Map<String, List<ColumnCount>>> fileTableMap, String filePath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            long startTime = System.currentTimeMillis();

            // 1. 총계 시트 생성 (모든 컬럼명 한 줄, 합계 한 줄)
            Sheet totalSheet = workbook.createSheet("총계");
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setAlignment(HorizontalAlignment.CENTER);
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            // 1-1. 모든 컬럼명 중복 없이 수집 + 합계 계산을 한 번에
            LinkedHashMap<String, Integer> columnTotalMap = new LinkedHashMap<>();
            for (Map<String, List<ColumnCount>> tableMap : fileTableMap.values()) {
                for (List<ColumnCount> columns : tableMap.values()) {
                    for (ColumnCount cc : columns) {
                        columnTotalMap.putIfAbsent(cc.getColumnName(), 0);
                        columnTotalMap.put(cc.getColumnName(), columnTotalMap.get(cc.getColumnName()) + cc.getCount());
                    }
                }
            }
            // 1-2. 컬럼명 한 줄
            Row headerRow = totalSheet.createRow(0);
            int colIdx = 0;
            for (String col : columnTotalMap.keySet()) {
                Cell cell = headerRow.createCell(colIdx++);
                cell.setCellValue(col);
                cell.setCellStyle(headerStyle);
            }
            // 1-3. 합계 한 줄
            Row totalRow = totalSheet.createRow(1);
            colIdx = 0;
            for (String col : columnTotalMap.keySet()) {
                Cell cell = totalRow.createCell(colIdx++);
                cell.setCellValue(columnTotalMap.get(col));
                cell.setCellStyle(dataStyle);
            }
            for (int i = 0; i < columnTotalMap.size(); i++) totalSheet.autoSizeColumn(i);

            // 2. 파일별 시트 생성
            for (Map.Entry<String, Map<String, List<ColumnCount>>> fileEntry : fileTableMap.entrySet()) {
                String sheetName = fileEntry.getKey();
                Map<String, List<ColumnCount>> tableMap = fileEntry.getValue();
                Sheet sheet = workbook.createSheet(sheetName);
                // 스타일 재사용
                int rowNum = 0;
                for (Map.Entry<String, List<ColumnCount>> entry : tableMap.entrySet()) {
                    String tableName = entry.getKey();
                    List<ColumnCount> columns = entry.getValue();
                    // 테이블명 헤더
                    Row tableRow = sheet.createRow(rowNum++);
                    Cell tableCell = tableRow.createCell(0);
                    tableCell.setCellValue("[" + tableName + "]");
                    tableCell.setCellStyle(headerStyle);
//                    sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum-1, rowNum-1, 0, columns.size() - 1));
                    // 컬럼명 행
                    Row columnRow = sheet.createRow(rowNum++);
                    for (int i = 0; i < columns.size(); i++) {
                        Cell cell = columnRow.createCell(i);
                        cell.setCellValue(columns.get(i).getColumnName());
                        cell.setCellStyle(headerStyle);
                    }
                    // 데이터 개수 행
                    Row countRow = sheet.createRow(rowNum++);
                    for (int i = 0; i < columns.size(); i++) {
                        Cell cell = countRow.createCell(i);
                        cell.setCellValue(columns.get(i).getCount());
                        cell.setCellStyle(dataStyle);
                    }
                    rowNum++;
                }
                for (int i = 0; i < 20; i++) {
                    sheet.autoSizeColumn(i);
                }
            }
            // 파일 저장
            try (FileOutputStream out = new FileOutputStream(filePath)) {
                workbook.write(out);
            }
            long endTime = System.currentTimeMillis();
            long timeElapsed = endTime - startTime;
            System.out.println("엑셀 저장 시간:"+timeElapsed/1000+"sec");
        }
    }
} 