package com.mdbcounter.controller;

import com.mdbcounter.model.TableCount;
import com.mdbcounter.service.FileService;
import com.mdbcounter.service.MdbCounterService;
import com.mdbcounter.service.startService;
import com.mdbcounter.util.ExcelExportUtil;
import com.mdbcounter.util.UserInputUtil;
import com.mdbcounter.view.ConsoleView;

import java.io.File;
import java.util.List;
import java.util.Map;

public class TableCountController implements startService {
    private final ConsoleView view;
    private final MdbCounterService mdbCounterService;

    public TableCountController(ConsoleView view, MdbCounterService mdbCounterService) {
        this.view = view;
        this.mdbCounterService = mdbCounterService;
    }

    @Override
    public void execute() {
        File mdbDir = UserInputUtil.getValidDirectoryFromUser(view, "MDB 파일이 있는 폴더 경로를 입력하세요: ");
        if (mdbDir == null) return;

        List<File> mdbFiles = FileService.searchMdbFilesWithTime(mdbDir);
        if (mdbFiles.isEmpty()) {
            view.printMessage("해당 폴더 및 하위 폴더에 mdb 파일이 없습니다.");
            return;
        }

        view.printFileList(mdbFiles);
        if (!view.confirm("이 파일들로 진행할까요?")) return;

        List<TableCount> allTableCounts = setTableCounts(mdbFiles);
        ExcelExportUtil.exportTableCountToExcel(view, allTableCounts);
    }


    /**
     * 테이블 데이터 집계
     */
    private List<TableCount> setTableCounts(List<File> mdbFiles) {
        view.printMessage("해당 경로의 모든 mdb 파일 집계 중입니다. . . ");
        long loadStart = System.currentTimeMillis();
        Map<String, Integer> tableTotalMap = mdbCounterService.aggregationMdbFile(mdbFiles);
        List<TableCount> allTableCounts = mdbCounterService.convertMapToList(tableTotalMap);
        view.printLoadingTime(loadStart, "MDB 로딩 시간: ");
        return allTableCounts;
    }
}