package org.goods2go.android.notification;

import android.util.ArrayMap;

import com.goods2go.authentication.SessionItem;

import org.goods2go.android.Configuration;
import org.goods2go.android.network.NetworkException;
import org.goods2go.android.network.NetworkTask;
import org.goods2go.android.notification.stomp.Stomp;
import org.goods2go.android.notification.stomp.Subscription;

import java.util.Map;

public class NotificationClient {

    private Stomp stomp;
    private NotificationService service;

    public NotificationClient(NotificationService service){
        this.service = service;
    }

    public void connectAndSubscribe(SessionItem sessionItem){
        Map header = new ArrayMap<String, String>();
        header.put(Configuration.AUTHORIZATION_HEADER, sessionItem.getToken());

        stomp = new Stomp(
                Configuration.STOMP_ADDRESS,
                header,
                new Stomp.ListenerWSNetwork() {
                    @Override
                    public void onState(int state) {
                        System.out.println("STOMP STATE: " + state);
                    }
                });
        new StompConnectAndSubscribeTask().execute();

    }

    public void disconnect(){
        new StompDisconnectTask().execute();
    }

    private class StompConnectAndSubscribeTask extends NetworkTask<Void, Void, Void> {

        @Override
        protected Void runInBackground(Void[] voids) throws NetworkException {
            stomp.connect();
            stomp.subscribe(new Subscription(
                    Configuration.STOMP_TOPIC,
                    new MessageReceivedListener(service)));
            return null;
        }
    }

    private class StompDisconnectTask extends NetworkTask<Void, Void, Void> {

        @Override
        protected Void runInBackground(Void[] voids) throws NetworkException {
            stomp.disconnect();
            return null;
        }
    }
}
