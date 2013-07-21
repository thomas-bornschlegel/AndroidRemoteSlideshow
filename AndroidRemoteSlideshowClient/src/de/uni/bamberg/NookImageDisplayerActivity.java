package de.uni.bamberg;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.uni.bamberg.helper.CustomLog;
import de.uni.bamberg.helper.PrefsHelper;
import de.uni.bamberg.helper.RemoteMessageCodes;

/**
 * Main activity that displays the remote slideshow and communicates with the Java server.
 * 
 * @author Thomas Bornschlegel
 * 
 */
/**
 * @author Thomas Bornschlegel
 * 
 */
public class NookImageDisplayerActivity extends Activity {

    // UI Elements:
    private ImageView imageView;
    private TextView textView;
    private LinearLayout linearLayout;

    // Constants for connection attempts:
    private int timeoutForServerConnectionInSeconds = 10;
    private final long thresholdBetweenConnectionEstablishingInMs = 40000;

    // Further constants:
    private int REQUEST_CODE_SETTINGS_ACTIVITY = 124357;
    public static final String EXTRA_EXIT_APP = "exit app extra";
    private String currentDirectory = "not set";
    private String blankScreenConstant = "blankScreen#+.,.";

    // Variables to save the current state:
    private int connectionAttemptCount = 1;
    private boolean globalExit = false;
    private int randomInstanceNumber = -1;

    // Handler codes to change the displayed image/text:
    private int handlerIdNextImage = 1;
    private int handlerIdRefreshScreen = 2;
    private int handlerIdDrawImage = 3;
    private int handlerIdDisplayMessage = 4;

    // For remote communication with the server:
    private Socket remote;
    private BufferedReader in;
    private PrintWriter out;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        randomInstanceNumber = (int) (Math.random() * (double) 100000);
        CustomLog.d("Calling onCreate: " + getCurrentTimeStamp());
        CustomLog.logVersion();

        // Init the UI:
        setContentView(R.layout.main);
        imageView = (ImageView) findViewById(R.id.imageView);
        textView = (TextView) findViewById(R.id.textView);
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);

        globalExit = false;

        // Start the connection to the server:
        PrefsHelper.storeTimestampOfLastConnection(getApplicationContext(), 0);
        new EstablishConnectionTask().execute();

        // Remove lock screen:
        initWindowFlags();
    }

    /**
     * Removes the lock screen by setting window flags.
     */
    private void initWindowFlags() {
        Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        win.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        win.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        win.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR);
    }

    @Override
    protected void onResume() {
        super.onResume();
        CustomLog.d("Calling onResume: " + getCurrentTimeStamp());
        CustomLog.logVersion();

        // Change layout to landscape if is currently portrait:
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

    }

    /**
     * We do nothing if the configuration changed (e.g. when the screen was rotated). By overwriting the default
     * implementation we make sure that this activity is not destroyed when the screen rotates.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        CustomLog.d("Calling onConfigurationChanged: " + getCurrentTimeStamp());
        // Do nothing
    }

    /**
     * Async Task that establishes the connection to the server.
     * 
     */
    private class EstablishConnectionTask extends AsyncTask<Void, Void, Integer> {

        private int port = 5060;
        private String server = "10.151.1.121";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Use the IP and port that the user set up in the settings screen:
            server = PrefsHelper.getServerIp(getApplicationContext());
            port = PrefsHelper.getServerPort(getApplicationContext());

            if (globalExit) {
                exitApp("onPreExecute in EstablishConnectionTask");
            } else {
                String message = "Trying to connect to " + server + ":" + port + "... (attempt "
                        + connectionAttemptCount++ + ")";
                displayMessage(message);
            }

            CustomLog.d("STARTED CONNECTION TASK (" + getCurrentTimeStamp() + ")");
        }

        @Override
        protected Integer doInBackground(Void... params) {
            // Check if another connections was established shortly before: (this is used to prevent two connections on
            // one device. It should never happen, as the EstablishConnectionTask is only executed once, but we want to
            // be sure that nothing wents wrong if this task should have been started twice.)
            long start = System.currentTimeMillis();
            long lastConnection = PrefsHelper.getTimestampOfLastConnection(getApplicationContext());
            boolean haveRecentOtherConnection = start - lastConnection < thresholdBetweenConnectionEstablishingInMs;
            CustomLog.d("Another connection was established " + thresholdBetweenConnectionEstablishingInMs + "ms ago: "
                    + haveRecentOtherConnection);
            if (remote == null && !haveRecentOtherConnection) {

                try {
                    // Save the socket of our newly established server connection:
                    remote = new Socket();
                    SocketAddress socketAddress = new InetSocketAddress(server, port);
                    remote.connect(socketAddress, timeoutForServerConnectionInSeconds * 1000);
                    // remote.setKeepAlive(true);
                    String formattedTime = new SimpleDateFormat().format(start);
                    CustomLog.d("Successfully connected @ " + formattedTime);
                    in = new BufferedReader(new InputStreamReader(remote.getInputStream()));
                    out = new PrintWriter(remote.getOutputStream(), true);
                    // Send our client ID to the server:
                    out.write(RemoteMessageCodes.MESSAGE_SEND_CLIENT_ID + ":"
                            + PrefsHelper.getClientId(getApplicationContext()) + "\n");
                    out.flush();
                    // Save the timestamp of this connection to prevent double connections:
                    PrefsHelper.storeTimestampOfLastConnection(getApplicationContext(), System.currentTimeMillis());
                    return 1;
                } catch (Exception e) {
                    String message = "Error while trying to connect to server.";
                    CustomLog.e(message, e);
                }
                long processedTime = System.currentTimeMillis() - start;
                long difference = processedTime - (timeoutForServerConnectionInSeconds * 1000);
                // If the thread did not take the time between the connection attempts force it to sleep:
                if (difference < 0) {
                    try {
                        difference *= -1;
                        Thread.sleep(difference);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                remote = null;
                return 0;
            }
            return -1;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            CustomLog.d("FINISHED CONNECTION TASK (" + getCurrentTimeStamp() + ")");

            if (result == 1) {
                // Successfully connected
                displayMessage("Connection established successfully! Listening for messages.");
                new ListenForMessagesTask().execute();
            } else if (result == 0) {
                // Connection timed out
                if (!globalExit) {
                    new EstablishConnectionTask().execute();
                } else {
                    exitApp("onPostExecute in EstablishConnectionTask");
                }
            } else if (result == -1) {
                // It appears that a second task is already connected => Do nothing. This task should starve.
                CustomLog.e("FOUND SECOND CONCURRENT CONNECTION TASK => starve this task");
            }
        }

    }

    /**
     * Async task that listens for messages from the server.
     * 
     */
    private class ListenForMessagesTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (globalExit) {
                cancel(true);
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean finish = false;
            while (!finish) {
                try {

                    // Wait until we receive some data from the server:
                    while (!globalExit && !in.ready()) {
                    }
                    if (!globalExit) {
                        String read = in.readLine();
                        read = read.replaceAll("\n", "");
                        CustomLog.d("read: " + read);
                        // Check which message code was sent and handle the message:
                        if (read.startsWith(RemoteMessageCodes.MESSAGE_EXIT)) {
                            // Server told as to close the connection:
                            finish = true;
                            String message = "Received exit signal.";
                            CustomLog.d(message);
                            closeServerSocket();
                            break;
                        } else if (read.startsWith(RemoteMessageCodes.MESSAGE_SHOW_BLANK_SCREEN)) {
                            // Show blank screen via handler (we have to use the handler because this current thread
                            // cannot touch the UI thread):
                            Message m = new Message();
                            m.obj = blankScreenConstant;
                            m.what = handlerIdNextImage;
                            handler.sendMessage(m);
                            CustomLog.d("Starting to display blank screen.");
                        } else if (read.startsWith(RemoteMessageCodes.MESSAGE_DISPLAY_IMAGE)) {
                            // Display the image specified by the message from the server (also via handler):
                            Message m = new Message();
                            String image = read.replace("image:", "");
                            m.obj = image;
                            m.what = handlerIdNextImage;
                            handler.sendMessage(m);
                            CustomLog.d("Starting to display image: " + image);
                        } else if (read.startsWith(RemoteMessageCodes.MESSAGE_USE_DIRECTORY)) {
                            // Change the directory in which images are located:
                            String directory = read.replace("directory:", "");
                            currentDirectory = directory;
                            out.write("Set current directory to : " + currentDirectory + "\n");
                            out.flush();
                            CustomLog.d("Set current directory to : " + currentDirectory);
                        } else if (read.startsWith(RemoteMessageCodes.MESSAGE_COUNT_IMAGES)) {
                            // Tell the server how many images are in the currently used image directory:
                            String path = Environment.getExternalStorageDirectory().getPath() + "/" + currentDirectory;
                            try {
                                File file = new File(path);
                                if (!file.isDirectory()) {
                                    out.write("-1\n");
                                    out.flush();
                                } else {
                                    String[] files = file.list();
                                    out.write(String.valueOf(files.length) + "\n");
                                    out.flush();
                                }
                            } catch (Exception e) {
                                CustomLog.e("Error while trying to count images in directory", e);
                            }

                        }
                    }
                } catch (Exception e) {
                    CustomLog.e("Error while trying to receive message. ", e);
                    if (e.getMessage().contains("BufferedReader is closed")) {
                        CustomLog.e("Stopping listening for messages because the server stream was closed.", e);
                        break;
                    }
                }
            }
            try {
                in.close();
            } catch (IOException e) {
                CustomLog.e("Error while closing server input stream. ", e);
            }
            try {
                out.close();
            } catch (Exception e) {
                CustomLog.e("Error while closing server output stream. ", e);
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            String msg = "Finished images display. Reconnecting to server in " + timeoutForServerConnectionInSeconds
                    + " seconds...";
            Message m = new Message();
            m.obj = msg;
            m.what = handlerIdDisplayMessage;
            handler.sendMessage(m);

            try {
                if (!globalExit) {
                    Thread.sleep(timeoutForServerConnectionInSeconds * 1000);
                }
            } catch (InterruptedException e) {
            }
            if (!globalExit) {
                new EstablishConnectionTask().execute();
            } else {
                exitApp("Exit from ListenForMessagesTask");
            }
        }
    }

    /**
     * Displays the given text message:
     */
    private void displayMessage(String message) {
        textView.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.GONE);
        textView.setText(message);
        CustomLog.d(message);
    }

    /**
     * This handler is used to communicate with the UI thread. It shows images, text messages and blank screens on the
     * display. For a more detailed explanation about the concept behind the handler read
     * https://developer.android.com/training/multiple-threads/communicate-ui.html#Handler
     */
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            CustomLog.d("Handler received message with id: " + msg.what + " and object: " + msg.obj);

            textView.setVisibility(View.GONE);
            imageView.setVisibility(View.GONE);
            Message m = new Message();
            m.copyFrom(msg);

            // Read the settings that the user setup on the settings screen:
            long msBetweenRedrawing = PrefsHelper.getMsBetweenRedrawing(getApplicationContext());
            int numberOfRedrawSteps = PrefsHelper.getNumberOfRedrawSteps(getApplicationContext());

            if (msg.what == handlerIdNextImage) {
                // First step after we got a request to draw an image. Starts with screen refreshing and
                // shows the image when all refresh steps were performed.
                m.arg1 = numberOfRedrawSteps;
                m.what = handlerIdRefreshScreen;
                setBackgroundAccordingToNumber(m.arg1);
                m.arg1 = m.arg1 - 1;
                handler.sendMessage(m);
            } else if (msg.what == handlerIdRefreshScreen) {
                // Second step to draw an image: Refresh the screen.
                if (m.arg1 > 0) {
                    // Case 1: not enough refresh steps were taken. Alternate the background color and send a new
                    // message.
                    setBackgroundAccordingToNumber(m.arg1);
                    m.arg1 = m.arg1 - 1;
                    handler.sendMessageDelayed(m, msBetweenRedrawing);
                } else {
                    // Case 2: enough refresh steps were taken. Send a message to display the image.
                    m.what = handlerIdDrawImage;
                    handler.sendMessageDelayed(m, msBetweenRedrawing);
                }
            } else if (msg.what == handlerIdDrawImage) {
                // Third step to draw the image: Actually draw the image (or a blank screen).
                String fileName = (String) msg.obj;
                if (fileName.equalsIgnoreCase(blankScreenConstant)) {
                    // Show a blank screen:
                    linearLayout.setBackgroundColor(Color.WHITE);
                    out.write("displaying blank screen \n");
                    out.flush();
                    CustomLog.d("Displaying blank screen.");
                } else {
                    // Show the image that was specified in the message that came from the server:
                    String path = Environment.getExternalStorageDirectory().getPath() + "/" + fileName;
                    // TODO get path with
                    // http://developer.android.com/reference/android/os/Environment.html#getExternalStorageDirectory()
                    // Uri imageUri = Uri.parse("/sdcard/" + fileName);
                    File file = new File(path);

                    if (out != null) {
                        if (file.exists()) {
                            // Image exists:
                            Uri imageUri = Uri.parse(path);
                            imageView.setVisibility(View.VISIBLE);
                            imageView.setImageURI(imageUri);
                            out.write(RemoteMessageCodes.MESSAGE_CLIENT_DISPLAYS_IMAGE + ":" + fileName + "\n");
                            out.flush();
                            CustomLog.d("Displaying image: " + fileName);
                        } else {
                            // Image does not exit:
                            String printOut = "COULD NOT DISPLAY: " + fileName + "!!!\n";
                            out.write(printOut);
                            out.flush();
                            displayMessage(printOut);
                            CustomLog.d(printOut);
                        }
                    }
                }
            } else if (msg.what == handlerIdDisplayMessage) {
                displayMessage(String.valueOf(msg.obj));
            }

        }

    };

    /**
     * Alternates the background to be black or white.
     */
    private void setBackgroundAccordingToNumber(int number) {
        if (number % 2 == 0) {
            linearLayout.setBackgroundColor(Color.BLACK);
        } else {
            linearLayout.setBackgroundColor(Color.WHITE);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        exitApp("Back pressed");
    }

    /**
     * Closes the remote connection to the server.
     */
    private void closeServerSocket() {
        if (remote != null) {
            try {
                if (remote != null) {
                    remote.close();
                    CustomLog.d("Closed remote successfully.");
                }
                if (in != null) {
                    in.close();
                    CustomLog.d("Closed input stream from remote successfully.");
                }
                if (out != null) {
                    out.close();
                    CustomLog.d("Closed output stream from remote successfully.");
                }
            } catch (IOException e) {
                CustomLog.e("Error while closing remote.", e);
                e.printStackTrace();
            }
        }
    }

    /**
     * @return true if an SD-card is inserted and readable.
     */
    private boolean isSdReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public void onSettingsTap(View v) {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivityForResult(i, REQUEST_CODE_SETTINGS_ACTIVITY);
    }

    /**
     * Handles the result from the settings activity. Kills the app if the user pressed this button on the settings
     * screen.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SETTINGS_ACTIVITY && resultCode == RESULT_OK) {
            // Additional check which is necessary because of the hard reset:
            // (strangely onActivityResult is sometimes called after the hard reset)
            boolean appExitInSettings = PrefsHelper.getExitApp(this);
            if (data != null && data.getBooleanExtra(EXTRA_EXIT_APP, false) && appExitInSettings) {
                String msg = "Initializing app exit...";
                displayMessage(msg);
                exitApp("SettingsActivity");
            }
        }
    }

    /**
     * Closes the app and sets a variable so that all running threads will starve.
     */
    private void exitApp(String cause) {
        globalExit = true;
        CustomLog.d("App exit initiated... Cause: " + cause);
        closeServerSocket();
        CustomLog.d("Closed server socket successfully.");
        saveLogcatToFile(getApplicationContext());
        PrefsHelper.storeTimestampOfLastConnection(getApplicationContext(), 0);
        PrefsHelper.storeExitApp(getApplicationContext(), false);
        finish();
    }

    private long lastTimeLogWritten = 0;

    /**
     * This method saves the output of the logcat to the file "logcat_output.txt" on the SD card. Needs permission
     * android.permission.READ_LOGS
     * */
    @SuppressLint("NewApi")
    public void saveLogcatToFile(Context context) {
        long now = System.currentTimeMillis();
        if (now - lastTimeLogWritten > 10000) {
            String fileName = "logcat_output.txt";
            CustomLog.d("Writing log to file: " + fileName);
            try {
                File outputFile = new File(Environment.getExternalStorageDirectory(), fileName);
                Process process = Runtime.getRuntime().exec("logcat -f " + outputFile.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
            lastTimeLogWritten = now;
            CustomLog.d("Successfully wrote log to file: " + fileName);
        }
    }

    private SimpleDateFormat formattedTime = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    private String getCurrentTimeStamp() {
        long now = System.currentTimeMillis();
        return formattedTime.format(now) + " Random Instance Number: " + randomInstanceNumber;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        exitApp("onDestroy");
    }

    /**
     * Not used any more. This is replaced by {@link #initWindowFlags}.
     * */
    @Deprecated
    private void unlockScreen() {
        CustomLog.d("Trying to unlock screen...");
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Activity.KEYGUARD_SERVICE);
        KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);
        lock.disableKeyguard();
    }

}