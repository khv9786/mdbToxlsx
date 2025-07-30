package com.mdbcounter;

import com.mdbcounter.controller.MdbCounterController;
import com.mdbcounter.view.ConsoleView;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) {
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(OutputStream.nullOutputStream())); // Java 11 이상
        Logger.getLogger("net.ucanaccess").setLevel(Level.SEVERE);
        Logger.getLogger("com.healthmarketscience.jackcess").setLevel(Level.SEVERE);
        try {
            new MdbCounterController(new ConsoleView()).run();
        } finally {
            System.setErr(originalErr);
        }
    }
}

