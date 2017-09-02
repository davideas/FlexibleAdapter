/*
 * Copyright 2017 Davide Steduto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.davidea.flexibleadapter.utils;

import android.support.annotation.IntDef;
import android.support.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static eu.davidea.flexibleadapter.utils.Log.Level.DEBUG;
import static eu.davidea.flexibleadapter.utils.Log.Level.ERROR;
import static eu.davidea.flexibleadapter.utils.Log.Level.INFO;
import static eu.davidea.flexibleadapter.utils.Log.Level.SUPPRESS;
import static eu.davidea.flexibleadapter.utils.Log.Level.VERBOSE;
import static eu.davidea.flexibleadapter.utils.Log.Level.WARN;

/**
 * Utility static class that simplifies the use of {@link android.util.Log} by improving the call
 * to all log methods by supplying arguments as parameters instead of creating a string. This
 * avoids useless memory allocations when not requested: the StringBuilder itself, the buffer
 * and the String object. The new methods check in advance if the level is enabled and only
 * after the string message with arguments is created.
 * <p><b>Note:</b> This class can work in collaboration with {@link Logger}.</p>
 * <p>Others features are:</p>
 * <ul>
 * <li>Automatic TAG corresponding to the caller class name.</li>
 * <li>Runtime log level without the need of props file (useful for libraries).</li>
 * <li>Method name with line number.</li>
 * </ul>
 * <p>Use {@link Level#SUPPRESS} to disable all logs. For instance:</p>
 * <pre>
 * if (BuildConfig.DEBUG) {
 *     Log.setLevel(Level.INFO);
 * } else {
 *     Log.setLevel(Level.SUPPRESS);
 * }
 * Log.i("Message arg1=%s, arg2=%s", arg1, arg2);
 * </pre>
 *
 * @author Davide Steduto
 * @see Logger
 * @since 02/06/2017
 */
public class Log {

    private static final String SOURCE_FILE = "SourceFile";
    private static int LEVEL = SUPPRESS;
    private static boolean withMethodName;
    private static boolean withLineNumber;
    public static String customTag;

    private Log() {
    }

    /**
     * Annotation interface for log level:
     * {@link #VERBOSE}, {@link #DEBUG}, {@link #INFO},
     * {@link #WARN}, {@link #ERROR}, {@link #SUPPRESS}
     */
    @IntDef({VERBOSE, DEBUG, INFO, WARN, ERROR, SUPPRESS})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Level {
        int VERBOSE = android.util.Log.VERBOSE;
        int DEBUG = android.util.Log.DEBUG;
        int INFO = android.util.Log.INFO;
        int WARN = android.util.Log.WARN;
        int ERROR = android.util.Log.ERROR;
        int SUPPRESS = 10;
    }

    public static void setLevel(@Level int level) {
        LEVEL = level;
    }

    /**
     * Allows to log method name and line number from where the log is called.
     * <p>- With method name: {@code [method] msg}.
     * <br>- With line number: {@code [method:line] msg}.</p>
     * <b>Note:</b> Line number needs method name enabled.
     *
     * @param method true to print method name at the beginning of the message, false otherwise
     * @param line   true to print line number after the method name, false otherwise
     */
    public static void logMethodName(boolean method, boolean line) {
        withMethodName = method;
        withLineNumber = line;
    }

    public static boolean isVerboseEnabled() {
        return LEVEL <= VERBOSE;
    }

    public static boolean isDebugEnabled() {
        return LEVEL <= DEBUG;
    }

    public static boolean isInfoEnabled() {
        return LEVEL <= INFO;
    }

    public static boolean isWarnEnabled() {
        return LEVEL <= WARN;
    }

    public static boolean isErrorEnabled() {
        return LEVEL <= ERROR;
    }

    /**
     * Sends a {@link Level#VERBOSE} log message.
     *
     * @param msg  the message you would like logged
     * @param args the extra arguments for the message
     */
    public static void v(String msg, Object... args) {
        if (isVerboseEnabled()) {
            android.util.Log.v(getTag(), formatMessage(msg, args));
        }
    }

    /**
     * Sends a {@link Level#DEBUG} log message.
     *
     * @param msg  the message you would like logged
     * @param args the extra arguments for the message
     */
    public static void d(String msg, Object... args) {
        if (isDebugEnabled()) {
            android.util.Log.d(getTag(), formatMessage(msg, args));
        }
    }

    /**
     * Sends an {@link Level#INFO} log message.
     *
     * @param msg  the message you would like logged
     * @param args the extra arguments for the message
     */
    public static void i(String msg, Object... args) {
        if (isInfoEnabled()) {
            android.util.Log.i(getTag(), formatMessage(msg, args));
        }
    }

    /**
     * As {@link #i(String, Object...)} but with custom tag for one call only.
     *
     * @param tag  custom tag, used to identify the source of a log message
     * @param msg  the message you would like logged
     * @param args the extra arguments for the message
     */
    public static void iTag(String tag, String msg, Object... args) {
        if (isInfoEnabled()) {
            android.util.Log.i(tag, formatMessage(msg, args));
        }
    }

    /**
     * Sends a {@link Level#WARN} log message.
     *
     * @param msg  the message you would like logged
     * @param args the extra arguments for the message
     */
    public static void w(String msg, Object... args) {
        if (isWarnEnabled()) {
            android.util.Log.w(getTag(), formatMessage(msg, args));
        }
    }

    /**
     * Sends a {@link Level#WARN} log message with the Exception at the end of the message.
     *
     * @param t    The exception to log
     * @param msg  the message you would like logged
     * @param args the extra arguments for the message
     */
    public static void w(Throwable t, String msg, Object... args) {
        if (isWarnEnabled()) {
            android.util.Log.w(getTag(), formatMessage(msg, args), t);
        }
    }

    /**
     * Sends an {@link Level#ERROR} log message.
     *
     * @param msg  the message you would like logged
     * @param args the extra arguments for the message
     */
    public static void e(String msg, Object... args) {
        if (isErrorEnabled()) {
            android.util.Log.e(getTag(), formatMessage(msg, args));
        }
    }

    /**
     * Sends an {@link Level#ERROR} log message with the Exception at the end of the message.
     *
     * @param t    The exception to log
     * @param msg  the message you would like logged
     * @param args the extra arguments for the message
     */
    public static void e(Throwable t, String msg, Object... args) {
        if (isErrorEnabled()) {
            android.util.Log.e(getTag(), formatMessage(msg, args), t);
        }
    }

    /**
     * What a Terrible Failure: Report a condition that should never happen.
     *
     * @param msg  the message you would like logged
     * @param args the extra arguments for the message
     */
    public static void wtf(String msg, Object... args) {
        if (isErrorEnabled()) {
            android.util.Log.wtf(getTag(), formatMessage(msg, args));
        }
    }

    /**
     * What a Terrible Failure: Report a condition that should never happen with the Exception
     * at the end of the message.
     *
     * @param t    The exception to log
     * @param msg  the message you would like logged
     * @param args the extra arguments for the message
     */
    public static void wtf(Throwable t, String msg, Object... args) {
        if (isErrorEnabled()) {
            android.util.Log.wtf(getTag(), formatMessage(msg, args), t);
        }
    }

    /**
     * Sets a general static custom tag for ALL log calls.
     * <p><b>Note:</b> If a {@link Logger} instance is created, this custom tag will be
     * reset to {@code null} and current filename will be used instead.</p>
     *
     * @param customTag general static tag for ALL logs.
     */
    public static void useTag(@Nullable String customTag) {
        Log.customTag = customTag;
    }

    private static String getTag() {
        if (customTag != null) return customTag;
        StackTraceElement traceElement = new Throwable().getStackTrace()[2];
        String fileName = traceElement.getFileName();
        if (fileName == null) return SOURCE_FILE;
        return fileName.split("[.]")[0];
    }

    static String formatMessage(String msg, Object... args) {
        // In order to have the "null" values logged we need to pass args when null to the formatter
        // (This can still break depending on conversion of the formatter, see String.format)
        // else if there is no args, we return the message as-is, otherwise we pass args to formatting normally.
        return createLog(args != null && args.length == 0 ? msg : String.format(msg, args));
    }

    private static String createLog(String log) {
        if (withMethodName) {
            StackTraceElement traceElement = new Throwable().getStackTrace()[3];
            if (withLineNumber) {
                return String.format("[%s:%s] %s", traceElement.getMethodName(), traceElement.getLineNumber(), log);
            } else {
                return String.format("[%s] %s", traceElement.getMethodName(), log);
            }
        }
        return log;
    }

}