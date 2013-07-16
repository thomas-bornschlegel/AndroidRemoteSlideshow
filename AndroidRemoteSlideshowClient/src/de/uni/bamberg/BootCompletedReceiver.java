package de.uni.bamberg;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Called when the android system booted up.
 * 
 * @author Thomas Bornschlegel
 * 
 */
public class BootCompletedReceiver extends BroadcastReceiver {

    /**
     * Called when the android system booted up. Starts {@link NookImageDisplayerActivity}.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, NookImageDisplayerActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }
}