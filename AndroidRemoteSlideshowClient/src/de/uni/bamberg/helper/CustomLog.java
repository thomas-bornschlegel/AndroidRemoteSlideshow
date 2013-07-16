package de.uni.bamberg.helper;

import android.util.Log;

/**
 * Allows convenient logging.
 * 
 * @author Thomas Bornschlegel
 * 
 */
public class CustomLog {

    private static String LOG_TAG = "NookImageDisplayer";
    private static final String VERSION_NAME = "Version date 16.07.2013";

    public static void logVersion() {
        Log.d(LOG_TAG, VERSION_NAME);
    }

    public static void d(String msg) {
        Log.d(LOG_TAG, msg);
    }

    public static void i(String msg) {
        Log.i(LOG_TAG, msg);
    }

    public static void e(String msg) {
        Log.e(LOG_TAG, msg);
    }

    public static void e(String msg, Throwable e) {
        Log.e(LOG_TAG, msg, e);
    }

}