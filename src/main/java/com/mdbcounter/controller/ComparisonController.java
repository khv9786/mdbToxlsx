package com.mdbcounter.controller;

import com.mdbcounter.model.ComparisonResult;
import com.mdbcounter.model.MdbTableInfo;
import com.mdbcounter.service.DbComparisonService;
import com.mdbcounter.service.FileService;
import com.mdbcounter.service.startService;
import com.mdbcounter.util.ExcelExportUtil;
import com.mdbcounter.util.UserInputUtil;
import com.mdbcounter.view.ConsoleView;

import java.io.File;
import java.util.List;

public class ComparisonController implements startService {
    private final ConsoleView view;
    private final DbComparisonService dbComparisonService;

    public ComparisonController(ConsoleView view, DbComparisonService dbComparisonService) {
        this.view = view;
        this.dbComparisonService = dbComparisonService;
    }

    @Override
    public void execute() {
        File mdbDir = UserInputUtil.getValidDirectoryFromUser(view, "MDB 파일이 있는 폴더 경로를 입력하세요: ");
        if (mdbDir == null) return;

        List<File> mdbFiles = FileService.findMdbFiles(mdbDir);
        if (mdbFiles.isEmpty()) {
            view.printMessage("해당 폴더 및 하위 폴더에 mdb 파일이 없습니다.");
            return;
        }
        view.printFileList(mdbFiles);
        if (!view.confirm("이 파일들로 진행할까요?")) return;

        view.printMessage("mdb 파일 로딩 중 입니다 . . . ");
        long loadStart = System.currentTimeMillis();
        List<MdbTableInfo> allMdbTableInfos = dbComparisonService.loadMdbData(mdbFiles);
        view.printLoadingTime(loadStart,"mdb 로드 시간: ");


        view.printMessage("mdb와 db 비교 중 입니다 . . . ");
        loadStart = System.currentTimeMillis();
        ComparisonResult result = dbComparisonService.compareDbWithMdb(allMdbTableInfos);
        view.printLoadingTime(loadStart,"MDB 비교 시간: ");

        displayAndExportResults(result);
    }

    /**
     * 결과 출력 및 엑셀 저장
     */
    private void displayAndExportResults(ComparisonResult result) {
        view.printMessage("=== 비교 결과 ===");
        view.printMessage("없는 테이블 개수: " + result.getMissingTables().size());
        view.printMessage("없는 키 개수: " + result.getMissingKeys().size());

        if (dbComparisonService.isDatathere(result)) {
            view.printMessage("엑셀로 저장할 데이터가 없습니다.");
            return;
        }

        ExcelExportUtil.exportComparisonToExcel(view, result);
    }
} 