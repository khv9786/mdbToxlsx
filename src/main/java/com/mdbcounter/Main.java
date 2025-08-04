package com.mdbcounter;

import com.mdbcounter.controller.MdbCounterController;
import com.mdbcounter.view.ConsoleView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    public static void main(String[] args) {
        logger.info("MDB Counter 애플리케이션 시작");
        
        try {
            new MdbCounterController(new ConsoleView()).run();
            logger.info("MDB Counter 애플리케이션 정상 종료");
        } catch (Exception e) {
            logger.error("애플리케이션 실행 중 오류 발생: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
}

