package org.goods2go.android.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartServiceAfterBootReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Intent startServiceIntent = new Intent(context, NotificationService.class);
            context.startService(startServiceIntent);
        }
    }

}
