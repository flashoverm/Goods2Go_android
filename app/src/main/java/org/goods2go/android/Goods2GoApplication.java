package org.goods2go.android;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.goods2go.authentication.SessionItem;
import com.goods2go.models.ShipmentSize;
import com.goods2go.models.User;

import org.goods2go.android.network.NetworkClient;
import org.goods2go.android.network.NetworkException;
import org.goods2go.android.network.NetworkTask;
import org.goods2go.android.notification.NotificationService;
import org.goods2go.android.ui.activity.LoginActivity;
import org.goods2go.android.util.OfflineStorage;

import java.util.List;

public class Goods2GoApplication extends Application {

    private NetworkClient networkClient;
    private User currentUser;
    private NotificationService notificationService;

    private List<ShipmentSize> shipmentSizes;

    private ServiceConnection serviceConnector;

    @Override
    public void onCreate() {
        super.onCreate();
        networkClient = new NetworkClient();
        currentUser = null;
        new GetShipmentSizesTask().execute();

        serviceConnector = new ServiceConnection() {
            public void onServiceConnected(ComponentName className,
                                           IBinder service) {
                Log.i(NotificationService.TAG, "Service connected");
                NotificationService.NotificationServiceBinder binder
                        = (NotificationService.NotificationServiceBinder) service;
                notificationService = binder.getService();
            }

            public void onServiceDisconnected(ComponentName arg0) {
                Log.i(NotificationService.TAG, "Service disconnected");
                notificationService = null;
            }
        };

        if(notificationService == null){
            Log.i(NotificationService.TAG, "Try starting service");
            Intent startServiceIntent = new Intent(this, NotificationService.class);
            startService(startServiceIntent);
            //bindService(startServiceIntent, serviceConnector, Context.BIND_AUTO_CREATE);
            bindService(startServiceIntent, serviceConnector, 0);
        }
    }

    public static NetworkClient getNetworkClient(Activity activity){
        NetworkClient networkClient = getApplication(activity).networkClient;
        if(!networkClient.isSessionItemSet()){
            networkClient.setSessionItem(OfflineStorage.loadSessionItem(activity));
        }
        return networkClient;
    }

    public static User getCurrentUser(Activity activity){
        if(getApplication(activity).currentUser == null){
            setCurrentUser(activity, OfflineStorage.loadUser(activity));
        }
        return getApplication(activity).currentUser;
    }

    public static List<ShipmentSize> getShipmentSizes(Activity activity){
        return getApplication(activity).shipmentSizes;
    }

    public static void setCurrentUser(Activity activity, User user) {
        OfflineStorage.saveUser(activity, user);
        getApplication(activity).currentUser = user;
    }

    public static void login(Activity activity, SessionItem sessionItem){
        setSessionItem(activity, sessionItem);
        if(getApplication(activity).notificationService != null){
            getApplication(activity).notificationService.startListening();
        } else {
            Log.i(NotificationService.TAG, "Login: Can start listening - service not present!");
        }
    }

    public static void logout(Activity activity){
        Goods2GoApplication.setSessionItem(activity, null);
        if(getApplication(activity).notificationService != null){
            getApplication(activity).notificationService.stopListening();
        } else {
            Log.i(NotificationService.TAG, "Logout: Can stop listening - service not present!");
        }
        Intent intent = new Intent(activity, LoginActivity.class);
        activity.startActivity(intent);
        activity.finish();
    }

    private static void setSessionItem(Activity activity, SessionItem sessionItem){
        getNetworkClient(activity).setSessionItem(sessionItem);
        OfflineStorage.saveSessionItem(activity, sessionItem);
    }

    private static Goods2GoApplication getApplication(Activity activity){
        return (Goods2GoApplication)activity.getApplication();
    }

    private class GetShipmentSizesTask extends NetworkTask<Void, Void, List<ShipmentSize>>{

        @Override
        protected List<ShipmentSize> runInBackground(Void[] voids) throws NetworkException {
            return networkClient.getShipmentSizes();
        }

        @Override
        protected void onPostExecute(List<ShipmentSize> result) {
            if(result != null){
                Goods2GoApplication.this.shipmentSizes = result;
                OfflineStorage.saveSizes(getApplicationContext(), result);
            } else {
                Goods2GoApplication.this.shipmentSizes =
                        OfflineStorage.loadSizes(getApplicationContext());
            }
        }
    }
}
