package com.mdbcounter.view;
import java.io.*;
import java.util.List;

public class ConsoleView {
    private final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    private StringBuilder sb = new StringBuilder();
    public File inputDirectory(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String path = br.readLine().trim();
                File dir = new File(path);
                if (!dir.isDirectory()) {
                    System.out.println("유효한 폴더 경로가 아닙니다. 다시 입력하세요.");
                } else {
                    return dir;
                }
            } catch (IOException e) {
                System.err.println("입력 중 오류가 발생했습니다: " + e.getMessage());
            }
        }
    }

    public void printMessage(String msg) {
        System.out.println(msg);
    }

    public void printErrorMessage(String msg) {
        System.err.println("[오류] " + msg);
    }

    public void printLoadingTime(long start, String msg){
        long end = System.currentTimeMillis();
        double time = (end - start) / 1000.0;
        System.out.println(msg + time + " sec");
    }

    public void printFileList(List<File> files) {
        sb = new StringBuilder();
        sb.append("\n=== 발견된 MDB 파일 목록 ===\n");
        for (int i = 0; i < files.size(); i++) {
            sb.append(i+1).append(". ").append(files.get(i).getAbsolutePath()).append("\n");
        }
        System.out.println(sb);
    }

    public boolean confirm(String message) {
        while (true) {
            try {
                System.out.print("\n" + message + " (y/n): ");
                String input = br.readLine().trim().toLowerCase();
                if (input.equals("y")) return true;
                if (input.equals("n")) return false;
                printErrorMessage("y 또는 n만 입력하세요.");
            } catch (IOException e) {
                System.err.println("입력 중 오류가 발생했습니다: " + e.getMessage());
            }
        }
    }

    public String inputChoice(String prompt) {
        try {
            System.out.print(prompt);
            return br.readLine().trim();
        } catch (IOException e) {
            System.err.println("입력 중 오류가 발생했습니다: " + e.getMessage());
            return "";
        }
    }
} 