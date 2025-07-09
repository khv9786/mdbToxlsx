package com.mdbcounter;

import java.io.File;
import java.util.List;
import java.util.Scanner;

public class ConsoleView {
    private final Scanner sc = new Scanner(System.in);

    public File inputDirectory(String prompt) {
        while (true) {
            System.out.print(prompt);
            String path = sc.nextLine().trim();
            File dir = new File(path);
            if (!dir.exists()) dir.mkdirs();
            if (!dir.isDirectory()) {
                System.out.println("유효한 폴더 경로가 아닙니다. 다시 입력하세요.");
            } else {
                return dir;
            }
        }
    }

    public void printFileList(List<File> files) {
        System.out.println("\n=== 발견된 MDB 파일 목록 ===");
        for (int i = 0; i < files.size(); i++)
            System.out.println((i + 1) + ". " + files.get(i).getAbsolutePath());
    }

    public boolean confirm(String message) {
        System.out.print("\n" + message + " (y/n): ");
        return sc.nextLine().trim().equalsIgnoreCase("y");
    }

    public String inputExcelPath(File excelDir) {
        String date = new java.text.SimpleDateFormat("yyyyMMdd-HHmm").format(new java.util.Date());
        return new File(excelDir, date + "-mdb컬럼cnt.xlsx").getAbsolutePath();
    }

    public void printResult(String message) {
        System.out.println(message);
    }

    public boolean askRepeat() {
        System.out.print("\n다른 폴더를 계속 탐색하시겠습니까? (y/n): ");
        String again = sc.nextLine().trim();
        if (!again.equalsIgnoreCase("y")) {
            System.out.println("프로그램을 종료합니다.");
            return false;
        }
        return true;
    }
} 