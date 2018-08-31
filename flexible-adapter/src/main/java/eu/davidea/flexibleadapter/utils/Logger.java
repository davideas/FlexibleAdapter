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

/**
 * Utility class that simplifies the use of {@link android.util.Log} by improving the call
 * to all log methods by supplying arguments as parameters instead of creating a string. This
 * avoids useless memory allocations when not requested: the StringBuilder itself, the buffer
 * and the String object. The new methods check in advance if the level is enabled and only
 * after the string message with arguments is created.
 * <p><b>Note:</b> This class has best usage with {@link Log}.</p>
 * <p><b>Classical usage</b>:</p>
 * <pre>
 * if (BuildConfig.DEBUG) {
 *     Log.setLevel(Level.INFO);
 * } else {
 *     Log.setLevel(Level.SUPPRESS);
 * }
 * Logger logger = new Logger("MyNewTag");
 * </pre>
 * <p><b>Advanced usage:</b></p>
 * The {@code Logger} constructor will reset the {@code Log.customTag} to {@code null}!
 * <br>So at the next static call of {@code Log.v/d/i/w/e(...);} it will log the current filename as tag.
 * <pre>
 * public class MyClass {
 *     private Logger logger;
 *
 *     public MyClass() {
 *         if (Log.customTag == null) Log.useTag("MyClass");
 *         this.logger = new Logger(Log.customTag);
 *         ...
 *     }
 *
 *     public static void useTag(String tag) {
 *         Log.useTag(tag);
 *     }
 * }
 *
 * // Usage
 * MyClass.useTag("MyNewSmartTag");
 * MyClass myClass = new MyClass();
 * </pre>
 *
 * @author Davide Steduto
 * @see Log
 * @since 02/09/2017
 */
public class Logger {

    private String instanceTag;

    public Logger(String tag) {
        instanceTag = tag;
        Log.useTag(null);
    }

    /**
     * Sends a {@link Log.Level#VERBOSE} log message.
     *
     * @param msg  the message you would like logged
     * @param args the extra arguments for the message
     */
    public void v(String msg, Object... args) {
        if (Log.isVerboseEnabled()) {
            android.util.Log.v(instanceTag, Log.formatMessage(msg, args));
        }
    }

    /**
     * Sends a {@link Log.Level#DEBUG} log message.
     *
     * @param msg  the message you would like logged
     * @param args the extra arguments for the message
     */
    public void d(String msg, Object... args) {
        if (Log.isDebugEnabled()) {
            android.util.Log.d(instanceTag, Log.formatMessage(msg, args));
        }
    }

    /**
     * Sends an {@link Log.Level#INFO} log message.
     *
     * @param msg  the message you would like logged
     * @param args the extra arguments for the message
     */
    public void i(String msg, Object... args) {
        if (Log.isInfoEnabled()) {
            android.util.Log.i(instanceTag, Log.formatMessage(msg, args));
        }
    }

    /**
     * Sends a {@link Log.Level#WARN} log message.
     *
     * @param msg  the message you would like logged
     * @param args the extra arguments for the message
     */
    public void w(String msg, Object... args) {
        if (Log.isWarnEnabled()) {
            android.util.Log.w(instanceTag, Log.formatMessage(msg, args));
        }
    }

    /**
     * Sends a {@link Log.Level#WARN} log message with the Exception at the end of the message.
     *
     * @param t    The exception to log
     * @param msg  the message you would like logged
     * @param args the extra arguments for the message
     */
    public void w(Throwable t, String msg, Object... args) {
        if (Log.isWarnEnabled()) {
            android.util.Log.w(instanceTag, Log.formatMessage(msg, args), t);
        }
    }

    /**
     * Sends an {@link Log.Level#ERROR} log message.
     *
     * @param msg  the message you would like logged
     * @param args the extra arguments for the message
     */
    public void e(String msg, Object... args) {
        if (Log.isErrorEnabled()) {
            android.util.Log.e(instanceTag, Log.formatMessage(msg, args));
        }
    }

    /**
     * Sends an {@link Log.Level#ERROR} log message with the Exception at the end of the message.
     *
     * @param t    The exception to log
     * @param msg  the message you would like logged
     * @param args the extra arguments for the message
     */
    public void e(Throwable t, String msg, Object... args) {
        if (Log.isErrorEnabled()) {
            android.util.Log.e(instanceTag, Log.formatMessage(msg, args), t);
        }
    }

    /**
     * What a Terrible Failure: Report a condition that should never happen.
     *
     * @param msg  the message you would like logged
     * @param args the extra arguments for the message
     */
    public void wtf(String msg, Object... args) {
        if (Log.isErrorEnabled()) {
            android.util.Log.wtf(instanceTag, Log.formatMessage(msg, args));
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
    public void wtf(Throwable t, String msg, Object... args) {
        if (Log.isErrorEnabled()) {
            android.util.Log.wtf(instanceTag, Log.formatMessage(msg, args), t);
        }
    }

}