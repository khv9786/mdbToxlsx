package com.mdbcounter;

import com.mdbcounter.controller.MdbCounterController;
import com.mdbcounter.service.DbCompareService;
import com.mdbcounter.service.MdbService;
import com.mdbcounter.service.dao.DbComparisonDao;
import com.mdbcounter.service.dao.DbDao;
import com.mdbcounter.service.dao.MdbDao;
import com.mdbcounter.view.ConsoleView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    public static void main(String[] args) {
        logger.info("MDB Counter 애플리케이션 시작");

        try {
            // DAO 객체 생성
            MdbDao mdbDao = new MdbDao();
            DbDao dbDao = new DbDao();
            DbComparisonDao dbComparisonDao = new DbComparisonDao();

            // Service 객체 생성 (DAO 주입)
            DbCompareService dbCompareService =
                    new DbCompareService(dbDao, dbComparisonDao);

            MdbService mdbService =
                    new MdbService(mdbDao);

            // Controller 생성 후 실행
            new MdbCounterController(
                    new ConsoleView(),
                    dbCompareService,
                    mdbService
            ).run();
            logger.info("MDB Counter 애플리케이션 정상 종료");
        } catch (Exception e) {
            logger.error("애플리케이션 실행 중 오류 발생: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
}

