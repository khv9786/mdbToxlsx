package com.mdbcounter.controller;

import com.mdbcounter.service.FileService;
import com.mdbcounter.service.TableCountService;
import com.mdbcounter.service.ComparisonService;
import com.mdbcounter.view.ConsoleView;

public class MdbCounterController {
    private final ConsoleView view;
    private final TableCountService tableCountService;
    private final ComparisonService comparisonService;

    public MdbCounterController(ConsoleView view) {
        this.view = view;
        this.tableCountService = new TableCountService(view);
        this.comparisonService = new ComparisonService(view);
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
                    tableCountService.execute();
                    break;
                case "2":
                    comparisonService.execute();
                    break;
                case "3":
                    view.printMessage("프로그램을 종료합니다.");
                    return;
                default:
                    view.printErrorMessage("잘못된 선택입니다. 다시 선택해주세요.");
                    continue;
            }

            if (!view.confirm("다른 작업을 계속하시겠습니까?"))
                view.printMessage("프로그램을 종료합니다.");
            view.printMessage("---------------------------------------------");
        }
    }

} 