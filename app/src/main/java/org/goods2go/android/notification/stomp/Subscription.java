package org.goods2go.android.notification.stomp;

import java.util.Map;

public class Subscription {

    public interface ListenerSubscription {
        public void onMessage(Map<String, String> headers, String body);
    }

    private String id;

    private String destination;

    private ListenerSubscription callback;

    public Subscription(String destination, ListenerSubscription callback){
        this.destination = destination;
        this.callback = callback;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getDestination() {
        return destination;
    }

    public ListenerSubscription getCallback() {
        return callback;
    }
}
