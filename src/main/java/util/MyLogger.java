package util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

public abstract class MyLogger {
    private static final boolean APPEND_BY_DEFAULT = false;
    private static final String CURRENT_DATE = new SimpleDateFormat("[yyyy.MM.dd HH.mm.ss]").format(new Date());
    private static final String LOGGER_NAME = "casefinderlog " + CURRENT_DATE;
    private static final String LOG_PATH = System.getProperty("user.home") + File.separator + ".case-finder";
    private static final String FILENAME = LOG_PATH + File.separator + LOGGER_NAME + ".log";
    private static final Level DEFAULT_LEVEL = Settings.getLogLevel();
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(LOGGER_NAME);

    static {
        Handler ch = new ConsoleHandler();
        Handler fh = null;

        File dir = new File(LOG_PATH);

        // if the directory does not exist, create it
        if (!dir.exists()) {
            boolean result = false;

            try {
                result = dir.mkdir();
            } catch(SecurityException se){
                //handle it
            }
            if(!result) {
                throw new RuntimeException("Couldn't create directory for logs");
            }
        }
        try {
            fh = new FileHandler(FILENAME, APPEND_BY_DEFAULT);
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
        assert fh != null;

        MyFormatter formatter = new MyFormatter();
        ch.setFormatter(formatter);
        fh.setFormatter(formatter);

        ch.setLevel(DEFAULT_LEVEL);
        fh.setLevel(DEFAULT_LEVEL);

        LOGGER.setUseParentHandlers(false);

        LOGGER.setLevel(DEFAULT_LEVEL);

        LOGGER.addHandler(ch);
        LOGGER.addHandler(fh);
    }

    public static void close() {
        for(Handler h : LOGGER.getHandlers()) {
            h.close();
        }
    }

    public static java.util.logging.Logger getLogger(String name) {
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(name);
        logger.setParent(LOGGER);
        return logger;
    }

    public static java.util.logging.Logger getGlobal() {
        java.util.logging.Logger logger = java.util.logging.Logger.getGlobal();
        logger.setParent(LOGGER);
        return logger;
    }

    static class MyFormatter extends Formatter {
        /// Copy and paste of SimpleFormatter except of Thread name

        // format string for printing the log record
        private static final String format =
                "[%1$tb %1$td, %1$tY %1$tl:%1$tM:%1$tS %1$Tp] [%2$s {%7$s}] %4$s: %5$s%6$s%n";
        private final Date dat = new Date();

        @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
        public synchronized String format(LogRecord record) {
            dat.setTime(record.getMillis());
            String source;
            if (record.getSourceClassName() != null) {
                source = record.getSourceClassName();
                if (record.getSourceMethodName() != null) {
                    source += "." + record.getSourceMethodName();
                }
            } else {
                source = record.getLoggerName();
            }
            String message = formatMessage(record);
            String throwable = "";
            if (record.getThrown() != null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                pw.println();
                record.getThrown().printStackTrace(pw);
                pw.close();
                throwable = sw.toString();
            }
            return String.format(format,
                    dat,
                    source,
                    record.getLoggerName(),
                    record.getLevel().getLocalizedName(),
                    message,
                    throwable,
                    Thread.currentThread().getName());
        }
    }
}
