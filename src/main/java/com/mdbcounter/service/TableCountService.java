package com.mdbcounter.service;

import com.mdbcounter.model.TableCount;
import com.mdbcounter.util.ExcelExportUtil;
import com.mdbcounter.util.FileSearchUtil;
import com.mdbcounter.util.UserInputUtil;
import com.mdbcounter.view.ConsoleView;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
// TODO 서비스랑 컨트롤러 역할 분리가 제대로 안되었음. 컨트롤러 분리만 너무 신경쓴듯
public class TableCountService implements startService {
    private final ConsoleView view;
    private final MdbCounterService mdbCounterService;

    public TableCountService(ConsoleView view) {
        this.view = view;
        this.mdbCounterService = new MdbCounterService();
    }

    @Override
    public void execute() {
        File mdbDir = UserInputUtil.getValidDirectoryFromUser(view, "MDB 파일이 있는 폴더 경로를 입력하세요: ");
        if (mdbDir == null) return;

        List<File> mdbFiles = FileSearchUtil.searchMdbFiles(view, mdbDir);
        if (mdbFiles.isEmpty()) {
            view.printMessage("해당 폴더 및 하위 폴더에 mdb 파일이 없습니다.");
            return;
        }

        if (!view.confirm("이 파일들로 진행할까요?")) return;

        List<TableCount> allTableCounts = aggregateTableCounts(mdbFiles);
        ExcelExportUtil.exportTableCountToExcel(view, allTableCounts);
    }


    /**
     * 테이블 데이터 집계
     */
    private List<TableCount> aggregateTableCounts(List<File> mdbFiles) {
        view.printMessage("해당 경로의 모든 mdb 파일 집계 중입니다. . . ");

        long loadStart = System.currentTimeMillis();
        Map<String, Integer> tableTotalMap = new LinkedHashMap<>();
        for (File mdb : mdbFiles) {
            List<TableCount> oneFileCounts = mdbCounterService.countTableColumnData(mdb);
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
        view.printLoadingTime(loadStart, loadEnd, "로딩 시간: ");
        return allTableCounts;
    }
}