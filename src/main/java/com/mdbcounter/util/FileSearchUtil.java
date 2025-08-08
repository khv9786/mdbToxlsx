package com.mdbcounter.util;

import com.mdbcounter.service.FileService;
import com.mdbcounter.view.ConsoleView;

import java.io.File;
import java.util.List;

/**
 * 파일 검색 및 시간 측정을 담당하는 유틸리티 클래스
 */
public class FileSearchUtil {
    
    /**
     * MDB 파일 검색 및 시간 측정
     * @param view 콘솔 뷰
     * @param mdbDir 검색할 디렉터리
     * @return 검색된 MDB 파일 리스트
     */
    public static List<File> searchMdbFiles(ConsoleView view, File mdbDir) {
        long searchStart = System.currentTimeMillis();
        List<File> mdbFiles = FileService.findMdbFiles(mdbDir);
        
        view.printMessage("해당 경로 mdb 파일 탐색중입니다.");
        view.printFileList(mdbFiles);
        view.printLoadingTime(searchStart,"MDB 파일 탐색 시간: ");
        
        return mdbFiles;
    }
    
    /**
     * MDB 파일 검색 (시간 측정 포함)
     * @param view 콘솔 뷰
     * @param mdbDir 검색할 디렉터리
     * @return 검색된 MDB 파일 리스트
     */
    public static List<File> searchMdbFilesWithTime(ConsoleView view, File mdbDir) {
        long searchStart = System.currentTimeMillis();
        List<File> mdbFiles = FileService.findMdbFiles(mdbDir);

        view.printLoadingTime(searchStart,"MDB 파일 탐색 시간: ");
        view.printFileList(mdbFiles);
        
        return mdbFiles;
    }
} 