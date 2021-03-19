package org.goods2go.android.notification;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.goods2go.authentication.SessionItem;

import org.goods2go.android.network.NetworkClient;
import org.goods2go.android.util.OfflineStorage;

public class NotificationService extends Service {

    public static final String TAG = "NotificationService";

    private NotificationClient notificationClient;
    private NetworkClient networkClient;

    public NetworkClient getNetworkClient() {
        return networkClient;
    }

    @Override
    public void onCreate() {
        networkClient = new NetworkClient();
        notificationClient = new NotificationClient(this);
        Log.i(TAG, "Service onCreate");
        startListening();
    }

    public void startListening(){
        SessionItem sessionItem = OfflineStorage.loadSessionItem(this);
        if(sessionItem != null){
            networkClient.setSessionItem(sessionItem);
            notificationClient.connectAndSubscribe(sessionItem);
            Log.i(TAG, "Start listening to notifications");
        } else {
            Log.i(TAG, "User not logged in - can't listen to notifications");
        }
    }

    public void stopListening(){
        if(notificationClient != null){
            Log.i(TAG, "Stop listening to notifications");
            notificationClient.disconnect();
        }
    }

    @Override
    public void onDestroy() {
        stopListening();
        super.onDestroy();
    }

    public class NotificationServiceBinder extends Binder {
        public NotificationService getService() {
            return NotificationService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new NotificationServiceBinder();
    }
}
