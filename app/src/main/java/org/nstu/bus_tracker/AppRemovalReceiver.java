package org.nstu.bus_tracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AppRemovalReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
            String packageName = intent.getData().getSchemeSpecificPart();
            if (packageName.equals(context.getPackageName())) {
                Intent serviceIntent = new Intent(context, LocationUpdateService.class);
                context.stopService(serviceIntent);
            }
        }
    }
}
