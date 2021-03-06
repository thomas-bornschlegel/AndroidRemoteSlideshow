package de.uni.bamberg;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import de.uni.bamberg.helper.PrefsHelper;

/**
 * This activity displays the settings screen.
 * 
 * @author Thomas Bornschlegel
 * 
 */
public class SettingsActivity extends Activity {

    private EditText editTextIp;
    private EditText editTextPort;
    private EditText editTextMsBetweenRedrawing;
    private EditText editNumberOfRedrawSteps;
    private EditText editClientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        // Find the views:
        editTextIp = (EditText) findViewById(R.id.editTextIp);
        editTextPort = (EditText) findViewById(R.id.editTextPort);
        editTextMsBetweenRedrawing = (EditText) findViewById(R.id.editTextMsBetweenRedrawing);
        editNumberOfRedrawSteps = (EditText) findViewById(R.id.editNumberOfRedrawSteps);
        editClientId = (EditText) findViewById(R.id.editClientId);
        // Set the currently stored settings:
        editTextIp.setText(PrefsHelper.getServerIp(this));
        editTextPort.setText(String.valueOf(PrefsHelper.getServerPort(this)));
        editTextMsBetweenRedrawing.setText(String.valueOf(PrefsHelper.getMsBetweenRedrawing(this)));
        editNumberOfRedrawSteps.setText(String.valueOf(PrefsHelper.getNumberOfRedrawSteps(this)));
        editClientId.setText(String.valueOf(PrefsHelper.getClientId(this)));
    }

    public void onOkClick(View v) {
        String serverIp = editTextIp.getText().toString();
        int serverPort = Integer.valueOf(editTextPort.getText().toString());
        long msBetweenRedrawing = Long.valueOf(editTextMsBetweenRedrawing.getText().toString());
        int numberOfRedrawSteps = Integer.valueOf(editNumberOfRedrawSteps.getText().toString());
        int clientId = Integer.valueOf(editClientId.getText().toString());

        PrefsHelper.storeSettings(getApplicationContext(), serverIp, serverPort, msBetweenRedrawing,
                numberOfRedrawSteps, clientId);

        finish();
    }

    public void onCancelClick(View v) {
        finish();
    }

    /**
     * Kills the App completely, so that all running threads are destroyed.
     * 
     * @param v
     */
    public void onKillSwitchClicked(View v) {
        Intent i = new Intent();
        i.putExtra(NookImageDisplayerActivity.EXTRA_EXIT_APP, true);
        setResult(RESULT_OK, i);
        PrefsHelper.storeExitApp(getApplicationContext(), true);
        finish();
    }

}
