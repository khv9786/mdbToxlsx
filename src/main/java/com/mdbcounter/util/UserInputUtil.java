package com.mdbcounter.util;

import com.mdbcounter.model.ComparisonResult;
import com.mdbcounter.service.FileService;
import com.mdbcounter.view.ConsoleView;

import java.io.File;

/**
 * 사용자 입력 처리를 담당하는 유틸리티 클래스
 */
public class UserInputUtil {
    
    /**
     * 사용자로부터 유효한 디렉터리를 입력받는 메서드
     * @param view 콘솔 뷰
     * @param prompt 사용자에게 보여줄 메시지
     * @return 유효한 디렉터리 또는 null (사용자 취소)
     */
    public static File getValidDirectoryFromUser(ConsoleView view, String prompt) {
        while (true) {
            File dir = view.inputDirectory(prompt);
            if (dir == null) return null; // 사용자 취소

            if (FileService.isValidDirectory(dir)) {
                return dir;
            } else {
                view.printErrorMessage("유효한 폴더 경로가 아닙니다. 다시 입력하세요.");
            }
        }
    }

} 