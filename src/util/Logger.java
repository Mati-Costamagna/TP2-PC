package util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Logger {
    private static final String FILE_NAME = "log.txt";

    public static synchronized void log(String mensaje) {
        try (PrintWriter out = new PrintWriter(new FileWriter(FILE_NAME, true))) {
            out.println(System.currentTimeMillis() + ": " + mensaje);
        } catch (IOException e) {
            System.err.println("Error escribiendo en log: " + e.getMessage());
        }
    }
}
