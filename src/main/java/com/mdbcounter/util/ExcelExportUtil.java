package com.mdbcounter.util;

import com.mdbcounter.domain.dto.ComparisonResult;
import com.mdbcounter.model.TableCount;
import com.mdbcounter.service.ExcelExporterService;
import com.mdbcounter.view.ConsoleView;

import java.io.File;
import java.util.List;

/**
 * 엑셀 저장 공통 로직을 담당하는 유틸리티 클래스
 */
public class ExcelExportUtil {
    
    /**
     * 테이블 카운트 데이터를 엑셀로 저장
     * @param view 콘솔 뷰
     * @param allTableCounts 저장할 테이블 카운트 데이터
     */
    public static void exportTableCountToExcel(ConsoleView view, List<TableCount> allTableCounts) {
        File excelDir = UserInputUtil.getValidDirectoryFromUser(view, "엑셀 파일을 저장할 폴더 경로를 입력하세요: ");
        if (excelDir == null) return;
        
        String excelPath = ExcelExporterService.getTableCntExcelPath(excelDir);
        view.printMessage("엑셀 파일을 저장 중입니다. 잠시만 기다려주세요...");
        
        long excelStart = System.currentTimeMillis();
        ExcelExporterService.exportTableCntExcel(allTableCounts, excelPath);
        view.printLoadingTime(excelStart,"엑셀 저장 시간: ");
    }
    
    /**
     * 비교 결과를 엑셀로 저장
     * @param view 콘솔 뷰
     * @param result 저장할 비교 결과
     */
    public static void exportComparisonToExcel(ConsoleView view, ComparisonResult result) {
        File excelDir = UserInputUtil.getValidDirectoryFromUser(view, "엑셀 파일을 저장할 폴더 경로를 입력하세요: ");
        if (excelDir == null) return;

        String excelPath = ExcelExporterService.getCompareExcelPath(excelDir);
        view.printMessage("엑셀 파일을 저장 중입니다. 잠시만 기다려주세요...");
        
        long excelStart = System.currentTimeMillis();
        ExcelExporterService.exportComparisonToExcel(result, excelPath);
        view.printLoadingTime(excelStart, "엑셀 저장 시간: ");
    }
} 