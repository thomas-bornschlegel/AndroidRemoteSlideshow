package de.uni.bamberg;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
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
    private CheckBox checkCloseApps;
    private CheckBox checkAdvancedRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        editTextIp = (EditText) findViewById(R.id.editTextIp);
        editTextPort = (EditText) findViewById(R.id.editTextPort);
        editTextMsBetweenRedrawing = (EditText) findViewById(R.id.editTextMsBetweenRedrawing);
        editNumberOfRedrawSteps = (EditText) findViewById(R.id.editNumberOfRedrawSteps);
        editClientId = (EditText) findViewById(R.id.editClientId);
        checkCloseApps = (CheckBox) findViewById(R.id.checkBoxCloseApps);
        checkAdvancedRefresh = (CheckBox) findViewById(R.id.checkBoxRefreshMode);
        editTextIp.setText(PrefsHelper.getServerIp(this));
        editTextPort.setText(String.valueOf(PrefsHelper.getServerPort(this)));
        editTextMsBetweenRedrawing.setText(String.valueOf(PrefsHelper.getMsBetweenRedrawing(this)));
        editNumberOfRedrawSteps.setText(String.valueOf(PrefsHelper.getNumberOfRedrawSteps(this)));
        editClientId.setText(String.valueOf(PrefsHelper.getClientId(this)));
        checkCloseApps.setChecked(PrefsHelper.getCloseBnApps(this));
        checkAdvancedRefresh.setChecked(PrefsHelper.getAdvancedRefresh(this));
    }

    public void onOkClick(View v) {
        String serverIp = editTextIp.getText().toString();
        int serverPort = Integer.valueOf(editTextPort.getText().toString());
        long msBetweenRedrawing = Long.valueOf(editTextMsBetweenRedrawing.getText().toString());
        int numberOfRedrawSteps = Integer.valueOf(editNumberOfRedrawSteps.getText().toString());
        int clientId = Integer.valueOf(editClientId.getText().toString());
        boolean closeApps = checkCloseApps.isChecked();
        boolean advancedRefresh = checkAdvancedRefresh.isChecked();

        PrefsHelper.storeSettings(getApplicationContext(), serverIp, serverPort, msBetweenRedrawing,
                numberOfRedrawSteps, clientId, closeApps, advancedRefresh);

        finish();
    }

    public void onCancelClick(View v) {
        finish();
    }

    public void onKillSwitchClicked(View v) {
        Intent i = new Intent();
        i.putExtra(NookImageDisplayerActivity.EXTRA_EXIT_APP, true);
        setResult(RESULT_OK, i);
        PrefsHelper.storeExitApp(getApplicationContext(), true);
        finish();
    }

}
