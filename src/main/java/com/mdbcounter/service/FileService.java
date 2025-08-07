package com.mdbcounter.service;

import java.io.File;
import java.util.*;

public class FileService {

    /**
     * 경로 내부에 있는 mdb 파일을 찾음
     */
    public static List<File> findMdbFiles(File dir) {
        List<File> result = new ArrayList<>();
        Queue<File> queue = new LinkedList<>();
        queue.add(dir);
        while (!queue.isEmpty()) {
            File current = queue.poll();
            File[] files = current.listFiles();
            if (files == null) continue;
            for (File file : files) {
                if (file.isDirectory()) {
                    queue.add(file);
                } else if (file.getName().toLowerCase().endsWith(MdbCounterService.MDB_EXT)) {
                    result.add(file);
                }
            }
        }
        return result;
    }



    /**
     * 디렉터리 유효성 검사
     * @param dir 검사할 디렉터리
     * @return 유효한 디렉터리인지 여부
     */
    public static boolean isValidDirectory(File dir) {
        if (dir == null) return false;
        
        if (!dir.exists()) {
            return dir.mkdirs();
        }
        
        return dir.isDirectory();
    }


}