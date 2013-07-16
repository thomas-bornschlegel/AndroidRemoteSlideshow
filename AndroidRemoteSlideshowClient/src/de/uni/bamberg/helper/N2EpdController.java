package de.uni.bamberg.helper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Used to set the redraw mode so that the screen is refreshed completely. Taken from:
 * http://forum.xda-developers.com/showpost.php?p=16206029&postcount=8
 * 
 * Further reading about another project called "NoRefreshToggler": Repository:
 * https://github.com/marspeople/NoRefreshToggle Post in Forum: http://forum.xda-developers.com/showthread.php?t=1502723
 * Similar Project for Sony PRS-T1: http://www.mobileread.com/forums/showpost.php?p=1956700&postcount=33
 * 
 * Description of Refresh Modes: http://forum.xda-developers.com/showthread.php?t=1549331
 * */
public class N2EpdController {

    /*
     * W/System.err( 6883): GC W/System.err( 6883): GU W/System.err( 6883): DU W/System.err( 6883): A2 W/System.err(
     * 6883): GL16 W/System.err( 6883): AUTO
     * 
     * W/System.err( 6982): APP_1 W/System.err( 6982): APP_2 W/System.err( 6982): APP_3 W/System.err( 6982): APP_4
     */

    public static void exitA2Mode() {
        System.err.println("FBReader::exitA2Mode");
        try {
            Class epdControllerClass = Class.forName("android.hardware.EpdController");
            Class epdControllerRegionClass = Class.forName("android.hardware.EpdController$Region");
            Class epdControllerRegionParamsClass = Class.forName("android.hardware.EpdController$RegionParams");
            Class epdControllerWaveClass = Class.forName("android.hardware.EpdController$Wave");

            Object[] waveEnums = null;
            if (epdControllerWaveClass.isEnum()) {
                System.err.println("EpdController Wave Enum successfully retrived.");
                waveEnums = epdControllerWaveClass.getEnumConstants();
            }

            Object[] regionEnums = null;
            if (epdControllerRegionClass.isEnum()) {
                System.err.println("EpdController Region Enum successfully retrived.");
                regionEnums = epdControllerRegionClass.getEnumConstants();
            }

            Constructor RegionParamsConstructor = epdControllerRegionParamsClass.getConstructor(new Class[] {
                    Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, epdControllerWaveClass, Integer.TYPE });

            Object localRegionParams = RegionParamsConstructor.newInstance(new Object[] { 0, 0, 600, 800, waveEnums[3],
                    16 }); // Wave = A2

            Method epdControllerSetRegionMethod = epdControllerClass.getMethod("setRegion", new Class[] { String.class,
                    epdControllerRegionClass, epdControllerRegionParamsClass });
            epdControllerSetRegionMethod.invoke(null, new Object[] { "FBReader-ReadingView", regionEnums[2],
                    localRegionParams });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void enterA2Mode() {
        System.err.println("FBReader::enterA2Mode");
        try {
            Class epdControllerClass = Class.forName("android.hardware.EpdController");
            Class epdControllerRegionClass = Class.forName("android.hardware.EpdController$Region");
            Class epdControllerRegionParamsClass = Class.forName("android.hardware.EpdController$RegionParams");
            Class epdControllerWaveClass = Class.forName("android.hardware.EpdController$Wave");

            Object[] waveEnums = null;
            if (epdControllerWaveClass.isEnum()) {
                System.err.println("EpdController Wave Enum successfully retrived.");
                waveEnums = epdControllerWaveClass.getEnumConstants();
            }

            Object[] regionEnums = null;
            if (epdControllerRegionClass.isEnum()) {
                System.err.println("EpdController Region Enum successfully retrived.");
                regionEnums = epdControllerRegionClass.getEnumConstants();
            }

            Constructor RegionParamsConstructor = epdControllerRegionParamsClass.getConstructor(new Class[] {
                    Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, epdControllerWaveClass, Integer.TYPE });

            Object localRegionParams = RegionParamsConstructor.newInstance(new Object[] { 0, 0, 600, 800, waveEnums[2],
                    16 }); // Wave = DU

            Method epdControllerSetRegionMethod = epdControllerClass.getMethod("setRegion", new Class[] { String.class,
                    epdControllerRegionClass, epdControllerRegionParamsClass });
            epdControllerSetRegionMethod.invoke(null, new Object[] { "FBReader-ReadingView", regionEnums[2],
                    localRegionParams });

            Thread.sleep(100L);
            localRegionParams = RegionParamsConstructor.newInstance(new Object[] { 0, 0, 600, 800, waveEnums[3], 14 }); // Wave
                                                                                                                        // =
                                                                                                                        // A2
            epdControllerSetRegionMethod.invoke(null, new Object[] { "FBReader-ReadingView", regionEnums[2],
                    localRegionParams });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void setGL16Mode() {
        System.err.println("FBReader::setGL16Mode");
        try {
            /*
             * Loading the Epson EPD Controller Classes
             */
            Class epdControllerClass = Class.forName("android.hardware.EpdController");
            Class epdControllerRegionClass = Class.forName("android.hardware.EpdController$Region");
            Class epdControllerRegionParamsClass = Class.forName("android.hardware.EpdController$RegionParams");
            Class epdControllerWaveClass = Class.forName("android.hardware.EpdController$Wave");
            Class epdControllerModeClass = Class.forName("android.hardware.EpdController$Mode");

            /*
             * Creating EPD enums
             */
            Object[] waveEnums = null;
            if (epdControllerWaveClass.isEnum()) {
                System.err.println("EpdController Wave Enum successfully retrived.");
                waveEnums = epdControllerWaveClass.getEnumConstants();
            }

            Object[] regionEnums = null;
            if (epdControllerRegionClass.isEnum()) {
                System.err.println("EpdController Region Enum successfully retrived.");
                regionEnums = epdControllerRegionClass.getEnumConstants();
            }

            Object[] modeEnums = null;
            if (epdControllerModeClass.isEnum()) {
                System.err.println("EpdController Region Enum successfully retrived.");
                modeEnums = epdControllerModeClass.getEnumConstants();
                System.err.println(modeEnums);
            }

            Constructor RegionParamsConstructor = epdControllerRegionParamsClass.getConstructor(new Class[] {
                    Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, epdControllerWaveClass });

            Object localRegionParams = RegionParamsConstructor
                    .newInstance(new Object[] { 0, 0, 600, 800, waveEnums[1] }); // Wave = GU

            Method epdControllerSetRegionMethod = epdControllerClass.getMethod("setRegion", new Class[] { String.class,
                    epdControllerRegionClass, epdControllerRegionParamsClass, epdControllerModeClass });
            epdControllerSetRegionMethod.invoke(null, new Object[] { "FBReader-ReadingView", regionEnums[2],
                    localRegionParams, modeEnums[2] }); // Mode = ONESHOT_ALL
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setDUMode() {
        System.err.println("FBReader::setDUMode");
        try {
            Class epdControllerClass = Class.forName("android.hardware.EpdController");
            Class epdControllerRegionClass = Class.forName("android.hardware.EpdController$Region");
            Class epdControllerRegionParamsClass = Class.forName("android.hardware.EpdController$RegionParams");
            Class epdControllerWaveClass = Class.forName("android.hardware.EpdController$Wave");

            Object[] waveEnums = null;
            if (epdControllerWaveClass.isEnum()) {
                System.err.println("EpdController Wave Enum successfully retrived.");
                waveEnums = epdControllerWaveClass.getEnumConstants();
            }

            Object[] regionEnums = null;
            if (epdControllerRegionClass.isEnum()) {
                System.err.println("EpdController Region Enum successfully retrived.");
                regionEnums = epdControllerRegionClass.getEnumConstants();
            }

            Constructor RegionParamsConstructor = epdControllerRegionParamsClass.getConstructor(new Class[] {
                    Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, epdControllerWaveClass, Integer.TYPE });

            Object localRegionParams = RegionParamsConstructor.newInstance(new Object[] { 0, 0, 600, 800, waveEnums[2],
                    14 });

            Method epdControllerSetRegionMethod = epdControllerClass.getMethod("setRegion", new Class[] { String.class,
                    epdControllerRegionClass, epdControllerRegionParamsClass });
            epdControllerSetRegionMethod.invoke(null, new Object[] { "FBReader-ReadingView", regionEnums[2],
                    localRegionParams });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
