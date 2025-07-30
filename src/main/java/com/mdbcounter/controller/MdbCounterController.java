package com.mdbcounter.controller;

import com.mdbcounter.view.ConsoleView;
import com.mdbcounter.service.ExcelExporter;
import com.mdbcounter.service.MdbCounterService;
import com.mdbcounter.service.DbComparisonService;
import com.mdbcounter.model.TableCount;
import com.mdbcounter.model.MdbTableInfo;
import com.mdbcounter.model.ComparisonResult;

import java.io.File;
import java.util.*;

public class MdbCounterController {
    private final ConsoleView view;
    public MdbCounterController(ConsoleView view) {
        this.view = view;
    }

    public void run() {
        while (true) {
            // 작업 선택
            view.printMessage("=== MDB 데이터 분석 도구 ===");
            view.printMessage("1. 테이블 데이터 개수 카운트");
            view.printMessage("2. MDB와 DB 키 연결 확인");
            view.printMessage("3. 종료");
            
            String choice = view.inputChoice("작업을 선택하세요 (1-3): ");
            
            switch (choice) {
                case "1":
                    runTableCountMode();
                    break;
                case "2":
                    runKeyComparisonMode();
                    break;
                case "3":
                    view.printMessage("프로그램을 종료합니다.");
                    return;
                default:
                    view.printErrorMessage("잘못된 선택입니다. 다시 선택해주세요.");
                    continue;
            }
            
            if (!view.confirm("다른 작업을 계속하시겠습니까?")) break;
            view.printMessage("---------------------------------------------");
        }
    }
    
    private void runTableCountMode() {
        File mdbDir = getValidDirectory("MDB 파일이 있는 폴더 경로를 입력하세요: ");
        long searchStart = System.currentTimeMillis();
        List<File> mdbFiles = findMdbFiles(mdbDir);
        long searchEnd = System.currentTimeMillis();
        view.printMessage("해당 경로 mdb 파일 탐색중입니다.");
        if (mdbFiles.isEmpty()) {
            view.printMessage("해당 폴더 및 하위 폴더에 mdb 파일이 없습니다.");
            return;
        }
        view.printFileList(mdbFiles);
        view.printMessage("MDB 파일 탐색 시간: " + (searchEnd - searchStart) / 1000.0 + " sec");
        if (!view.confirm("이 파일들로 진행할까요?")) return;
        view.printMessage("해당 경로의 모든 mdb 파일 집계 중입니다. . . ");
        // 로딩 시간 측정 시작
        long loadStart = System.currentTimeMillis();
        Map<String, Integer> tableTotalMap = new LinkedHashMap<>();
        MdbCounterService service = new MdbCounterService();
        for (File mdb : mdbFiles) {
            List<TableCount> oneFileCounts = service.countTableColumnData(mdb);
            for (TableCount tableCount : oneFileCounts) {
                String tableName = tableCount.getTableName();
                int currentCount = tableTotalMap.getOrDefault(tableName, 0);
                tableTotalMap.put(tableName, currentCount + tableCount.getCount());
            }
        }
        // Map을 List<TableCount>로 변환
        List<TableCount> allTableCounts = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : tableTotalMap.entrySet()) {
            allTableCounts.add(new TableCount.Builder()
                    .tableName(entry.getKey())
                    .count(entry.getValue())
                    .build());
        }
        long loadEnd = System.currentTimeMillis();
        view.printMessage("로딩 시간: " + (loadEnd - loadStart) / 1000.0 + " sec");

        File excelDir = getValidDirectory("엑셀 파일을 저장할 폴더 경로를 입력하세요: ");
        String excelPath = getExcelPath(excelDir);
        view.printMessage("엑셀 파일을 저장 중입니다. 잠시만 기다려주세요...");
        long excelStart = System.currentTimeMillis();
        exportToExcel(allTableCounts, excelPath);
        long excelEnd = System.currentTimeMillis();
        view.printMessage("엑셀 저장 시간: " + (excelEnd - excelStart) / 1000.0 + " sec");
    }
    
    private void runKeyComparisonMode() {
        File mdbDir = getValidDirectory("MDB 파일이 있는 폴더 경로를 입력하세요: ");
        long searchStart = System.currentTimeMillis();
        List<File> mdbFiles = findMdbFiles(mdbDir);
        long searchEnd = System.currentTimeMillis();
        view.printMessage("MDB 파일 탐색 시간: " + (searchEnd - searchStart) / 1000.0 + " sec");
        if (mdbFiles.isEmpty()) {
            view.printMessage("해당 폴더 및 하위 폴더에 mdb 파일이 없습니다.");
            return;
        }
        view.printFileList(mdbFiles);
        if (!view.confirm("이 파일들로 진행할까요?")) return;
        view.printMessage("MDB 파일을 로딩중입니다.");
        // MDB 파일에서 테이블 정보 읽기
        long loadStart = System.currentTimeMillis();
        List<MdbTableInfo> allMdbTableInfos = new ArrayList<>();
//        MdbCounterService mdbService = new MdbCounterService();
        DbComparisonService dbComparisonService = new DbComparisonService();
        for (File mdb : mdbFiles) {
            List<MdbTableInfo> oneFileInfos = dbComparisonService.getMdbTableInfo(mdb);
            allMdbTableInfos.addAll(oneFileInfos);
        }
        long loadEnd = System.currentTimeMillis();
        view.printMessage("MDB 로딩 시간: " + (loadEnd - loadStart) / 1000.0 + " sec");

        // DB와 비교
        view.printMessage("DB와 비교 중입니다. 잠시만 기다려주세요...");
        long compareStart = System.currentTimeMillis();
        DbComparisonService dbService = new DbComparisonService();
        ComparisonResult result = dbService.compareMdbWithDb(allMdbTableInfos);
        long compareEnd = System.currentTimeMillis();
        view.printMessage("DB 비교 시간: " + (compareEnd - compareStart) / 1000.0 + " sec");

        // 결과 출력
        view.printMessage("=== 비교 결과 ===");
        view.printMessage("없는 테이블 개수: " + result.getMissingTables().size());
        view.printMessage("없는 키 개수: " + result.getMissingKeys().size());

        if(result.getMissingTables().size() == 0 && result.getMissingKeys().size() == 0){
            view.printMessage("엑셀로 저장할 데이터가 없습니다.");
            return;
        }
        // 엑셀 저장
        File excelDir = getValidDirectory("엑셀 파일을 저장할 폴더 경로를 입력하세요: ");
        String excelPath = getComparisonExcelPath(excelDir);
        view.printMessage("엑셀 파일을 저장 중입니다. 잠시만 기다려주세요...");
        long excelStart = System.currentTimeMillis();
        exportComparisonToExcel(result, excelPath);
        long excelEnd = System.currentTimeMillis();
        view.printMessage("엑셀 저장 시간: " + (excelEnd - excelStart) / 1000.0 + " sec");
    }

    private File getValidDirectory(String prompt) {
        while (true) {
            String path = String.valueOf(view.inputDirectory(prompt));
            File dir = new File(path);
            if (!dir.exists()) dir.mkdirs();
            if (!dir.isDirectory()) {
                view.printErrorMessage("유효한 폴더 경로가 아닙니다. 다시 입력하세요.");
            } else {
                return dir;
            }
        }
    }

    private List<File> findMdbFiles(File dir) {
        List<File> result = new ArrayList<>();
        Queue<File> queue = new LinkedList<>();
        queue.add(dir);
        while (!queue.isEmpty()) {
            File current = queue.poll();
            File[] files = current.listFiles();
            if (files == null) continue;
            for (File file : files) {
                if (file.isDirectory()) {
                    queue.add(file);
                } else if (file.getName().toLowerCase().endsWith(MdbCounterService.MDB_EXT)) {
                    result.add(file);
                }
            }
        }
        return result;
    }

    private String getExcelPath(File excelDir) {
        String date = new java.text.SimpleDateFormat("yyyyMMdd-HHmm").format(new java.util.Date());
        return new File(excelDir, date + "-mdb테이블총계.xlsx").getAbsolutePath();
    }

    private void exportToExcel(List<TableCount> tableCounts, String excelPath) {
        try {
            ExcelExporter exporter = new ExcelExporter.Builder()
                .sheetName("총계")
                .build();
            exporter.exportCntTable(tableCounts, excelPath);
            view.printMessage("엑셀 파일이 성공적으로 저장되었습니다: " + excelPath);
        } catch (Exception e) {
            view.printErrorMessage("엑셀 저장 중 오류: " + e.getMessage());
        }
    }
    
    private String getComparisonExcelPath(File excelDir) {
        String date = new java.text.SimpleDateFormat("yyyyMMdd-HHmm").format(new java.util.Date());
        return new File(excelDir, date + "-mdb_db_비교결과.xlsx").getAbsolutePath();
    }
    
    private void exportComparisonToExcel(ComparisonResult result, String excelPath) {
        try {
            ExcelExporter exporter = new ExcelExporter.Builder()
                .sheetName("비교결과")
                .build();
            exporter.exportComparisonResult(result, excelPath);
            view.printMessage("비교 결과 엑셀 파일이 성공적으로 저장되었습니다: " + excelPath);
        } catch (Exception e) {
            view.printErrorMessage("엑셀 저장 중 오류: " + e.getMessage());
        }
    }
} 