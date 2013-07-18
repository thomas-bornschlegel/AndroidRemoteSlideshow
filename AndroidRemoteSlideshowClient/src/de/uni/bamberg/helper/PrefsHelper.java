package de.uni.bamberg.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * 
 * Used to manage preferences with Android's SharedPreferences.
 * 
 * @author Thomas Bornschlegel
 * 
 */
public class PrefsHelper {

    // Name of settings
    private static final String PREFS_NAME = "nook settings";
    // Variables for individual settings:
    private static final String PREF_CLIENT_ID = "client id";
    private static final String PREF_SERVER_IP = "server ip";
    private static final String PREF_SERVER_PORT = "server port";
    private static final String PREF_EXIT_APP = "exit app";
    private static final String PREF_MS_BETWEEN_REDRAWING = "redraw timing";
    private static final String PREF_NUMBER_OF_REDRAW_STEPS = "redraw steps";
    private static final String PREF_TIMESTAMP_OF_LAST_CONNECTION = "last connection timestamp";
    private static final String PREF_ADVANCED_REFRESH = "advanced refresh";

    public static boolean getExitApp(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getBoolean(PREF_EXIT_APP, false);
    }

    public static String getServerIp(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getString(PREF_SERVER_IP, "192.168.1.2");
    }

    public static int getServerPort(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getInt(PREF_SERVER_PORT, 5060);
    }

    public static int getNumberOfRedrawSteps(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getInt(PREF_NUMBER_OF_REDRAW_STEPS, 4);
    }

    public static int getClientId(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getInt(PREF_CLIENT_ID, -1);
    }

    public static long getMsBetweenRedrawing(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getLong(PREF_MS_BETWEEN_REDRAWING, 700l);
    }

    public static long getTimestampOfLastConnection(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getLong(PREF_TIMESTAMP_OF_LAST_CONNECTION, 0);
    }

    public static boolean getAdvancedRefresh(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        return prefs.getBoolean(PREF_ADVANCED_REFRESH, false);
    }

    /**
     * Stores all settings using SharedPreferences.
     */
    public static void storeSettings(Context context, String serverIp, int serverPort, long msBetweenRedrawing,
            int numberOfRedrawSteps, int clientId, boolean advancedRefresh) {
        SharedPreferences prefs = getSharedPreferences(context);
        Editor editor = prefs.edit();
        editor.putString(PREF_SERVER_IP, serverIp);
        editor.putInt(PREF_SERVER_PORT, serverPort);
        editor.putLong(PREF_MS_BETWEEN_REDRAWING, msBetweenRedrawing);
        editor.putInt(PREF_NUMBER_OF_REDRAW_STEPS, numberOfRedrawSteps);
        editor.putInt(PREF_CLIENT_ID, clientId);
        editor.putBoolean(PREF_ADVANCED_REFRESH, advancedRefresh);
        editor.commit();
    }

    public static void storeTimestampOfLastConnection(Context context, long timestampOfLastConnection) {
        storeLong(context, PREF_TIMESTAMP_OF_LAST_CONNECTION, timestampOfLastConnection);
    }

    public static void storeExitApp(Context context, boolean exitApp) {
        storeBoolean(context, PREF_EXIT_APP, exitApp);
    }

    //
    // -------------------------------------------------
    //

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREFS_NAME, 0);
    }

    private static void storeString(Context context, String key, String value) {
        SharedPreferences prefs = getSharedPreferences(context);
        Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    private static void storeLong(Context context, String key, long value) {
        SharedPreferences prefs = getSharedPreferences(context);
        Editor editor = prefs.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    private static void storeInt(Context context, String key, int value) {
        SharedPreferences prefs = getSharedPreferences(context);
        Editor editor = prefs.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    private static void storeBoolean(Context context, String key, boolean value) {
        SharedPreferences prefs = getSharedPreferences(context);
        Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

}
