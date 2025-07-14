package com.mdbcounter.controller;

import com.mdbcounter.view.ConsoleView;
import com.mdbcounter.model.ColumnCount;
import com.mdbcounter.service.ExcelExporter;
import com.mdbcounter.service.MdbCounterService;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MdbCounterController {
    private final ConsoleView view;

    public MdbCounterController(ConsoleView view) {
        this.view = view;
    }

    public void run() {
        while (true) {
            File mdbDir = view.inputDirectory("MDB 파일이 있는 폴더 경로를 입력하세요: ");
            List<File> mdbFiles = findMdbFiles(mdbDir);
            if (mdbFiles.isEmpty()) {
                view.printResult("해당 폴더 및 하위 폴더에 mdb 파일이 없습니다.");
                continue;
            }
            view.printFileList(mdbFiles);
            if (!view.confirm("이 파일들로 진행할까요?")) {
                continue;
            } 
            Map<String, Map<String, List<ColumnCount>>> fileTableMap = countAllTables(mdbFiles);
            File excelDir = view.inputDirectory("엑셀 파일을 저장할 폴더 경로를 입력하세요: ");
            String excelPath = view.inputExcelPath(excelDir);

            exportToExcel(fileTableMap, excelPath);

            if (!view.askRepeat()) break;
            System.out.println("---------------------------------------------");
        }
    }

    private List<File> findMdbFiles(File dir) {
        try {
            return Files.walk(dir.toPath())
                    .filter(p -> p.toString().toLowerCase().endsWith(MdbCounterService.MDB_EXT))
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            view.printResult("폴더 탐색 중 오류: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private Map<String, Map<String, List<ColumnCount>>> countAllTables(List<File> mdbFiles) {
        MdbCounterService service = new MdbCounterService();
        return service.countFileTableColumns(mdbFiles);
    }

    private void exportToExcel(Map<String, Map<String, List<ColumnCount>>> fileTableMap, String excelPath) {
        try {
            ExcelExporter exporter = new ExcelExporter.Builder()
                    .totalSheetName("총계")
                    .autoSizeColumn(true)
                    .build();
            exporter.export(fileTableMap, excelPath);
            view.printResult("엑셀 파일이 성공적으로 저장되었습니다: " + excelPath);
        } catch (Exception e) {
            view.printResult("엑셀 저장 중 오류: " + e.getMessage());
        }
    }
} 