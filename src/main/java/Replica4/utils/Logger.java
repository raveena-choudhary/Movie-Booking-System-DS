package utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class Logger {

    public static class CustomFormatter extends Formatter {
        private static final DateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");

        @Override
        public String format(LogRecord record) {
            StringBuilder builder = new StringBuilder(1000);
            builder.append(df.format(new Date(record.getMillis()))).append(" - ");
//            builder.append("[").append(record.getSourceClassName()).append(".");
            //builder.append(record.getSourceMethodName()).append("] - ");
            //    builder.append(Arrays.stream(record.getParameters()).toArray().toString()).append("] - ");
            builder.append("[").append(record.getLevel()).append("] - ");
            builder.append(formatMessage(record));
            builder.append("\n");
            return builder.toString();
        }
    }

    public static class CustomMessage {
        //  String methodName;
        String reqType;
        String reqParameters;
        String message;
        String res;

        public CustomMessage(String reqType, String reqParameters, String message, String res) {
            //this.methodName = methodName;
            this.reqType = reqType;
            this.reqParameters = reqParameters;
            this.message = message;
            this.res = res;
        }

        @Override
        public String toString() {
            return "{" +

                    "reqType='" + reqType + '\'' +
                    ", reqParameters=[" + reqParameters + '\'' + "]" +
                    ", message='" + message + '\'' +
                    ", res='" + res + '\'' +
                    '}';
        }
    }
}
