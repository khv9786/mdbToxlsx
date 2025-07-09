package com.mdbcounter;

import com.mdbcounter.model.ColumnCount;
import com.mdbcounter.service.ExcelExporter;
import com.mdbcounter.service.MdbCounterService;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        System.setProperty("ucanaccess.disableAutoLoadFunctions", "true");

        // 로그 줄이기
        java.util.logging.Logger.getLogger("net.ucanaccess").setLevel(java.util.logging.Level.SEVERE);
        java.util.logging.Logger.getLogger("com.healthmarketscience.jackcess").setLevel(java.util.logging.Level.SEVERE);


        Scanner sc = new Scanner(System.in);
        while (true) {
            // 1. MDB 폴더 입력 및 검증
            File dir;
            while (true) {
                System.out.print("MDB 파일이 있는 폴더 경로를 입력하세요: ");
                String path = sc.nextLine().trim();
                dir = new File(path);
                if (!dir.exists() || !dir.isDirectory()) {
                    System.out.println("유효한 폴더 경로가 아닙니다. 다시 입력하세요.");
                } else break;
            }

            // 2. 하위 폴더까지 mdb 파일 모두 찾기
            List<File> mdbFiles = new ArrayList<>();
            try {
                mdbFiles = Files.walk(dir.toPath())
                        .filter(p -> p.toString().toLowerCase().endsWith(MdbCounterService.MDB_EXT))
                        .map(Path::toFile)
                        .collect(Collectors.toList());
            } catch (Exception e) {
                System.err.println("폴더 탐색 중 오류: " + e.getMessage());
                continue;
            }
            if (mdbFiles.isEmpty()) {
                System.out.println("해당 폴더 및 하위 폴더에 mdb 파일이 없습니다.");
                continue;
            }

            // 3. 파일 리스트 출력
            System.out.println("\n=== 발견된 MDB 파일 목록 ===");
            String fileName = "";
            for (int i = 0; i < mdbFiles.size(); i++)
                System.out.println((i + 1) + ". " + mdbFiles.get(i).getAbsolutePath());

            // 4. 사용자 확인
            System.out.print("\n이 파일들로 진행할까요? (y/n): ");
            if (!sc.nextLine().trim().equalsIgnoreCase("y")) {
                System.out.println("작업을 취소합니다.");
                continue;
            }

            // 5. 테이블별 컬럼 카운트(row)
//            // 불필요한 warn 제거용.
//            System.setProperty("ucanaccess.disableAutoLoadFunctions", "true");
            MdbCounterService service = new MdbCounterService();
            Map<String, Map<String, List<ColumnCount>>> fileTableMap = service.countFileTableColumns(mdbFiles);

            // 6. 엑셀 저장 폴더 입력
            File excelDir;
            while (true) {
                System.out.print("엑셀 파일을 저장할 폴더 경로를 입력하세요: ");
                String path = sc.nextLine().trim();
                excelDir = new File(path);
                if (!excelDir.exists()) excelDir.mkdirs();
                if (!excelDir.isDirectory()) {
                    System.out.println("폴더 경로를 입력하세요.");
                } else break;
            }
            String date = new SimpleDateFormat("yyyyMMdd-HHmm").format(new Date());
            String excelPath = new File(excelDir, date + "-mdb컬럼cnt.xlsx").getAbsolutePath();

            // 7. 엑셀 생성 (테이블별 row)
            try {
                new ExcelExporter().export(fileTableMap, excelPath);
                System.out.println("엑셀 파일이 성공적으로 저장되었습니다: " + excelPath);
            } catch (Exception e) {
                System.err.println("엑셀 저장 중 오류: " + e.getMessage());
            }

            // 8. 반복 여부 확인
            System.out.print("\n다른 폴더를 계속 탐색하시겠습니까? (y/n): ");
            String again = sc.nextLine().trim();
            if (!again.equalsIgnoreCase("y")) {
                System.out.println("프로그램을 종료합니다.");
                break;
            }
            System.out.println("---------------------------------------------");
        }
    }
} 