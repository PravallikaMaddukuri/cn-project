import java.util.logging.Formatter;
import java.util.logging.*;
import java.io.IOException;
import java.util.Date;

// class for recoedlogging
public class RecordLogging {

    Logger helper_log;
    FileHandler handlingF;

    //method for record logging
    RecordLogging(String ID_peer) throws IOException {
        helper_log = Logger.getLogger(ID_peer);
        handlingF = new FileHandler("./" + ID_peer + "/logs_" + ID_peer + ".log");
        handlingF.setFormatter(new FormatClass());
        helper_log.addHandler(handlingF);

    }
    //information for logging method
    public void informationLog(String message) {
        helper_log.log(new LogRecord(Level.INFO, message));
    }

    //method for error log method
    public void ErrorLog(String message) {
        helper_log.log(new LogRecord(Level.SEVERE, message));
    }


    // class for formating
    class FormatClass extends Formatter {

        // method for formating
        @Override
        public String format(LogRecord logRecord) {
            return new Date(logRecord.getMillis()) + " : " + logRecord.getMessage() + "\n";
        }

    }

}

