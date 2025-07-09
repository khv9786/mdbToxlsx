package com.mdbcounter;

import com.mdbcounter.controller.MdbCounterController;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) {
        // UCanAccess 경고 차단용
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(OutputStream.nullOutputStream())); // Java 11 이상

        // 로그 레벨 조정 (추가적으로)
        Logger.getLogger("net.ucanaccess").setLevel(Level.SEVERE);
        Logger.getLogger("com.healthmarketscience.jackcess").setLevel(Level.SEVERE);

        new MdbCounterController(new ConsoleView()).run();
    }
}

