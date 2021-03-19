package org.goods2go.android.network;

import android.util.Log;

import com.goods2go.authentication.SessionItem;
import com.goods2go.authentication.SessionResponse;
import com.goods2go.models.Address;
import com.goods2go.models.NotificationMessage;
import com.goods2go.models.Shipment;
import com.goods2go.models.ShipmentAnnouncement;
import com.goods2go.models.ShipmentRequest;
import com.goods2go.models.ShipmentSize;
import com.goods2go.models.ShipmentSubscription;
import com.goods2go.models.User;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static org.goods2go.android.Configuration.HTTP_BACKEND_ADDRESS;

public class NetworkClient {

    private static final String TAG = "NETWORKCLIENT";

    private SessionItem sessionItem;

    public boolean isSessionItemSet(){
        return (sessionItem != null);
    }

    public void setSessionItem (SessionItem sessionItem){
        this.sessionItem = sessionItem;
    }

    /*
     *   User related
     */

    public User signUp(User user) throws NetworkException {
        String url = HTTP_BACKEND_ADDRESS +"/user/signup";
        return HttpClient.post(url, user, User.class);
    }

    public SessionItem signIn(String email, String password) throws NetworkException {
        String url = HTTP_BACKEND_ADDRESS +"/login";
        User login = new User(email, password);
        SessionResponse result = HttpClient.post(url, login, SessionResponse.class);
        return result.getItem();
    }

    public User getUser() throws NetworkException {
        String url = HTTP_BACKEND_ADDRESS +"/user/get";
        return HttpClient.get(url, sessionItem.getToken(), User.class);
    }

    public List<Address> getUserAddressHistory() throws NetworkException {
        String url = HTTP_BACKEND_ADDRESS +"/user/addresshistory";
        Type type = new TypeToken<List<Address>>(){}.getType();
        return HttpClient.get(url, sessionItem.getToken(), type);
    }

    public User changePassword(User user) throws NetworkException {
        String url = HTTP_BACKEND_ADDRESS +"/user/password";
        return HttpClient.post(url, sessionItem.getToken(), user, User.class);
    }

    public User changeUserData(User user) throws NetworkException{
        String url = HTTP_BACKEND_ADDRESS +"/user/update";
        return HttpClient.post(url, sessionItem.getToken(), user, User.class);
    }

    public User becomeDeliverer(User user) throws NetworkException {
        String url = HTTP_BACKEND_ADDRESS +"/user/becomedeliverer";
        return HttpClient.post(url, sessionItem.getToken(), user, User.class);
    }

    /*
     *  Sender related
     */

    public Shipment getShipment(long shipmentId)
            throws NetworkException {
        String url = HTTP_BACKEND_ADDRESS +"/shipment/get/"+shipmentId;
        return HttpClient.get(url, sessionItem.getToken(), Shipment.class);
    }

    public ShipmentAnnouncement getShipmentAnnouncement(long announcementId)
            throws NetworkException {
        String url = HTTP_BACKEND_ADDRESS +"/shipmentannouncement/get/"+announcementId;
        return HttpClient.get(url, sessionItem.getToken(), ShipmentAnnouncement.class);
    }

    public List<ShipmentAnnouncement> getOpenAnnouncements() throws NetworkException {
        String url = HTTP_BACKEND_ADDRESS +"/shipmentannouncement/getall";
        Type type = new TypeToken<List<ShipmentAnnouncement>>(){}.getType();
        return HttpClient.get(url, sessionItem.getToken(), type);
    }

    public ShipmentAnnouncement announceShipment(ShipmentAnnouncement shipmentAnnouncement)
            throws NetworkException {
        String url = HTTP_BACKEND_ADDRESS +"/shipmentannouncement/save";
        return HttpClient.post(url, sessionItem.getToken(),shipmentAnnouncement,
                ShipmentAnnouncement.class);
    }

    public boolean revokeAnnouncement(ShipmentAnnouncement shipmentAnnouncement)
            throws NetworkException {
        String url = HTTP_BACKEND_ADDRESS +"/shipmentannouncement/revoke";
        Boolean result = HttpClient.post(url, sessionItem.getToken(),
                shipmentAnnouncement, Boolean.class);
        if(result != null){
            return result;
        }
        return false;
    }

    public Shipment acceptShipmentRequest(ShipmentRequest shipmentRequest) throws NetworkException {
        String url = HTTP_BACKEND_ADDRESS +"/shipmentrequest/accept";
        return HttpClient.post(url, sessionItem.getToken(), shipmentRequest, Shipment.class);
    }

    public List<Shipment> getPendingSenderShipments() throws NetworkException {
        String url = HTTP_BACKEND_ADDRESS +"/shipment/sender/getpending";
        Type type = new TypeToken<List<Shipment>>(){}.getType();
        return HttpClient.get(url, sessionItem.getToken(), type);
    }

    public List<Shipment> getActiveSenderShipments() throws NetworkException {
        String url = HTTP_BACKEND_ADDRESS +"/shipment/sender/getactive";
        Type type = new TypeToken<List<Shipment>>(){}.getType();
        return HttpClient.get(url, sessionItem.getToken(), type);
    }

    public List<Shipment> getClosedSenderShipments() throws NetworkException {
        String url = HTTP_BACKEND_ADDRESS +"/shipment/sender/getclosed";
        Type type = new TypeToken<List<Shipment>>(){}.getType();
        return HttpClient.get(url, sessionItem.getToken(), type);
    }

    public Shipment rateShipmentAsSender(Shipment shipment) throws NetworkException {
        String url = HTTP_BACKEND_ADDRESS +"/shipment/sender/rate";
        return HttpClient.post(url, sessionItem.getToken(), shipment, Shipment.class);
    }


    /*
     *  Deliverer related
     */

    /*
    public List<ShipmentAnnouncement> getAnnouncements(ShipmentSubscription filter)
            throws NetworkException {
        String url = HTTP_BACKEND_ADDRESS +"/shipmentannouncement/getfiltered";
        Type type = new TypeToken<List<ShipmentAnnouncement>>(){}.getType();
        return HttpClient.post(url, sessionItem.getToken(), filter, type);
    }
    */

    public List<ShipmentAnnouncement> getAnnouncements(ShipmentSubscription filter)
            throws NetworkException {
        String url = HTTP_BACKEND_ADDRESS +"/shipmentannouncement/find";
        List<ShipmentSubscription> list = new ArrayList<>();
        list.add(filter);
        Type type = new TypeToken<List<ShipmentAnnouncement>>(){}.getType();
        return HttpClient.post(url, sessionItem.getToken(), list , type);
    }

    public ShipmentRequest sendShipmentRequest(ShipmentRequest shipmentRequest)
            throws NetworkException {
        String url = HTTP_BACKEND_ADDRESS +"/shipmentrequest/save";
        return HttpClient.post(url, sessionItem.getToken(),
                shipmentRequest, ShipmentRequest.class);
    }

    public List<ShipmentRequest> getOpenRequests() throws NetworkException {
        String url = HTTP_BACKEND_ADDRESS +"/shipmentrequest/getall";
        Type type = new TypeToken<List<ShipmentRequest>>(){}.getType();
        return HttpClient.get(url, sessionItem.getToken(), type);
    }

    public boolean revokeShipmentRequest(ShipmentRequest shipmentRequest) throws NetworkException {
        String url = HTTP_BACKEND_ADDRESS +"/shipmentrequest/delete";
        Boolean result = HttpClient.post(url,sessionItem.getToken(),shipmentRequest,Boolean.class);
        if(result != null){
            return result;
        }
        return false;
    }

    public Shipment rateShipmentAsDeliverer(Shipment shipment) throws NetworkException {
        String url = HTTP_BACKEND_ADDRESS +"/shipment/deliverer/rate";
        return HttpClient.post(url, sessionItem.getToken(), shipment, Shipment.class);
    }

    /*
     *  Deliverer related - shipment related
     */

    public List<Shipment> getPendingDelivererShipments() throws NetworkException {
        String url = HTTP_BACKEND_ADDRESS +"/shipment/deliverer/getpending";
        Type type = new TypeToken<List<Shipment>>(){}.getType();
        return HttpClient.get(url, sessionItem.getToken(), type);
    }

    public boolean pickupShipment(Shipment shipment) throws NetworkException {
        String url = HTTP_BACKEND_ADDRESS +"/shipment/pickup";
        Boolean result = HttpClient.post(url, sessionItem.getToken(), shipment, Boolean.class);
        if(result != null){
            return result;
        }
        return false;
    }

    public List<Shipment> getActiveDelivererShipments() throws NetworkException {
        String url = HTTP_BACKEND_ADDRESS +"/shipment/deliverer/getactive";
        Type type = new TypeToken<List<Shipment>>(){}.getType();
        return HttpClient.get(url, sessionItem.getToken(), type);
    }

    public boolean deliverShipment(Shipment shipment) throws NetworkException {
        String url = HTTP_BACKEND_ADDRESS +"/shipment/deliver";
        Boolean result = HttpClient.post(url, sessionItem.getToken(), shipment, Boolean.class);
        if(result != null){
            return result;
        }
        return false;
    }

    public List<Shipment> getClosedDelivererShipments() throws NetworkException {
        String url = HTTP_BACKEND_ADDRESS +"/shipment/deliverer/getclosed";
        Type type = new TypeToken<List<Shipment>>(){}.getType();
        return HttpClient.get(url, sessionItem.getToken(), type);
    }

    /*
     *  Deliverer related - subscription related
     */

    public ShipmentSubscription subscribeShipments(ShipmentSubscription shipmentSubscription)
            throws NetworkException {
        String url = HTTP_BACKEND_ADDRESS +"/shipmentsubscription/save";
        return HttpClient.post(url, sessionItem.getToken(), shipmentSubscription,
                ShipmentSubscription.class);
    }

    public ShipmentSubscription getSubscription(ShipmentSubscription shipmentSubscription)
            throws NetworkException {
        String url = HTTP_BACKEND_ADDRESS +"/shipmentsubscription/save";
        return HttpClient.get(url, sessionItem.getToken(), ShipmentSubscription.class);
    }

    public List<ShipmentSubscription> getShipmentSubscriptions() throws NetworkException {
        String url = HTTP_BACKEND_ADDRESS +"/shipmentsubscription/getAll";
        Type type = new TypeToken<List<ShipmentSubscription>>(){}.getType();
        return HttpClient.get(url, sessionItem.getToken(), type);
    }

    public boolean revokeShipmentSubscription(ShipmentSubscription shipmentSubscription)
            throws NetworkException {
        String url = HTTP_BACKEND_ADDRESS +"/shipmentsubscription/delete";
        Boolean result = HttpClient.post(url, sessionItem.getToken(), shipmentSubscription, Boolean.class);
        if(result != null){
            return result;
        }
        return false;
    }

    /*
     * Other
     */

    public boolean confirmNotificationMessage(NotificationMessage notificationMessage)
            throws NetworkException {
        String url = HTTP_BACKEND_ADDRESS +"/notificationmessage/confirm";
        Boolean result = HttpClient.post(url, sessionItem.getToken(), notificationMessage, Boolean.class);
        if(result != null){
            return result;
        }
        return false;
    }

    public List<ShipmentSize> getShipmentSizes()
            throws NetworkException {
        String url = HTTP_BACKEND_ADDRESS +"/shipmentsize/all";
        Type type = new TypeToken<List<ShipmentSize>>(){}.getType();
        return HttpClient.get(url, type);
    }


    private static void simulateNetwork(){
        try {
            // Simulate network access.
            Thread.sleep(800);
        } catch (InterruptedException e) {
            Log.e("WAITTHREAD", e.getMessage());
        }
    }
}
