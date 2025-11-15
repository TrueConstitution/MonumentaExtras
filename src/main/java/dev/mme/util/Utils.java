package dev.mme.util;

import dev.mme.MMEClient;
import org.jetbrains.annotations.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public abstract class Utils {
    public static String logError(Throwable exception, @Nullable String message) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        if (message != null) {
            printWriter.println(message);
        }
        exception.printStackTrace(printWriter);
        MMEClient.LOGGER.error(writer.toString());
        return writer.toString();
    }

    public static Pattern verifyPattern(String s) throws PatternSyntaxException {
        return s == null || s.isEmpty() ? null : Pattern.compile(s);
    }
}
