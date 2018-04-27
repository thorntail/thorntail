package io.thorntail.logging.impl.jdk;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Created by bob on 1/18/18.
 */
public class DefaultConsoleFormatter extends Formatter {

    public DefaultConsoleFormatter(String format) {
        this.format = format;
    }

    @Override
    public synchronized String format(LogRecord record) {
        date.setTime(record.getMillis());
        String source;
        if (record.getSourceClassName() != null) {
            source = record.getSourceClassName();
            if (record.getSourceMethodName() != null) {
                source += " " + record.getSourceMethodName();
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
                             date,
                             source,
                             record.getLoggerName(),
                             record.getLevel(),
                             message,
                             throwable);
    }

    private final String format;

    private final Date date = new Date();
}
