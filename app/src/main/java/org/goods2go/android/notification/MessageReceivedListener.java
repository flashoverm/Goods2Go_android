package org.goods2go.android.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.goods2go.models.NotificationMessage;
import com.goods2go.models.enums.NotificationMessageType;

import org.goods2go.android.R;
import org.goods2go.android.network.NetworkException;
import org.goods2go.android.network.NetworkTask;
import org.goods2go.android.notification.stomp.Subscription;
import org.goods2go.android.ui.activity.DelivererActivity;
import org.goods2go.android.ui.activity.RoleChooseActivity;
import org.goods2go.android.ui.activity.SenderActivity;
import org.goods2go.android.util.CustomGson;
import org.goods2go.android.util.StringLookup;

import java.net.HttpURLConnection;
import java.util.Map;

public class MessageReceivedListener implements Subscription.ListenerSubscription {

    public static final String OPEN_TYPE = "opentype";
    public static final int OPEN_SHIPMENT = 0;
    public static final int OPEN_ANNOUNCEMENT_LIST = 1;
    public static final int OPEN_ANNOUNCEMENT_REQUEST = 2;
    public static final String OPEN_ID = "openid";

    private NotificationService service;

    public MessageReceivedListener(NotificationService notificationService){
        this.service = notificationService;
    }

    @Override
    public void onMessage(Map<String, String> headers, String body) {
        NotificationMessage message = CustomGson.build().fromJson(body, NotificationMessage.class);
        Log.i(NotificationService.TAG, "Message received");
        new ConfirmNotificationTask().execute(message);
    }

    private class ConfirmNotificationTask extends NetworkTask<NotificationMessage, Void, NotificationMessage> {

        @Override
        protected NotificationMessage runInBackground(NotificationMessage[] params)
                throws NetworkException {
            NotificationMessage message = params[0];
            if(service.getNetworkClient().confirmNotificationMessage(message)){
                return message;
            }
            return null;
        }

        @Override
        protected void onPostExecute(NotificationMessage result) {
            /*FIXME Workaround - Websocket is not closed until a confirmation request fails!
            The last message is sent to the client although the session is expired! */
            if(networkException != null
                && networkException.httpError == HttpURLConnection.HTTP_UNAUTHORIZED){
                //session expired
                service.stopListening();
            } else if(result != null){
                showNotification(result);
            }
        }
    }

    private void showNotification(NotificationMessage message){
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                service,
                NotificationChannel.DEFAULT_CHANNEL_ID)
                .setSmallIcon(R.drawable.g2g_logo)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSound(alarmSound)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(service);
        notificationManager.notify(112, prepareNotification(builder, message));
    }

    private Notification prepareNotification(
            NotificationCompat.Builder builder,
            NotificationMessage message) {

        NotificationMessageType type = message.getType();
        if (type == NotificationMessageType.NewRequest
                || type == NotificationMessageType.RequestAccepted
                || type == NotificationMessageType.RequestDeclined) {

            builder = buildRequestNotification(builder, message);

        } else if (type == NotificationMessageType.ShipmentPickedUp
                || type == NotificationMessageType.ShipmentDelivered
                || type == NotificationMessageType.ShipmentSenderRated
                || type == NotificationMessageType.ShipmentDelivererRated) {

            builder = buildShipmentNotification(builder, message);

        } else if (type == NotificationMessageType.FittingAnnouncement) {
            builder.setContentTitle(service.getString(StringLookup.getIdFrom(type)));
            builder.setContentText(service.getString(StringLookup.getIdFrom(type)));
            builder.setContentIntent(createIntent(
                    DelivererActivity.class, OPEN_ANNOUNCEMENT_REQUEST, message.getSubjectId()));

        } else {
            builder.setContentTitle(service.getString(StringLookup.getIdFrom(type)));
            builder.setContentText(service.getString(StringLookup.getIdFrom(type)));
            builder.setContentIntent(
                    createIntent(RoleChooseActivity.class, -1, -1));
        }
        return builder.build();
    }

    private NotificationCompat.Builder buildShipmentNotification(
            NotificationCompat.Builder builder,
            NotificationMessage message){

        builder.setContentTitle(service.getString(R.string.text_shipment)
                + " "
                + service.getString(StringLookup.getIdFrom(message.getType()))
        );
        builder.setContentText(service.getString(R.string.text_shipment)
                + " \"" + message.getSubjectDescription() + "\" "
                + service.getString(StringLookup.getIdFrom(message.getType()))
        );

        Class intentClass;
        if(message.getType() == NotificationMessageType.ShipmentSenderRated){
            intentClass = DelivererActivity.class;
        } else {
            intentClass = SenderActivity.class;
        }
        builder.setContentIntent(createIntent(intentClass, OPEN_SHIPMENT, message.getSubjectId()));
        return builder;
    }

    private NotificationCompat.Builder buildRequestNotification(
            NotificationCompat.Builder builder,
            NotificationMessage message){

        builder.setContentTitle(service.getString(R.string.text_reqeust)
                + " "
                + service.getString(StringLookup.getIdFrom(message.getType()))
        );
        builder.setContentText(service.getString(R.string.text_reqeust)
                + " " + service.getString(R.string.text_reqeust_to)
                + " \"" + message.getSubjectDescription() + "\" "
                + service.getString(StringLookup.getIdFrom(message.getType()))
        );

        if(message.getType() == NotificationMessageType.NewRequest){
            builder.setContentIntent(
                    createIntent(SenderActivity.class, OPEN_ANNOUNCEMENT_LIST, message.getSubjectId()));
        } else if(message.getType() == NotificationMessageType.RequestAccepted) {
            builder.setContentIntent(
                    createIntent(DelivererActivity.class, OPEN_SHIPMENT, message.getSubjectId()));
        } else {
            builder.setContentIntent(
                    createIntent(RoleChooseActivity.class, -1, -1));
        }
        return builder;
    }

    private PendingIntent createIntent(Class intentClass, int openType, long openId){
        Intent intent = new Intent(service, intentClass);
        intent.putExtra(OPEN_TYPE, openType);
        intent.putExtra(OPEN_ID, openId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        return PendingIntent.getActivity(service, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
