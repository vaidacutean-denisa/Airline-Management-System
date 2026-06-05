package airlinesystem.services;

import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class AuditService {
    private static AuditService instance;
    private static final String FILE = "audit.csv";

    private AuditService() {}

    public static AuditService getInstance() {                          // singleton design pattern
        if (instance == null) {
            instance = new AuditService();
        }
        return instance;
    }

    // generic method for logging operations
    private void writeLog(String operation, String object, String details) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE, true))) {                // boolean append: true -> adds data at the end of the specified file
            LocalDateTime timestamp = LocalDateTime.now();
            writer.println(timestamp + "," + operation + "," + object + "," + details);                 // logs the timestamp of the operation + specific details

        } catch (IOException e) {                                                                       // we use try-with-resources to ensure the FileWriter is closed automatically (preventing leaks)
            System.err.println("Error writing to audit log file: " + e.getMessage());
        }
    }

    // we log every operation performed on the database by calling the private generic method (encapsulation)
    public void logAdd(String object, String details) {
        writeLog("ADD", object, details);
    }

    public void logGet(String object, String details) {
        writeLog("GET", object, details);
    }

    public void logUpdate(String object, String details) {
        writeLog("UPDATE", object, details);
    }

    public void logDelete(String object, String details) {
        writeLog("DELETE", object, details);
    }

    // log the errors as well
    public void logError(String object, String details) {
        writeLog("ERROR", object, details);
    }
}
