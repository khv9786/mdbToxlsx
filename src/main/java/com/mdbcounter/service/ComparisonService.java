package com.mdbcounter.service;

import com.mdbcounter.model.MdbTableInfo;
import com.mdbcounter.model.ComparisonResult;
import com.mdbcounter.view.ConsoleView;
import com.mdbcounter.util.UserInputUtil;
import com.mdbcounter.util.FileSearchUtil;
import com.mdbcounter.util.ExcelExportUtil;

import java.io.File;
import java.util.*;

public class ComparisonService implements startService {
    private final ConsoleView view;
    private final DbComparisonService dbComparisonService;

    public ComparisonService(ConsoleView view) {
        this.view = view;
        this.dbComparisonService = new DbComparisonService();
    }

    @Override
    public void execute() {
        // 1. MDB 파일 검색
        File mdbDir = UserInputUtil.getValidDirectoryFromUser(view, "MDB 파일이 있는 폴더 경로를 입력하세요: ");
        if (mdbDir == null) return;

        List<File> mdbFiles = FileSearchUtil.searchMdbFilesWithTime(view, mdbDir);
        if (mdbFiles.isEmpty()) {
            view.printMessage("해당 폴더 및 하위 폴더에 mdb 파일이 없습니다.");
            return;
        }

        if (!view.confirm("이 파일들로 진행할까요?")) return;

        // 2. MDB 데이터 로딩
        List<MdbTableInfo> allMdbTableInfos = loadMdbData(mdbFiles);

        // 3. DB와 비교
        ComparisonResult result = compareWithDatabase(allMdbTableInfos);

        // 4. 결과 출력 및 엑셀 저장
        displayAndExportResults(result);
    }



    /**
     * MDB 데이터 로딩
     */
    private List<MdbTableInfo> loadMdbData(List<File> mdbFiles) {
        view.printMessage("MDB 파일을 로딩중입니다.");
        
        long loadStart = System.currentTimeMillis();
        List<MdbTableInfo> allMdbTableInfos = new ArrayList<>();

        for (File mdb : mdbFiles) {
            List<MdbTableInfo> oneFileInfos = dbComparisonService.getMdbTableInfo(mdb);
            allMdbTableInfos.addAll(oneFileInfos);
        }

        long loadEnd = System.currentTimeMillis();
        view.printLoadingTime(loadStart, loadEnd, "MDB 로딩 시간: ");
        
        return allMdbTableInfos;
    }

    /**
     * DB와 비교
     */
    private ComparisonResult compareWithDatabase(List<MdbTableInfo> allMdbTableInfos) {
        view.printMessage("DB와 비교 중입니다. 잠시만 기다려주세요...");
        
        long compareStart = System.currentTimeMillis();
        ComparisonResult result = dbComparisonService.compareMdbWithDbOptimized(allMdbTableInfos);
        long compareEnd = System.currentTimeMillis();
        
        view.printLoadingTime(compareStart, compareEnd, "DB 비교 시간: ");
        
        return result;
    }

    /**
     * 결과 출력 및 엑셀 저장
     */
    private void displayAndExportResults(ComparisonResult result) {
        view.printMessage("=== 비교 결과 ===");
        view.printMessage("없는 테이블 개수: " + result.getMissingTables().size());
        view.printMessage("없는 키 개수: " + result.getMissingKeys().size());

        if (result.getMissingTables().isEmpty() && result.getMissingKeys().isEmpty()) {
            view.printMessage("엑셀로 저장할 데이터가 없습니다.");
            return;
        }

        exportToExcel(result);
    }

    /**
     * 엑셀 파일로 저장
     */
    private void exportToExcel(ComparisonResult result) {
        ExcelExportUtil.exportComparisonToExcel(view, result);
    }
} 