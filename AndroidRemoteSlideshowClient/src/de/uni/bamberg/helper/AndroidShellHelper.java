package de.uni.bamberg.helper;

import java.io.DataOutputStream;

/**
 * Allows the execution of shell commands on a rooted Android device.
 * 
 * @author Thomas Bornschlegel
 * 
 */
public class AndroidShellHelper {

    private Process p = null;

    public boolean runCommand(String command) {
        try {
            if (p == null) {
                p = Runtime.getRuntime().exec("su");
            }
            DataOutputStream outs = new DataOutputStream(p.getOutputStream());
            outs.writeBytes(command + "\n");
            return true;
        } catch (Exception e) {
            CustomLog.e("Could not execute command: " + command, e);
        }
        return false;
    }

    public boolean closeProgram(String programPackage) {
        boolean success = runCommand("adb shell ps | grep " + programPackage
                + " | awk '{print $2}' | xargs adb shell kill");
        if (success) {
            CustomLog.d("Successfully executed command to close: " + programPackage);
        } else {
            CustomLog.e("Could NOT close program: " + programPackage);
        }
        return success;
    }

}
