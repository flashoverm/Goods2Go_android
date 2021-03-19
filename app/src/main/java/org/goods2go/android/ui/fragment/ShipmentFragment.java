package org.goods2go.android.ui.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;

import com.goods2go.models.DelivererRating;
import com.goods2go.models.PaymentInformation;
import com.goods2go.models.SenderRating;
import com.goods2go.models.Shipment;
import com.goods2go.models.ShipmentAnnouncement;
import com.goods2go.models.User;
import com.goods2go.models.enums.PaymentType;
import com.goods2go.models.enums.Status;
import com.goods2go.models.util.DateTime;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import org.goods2go.android.Goods2GoApplication;
import org.goods2go.android.R;
import org.goods2go.android.geo.StaticMapClient;
import org.goods2go.android.network.NetworkException;
import org.goods2go.android.network.NetworkTask;
import org.goods2go.android.ui.activity.NetworkActivity;
import org.goods2go.android.ui.view.AddressView;
import org.goods2go.android.ui.view.ShipmentDetailView;
import org.goods2go.android.util.StringLookup;

import static org.goods2go.android.Configuration.QR_SIZE;

public class ShipmentFragment extends LowerLevelFragment{

    public static ShipmentFragment newInstance() {
        ShipmentFragment fragment = new ShipmentFragment();
        return fragment;
    }

    private Shipment shipment;
    private ShipmentAnnouncement shipmentAnnouncement;
    private User currentUser;

    private ShipmentDetailView shipmentDetailView;

    private AddressView source;
    private TextView textPickupDateTime;
    private TextView pickupDateTime;
    private TextView textPickupUntil;
    private TextView pickupUntil;

    private AddressView destination;
    private TextView textDeliveryDateTime;
    private TextView deliveryDateTime;
    private TextView textCoordinates;
    private TextView coordinates;
    private ImageView deliveryMap;

    private TextView paymentType;
    private TextView paymentAccount;

    private ImageView paymentDivider;
    private LinearLayout layoutPayment;
    private LinearLayout layoutRating;

    private Button showQr;
    private ImageView qrView;

    private ImageView buttonDivider;
    private Button revoke;

    private ImageView ratingDivider;
    private RatingBar sendersRating;
    private RatingBar deliverersRating;

    private RateAsDelivererTask rateAsDelivererTask;
    private RateAsSenderTask rateAsSenderTask;
    private RevokeAnnouncementTask revokeAnnouncementTask;

    public void setShipment(Shipment shipment){
        this.shipment = shipment;
    }

    public void setShipmentAnnouncement(ShipmentAnnouncement shipmentAnnouncement){
        this.shipmentAnnouncement = shipmentAnnouncement;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shipment, container, false);
        progress = view.findViewById(R.id.progress);
        content = view.findViewById(R.id.content);

        setTitle(R.string.text_shipment_details);

        initShipmentView(view);

        if(shipment != null) {
            displayShipment();
        }else if(shipmentAnnouncement != null){
            displayAnnouncement();
        }

        return view;
    }

    private void initShipmentView(View view){
        shipmentDetailView = view.findViewById(R.id.shipment_detail);

        source = view.findViewById(R.id.source);
        destination = view.findViewById(R.id.destination);
        textDeliveryDateTime = view.findViewById(R.id.text_delivery_datetime);
        deliveryDateTime = view.findViewById(R.id.delivery_datetime);
        textPickupDateTime = view.findViewById(R.id.text_pickup_datetime);
        pickupDateTime = view.findViewById(R.id.pickup_datetime);
        pickupUntil = view.findViewById(R.id.latest_pickup_date);
        textPickupUntil = view.findViewById(R.id.text_latest_pickup_date);
        textCoordinates = view.findViewById(R.id.text_delivery_coordinates);
        coordinates = view.findViewById(R.id.delivery_coordinates);
        deliveryMap = view.findViewById(R.id.delivery_map);

        showQr = view.findViewById(R.id.button_show_qr);
        qrView = view.findViewById(R.id.qrView);

        paymentType = view.findViewById(R.id.payment_type);
        paymentAccount = view.findViewById(R.id.payment_account);
        paymentDivider = view.findViewById(R.id.divider_payment);
        layoutPayment = view.findViewById(R.id.layout_payment);
        layoutRating = view.findViewById(R.id.layout_rating);

        ratingDivider = view.findViewById(R.id.divider_rating);
        sendersRating = view.findViewById(R.id.senders_rating);
        deliverersRating = view.findViewById(R.id.deliverers_rating);

        buttonDivider = view.findViewById(R.id.divider_button);
        revoke = view.findViewById(R.id.button_revoke);
    }

    private void displayAnnouncement(){
        shipmentDetailView.setAnnouncement(shipmentAnnouncement);

        pickupUntil.setVisibility(View.VISIBLE);
        textPickupUntil.setVisibility(View.VISIBLE);

        textPickupDateTime.setText(R.string.text_pickup_from);
        textDeliveryDateTime.setText(R.string.text_deliver_until);

        source.setAddress(shipmentAnnouncement.getSource());
        pickupDateTime.setText(
                DateTime.DATE_FORMAT.format(shipmentAnnouncement.getEarliestpickupdate()));
        pickupUntil.setText(
                DateTime.DATE_FORMAT.format(shipmentAnnouncement.getLatestpickupdate()));

        destination.setAddress(shipmentAnnouncement.getDestination());
        deliveryDateTime.setText(
                DateTime.DATE_FORMAT.format(shipmentAnnouncement.getLatestdeliverydate()));


        buttonDivider.setVisibility(View.VISIBLE);
        revoke.setVisibility(View.VISIBLE);
        revoke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(revokeAnnouncementTask == null && shipmentAnnouncement != null){
                    showProgress(true);
                    revokeAnnouncementTask = new RevokeAnnouncementTask();
                    revokeAnnouncementTask.execute(shipmentAnnouncement);
                }
            }
        });
    }

    private void displayShipment() {
        shipmentDetailView.setShipment(shipment);
        shipmentDetailView.showState();

        source.setAddress(shipment.getSource());
        destination.setAddress(shipment.getDestination());

        if (shipment.getPickupdatetime() == null) {
            pickupDateTime.setText(
                    DateTime.DATETIME_FORMAT.format(shipment.getNegpickupdatetime()));
        } else {
            pickupDateTime.setText(
                    DateTime.DATETIME_FORMAT.format(shipment.getPickupdatetime()));
        }
        if (shipment.getDeliverydatetime() == null) {
            deliveryDateTime.setText(
                    DateTime.DATETIME_FORMAT.format(shipment.getNegdeliverydatetime()));
        } else {
            deliveryDateTime.setText(
                    DateTime.DATETIME_FORMAT.format(shipment.getDeliverydatetime()));
        }

        if (shipment.getDeliverycoordinates() != null) {
            coordinates.setVisibility(View.VISIBLE);
            textCoordinates.setVisibility(View.VISIBLE);
            coordinates.setText(shipment.getDeliverycoordinates());
            System.out.println("Thread0: " + Thread.currentThread().getId());
            new GetDeliveryMapTask().execute(shipment.getDeliverycoordinates());
        }

        currentUser = Goods2GoApplication.getCurrentUser(getActivity());
        /*
        if(currentUser.getEmail().equals(shipment.getDeliverer().getEmail())){
            displayDelivererRelatedInformation();
        }
        if(currentUser.getEmail().equals(shipment.getSender().getEmail())){
            displaySenderRelatedInformation();
        }
        */
        if(currentUser.getId() == shipment.getDeliverer().getId()){
            displayDelivererRelatedInformation();
        }
        if(currentUser.getId() == shipment.getSender().getId()){
            displaySenderRelatedInformation();
        }
    }

    private void displayDelivererRelatedInformation(){
        shipmentDetailView.showUser(true);

        if (shipment.getStatus().equals(Status.PAIDANDSENDERRATED)
                || shipment.getStatus().equals(Status.DELIVERERRATED)) {
            displayDeliverersRating();
        }
    }

    private void displaySenderRelatedInformation(){
        shipmentDetailView.showUser(false);

        displayQrCode();

        if (shipment.getStatus().equals(Status.DELIVERED)
                || shipment.getStatus().equals(Status.PAIDANDSENDERRATED)
                || shipment.getStatus().equals(Status.DELIVERERRATED)) {
            displayPaymentInformation();
            displaySendersRatingView();
        }
    }

    private void displaySendersRatingView(){
        ratingDivider.setVisibility(View.VISIBLE);
        layoutRating.setVisibility(View.VISIBLE);

        deliverersRating.setIsIndicator(true);
        if(shipment.getSenderRating() != null){
            deliverersRating.setRating(shipment.getSenderRating().getRating());
        }
        if(shipment.getDelivererRating() == null){
            sendersRating.setIsIndicator(false);
            sendersRating.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                    if(fromUser){
                        showSendersRatingConfirmationDialog(rating);
                    }
                }
            });
        }else{
            sendersRating.setRating(shipment.getDelivererRating().getRating());
            sendersRating.setIsIndicator(true);
        }
    }

    private void showSendersRatingConfirmationDialog(final float rating){
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(getResources().getString(R.string.title_rate_dialog))
                .setMessage(
                        getString(R.string.message_rate_dialog0) + "\n" +
                        getString(R.string.message_rate_dialog1)
                                + " " + (int)rating + " "
                                + getString(R.string.message_rate_dialog2))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        attemptRateAsSender(rating);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        sendersRating.setRating(0.0f);
                    }
                }).create();
        dialog.show();
    }

    private void attemptRateAsSender(float rating){
        if(rateAsSenderTask != null){
            return;
        }

        DelivererRating delivererRating = new DelivererRating();
        delivererRating.setIdshipment(shipment.getId());
        delivererRating.setIduser(currentUser.getId());
        delivererRating.setRating(rating);

        shipment.setDelivererRating(delivererRating);

        showProgress(true);
        rateAsSenderTask = new RateAsSenderTask();
        rateAsSenderTask.execute(shipment);
    }

    private void displayDeliverersRating(){
        ratingDivider.setVisibility(View.VISIBLE);
        layoutRating.setVisibility(View.VISIBLE);

        sendersRating.setIsIndicator(true);
        if(shipment.getDelivererRating() != null){
            sendersRating.setRating(shipment.getDelivererRating().getRating());
        }
        if(shipment.getSenderRating() == null){
            deliverersRating.setIsIndicator(false);
            deliverersRating.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                    if(fromUser){
                        showDeliverersRatingConfirmationDialog(rating);
                    }
                }
            });
        }else {
            deliverersRating.setRating(shipment.getSenderRating().getRating());
            deliverersRating.setIsIndicator(true);
        }
    }

    private void showDeliverersRatingConfirmationDialog(final float rating){
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(getResources().getString(R.string.title_rate_dialog))
                .setMessage(
                        getString(R.string.message_rate_dialog1)
                                + " " + (int)rating + " "
                                + getString(R.string.message_rate_dialog2))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        attemptRateAsDeliverer(rating);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deliverersRating.setRating(0.0f);
                    }
                }).create();
        dialog.show();
    }

    private void attemptRateAsDeliverer(float rating){
        if(rateAsDelivererTask != null){
            return;
        }

        SenderRating senderRating = new SenderRating();
        senderRating.setIdshipment(shipment.getId());
        senderRating.setIduser(currentUser.getId());
        senderRating.setRating(rating);

        shipment.setSenderRating(senderRating);

        showProgress(true);
        rateAsDelivererTask = new RateAsDelivererTask();
        rateAsDelivererTask.execute(shipment);
    }

    private void displayQrCode(){
        if(initQrCode(shipment.getQrstring())){
            showQr.setVisibility(View.VISIBLE);
            showQr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(qrView.getVisibility() == View.VISIBLE){
                        qrView.setVisibility(View.GONE);
                        showQr.setText(R.string.text_show_qr);
                    } else {
                        qrView.setVisibility(View.VISIBLE);
                        showQr.setText(R.string.text_hide_qr);
                    }
                }
            });
        }
    }

    private boolean initQrCode(String code){
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(
                    code, BarcodeFormat.QR_CODE,QR_SIZE,QR_SIZE);
            Bitmap image = Bitmap.createBitmap(
                    QR_SIZE, QR_SIZE, Bitmap.Config.ARGB_8888);

            for (int i = 0; i < QR_SIZE; i++) {
                for (int j = 0; j < QR_SIZE; j++) {
                    image.setPixel(i, j, bitMatrix.get(i, j) ? Color.BLACK: Color.WHITE);
                }
            }

            if (qrView != null) {
                qrView.setImageBitmap(image);
                return true;
            }
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void displayPaymentInformation(){
        PaymentInformation paymentInformation = shipment.getDeliverer().getPaymentInformation();
        if(paymentInformation != null){
            paymentDivider.setVisibility(View.VISIBLE);
            layoutPayment.setVisibility(View.VISIBLE);

            if (paymentInformation.getPaymentType()
                    .equals(PaymentType.BANKTRANSFER)) {

                paymentAccount.setText(paymentInformation.getName() + "\n" +
                        paymentInformation.getIdentifier());
                paymentType.setText(StringLookup.getIdFrom(paymentInformation.getPaymentType()));

            } else {
                paymentAccount.setText(paymentInformation.getIdentifier());
                paymentType.setText(paymentInformation.getPaymentType().toString());
            }
        }
    }

    private class RevokeAnnouncementTask extends NetworkTask<ShipmentAnnouncement, Void, Boolean>{

        @Override
        protected Boolean runInBackground(ShipmentAnnouncement[] params) throws NetworkException {
            ShipmentAnnouncement announcement = params[0];
            return networkClient.revokeAnnouncement(announcement);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            showProgress(false);
            revokeAnnouncementTask = null;

            if(networkException != null){
                networkException.handleException((NetworkActivity)getActivity());
                return;
            }

            if(result != null && result){
                showResult(R.string.info_announcement_revoked);
                ShipmentFragment.this.backToPreviousFragment();
            } else {
                showResult(R.string.error_announcement_revoked);
            }
        }
    }

    private class GetDeliveryMapTask extends NetworkTask<String, Void, Drawable>{

        @Override
        protected Drawable runInBackground(String[] strings) throws NetworkException {
            return StaticMapClient.getMap(strings[0], 18, 400);
        }

        @Override
        protected void onPostExecute(Drawable result) {
            deliveryMap.setVisibility(View.VISIBLE);
            deliveryMap.setImageDrawable(result);
            deliveryMap.requestLayout();
            deliveryMap.getLayoutParams().height = deliveryMap.getWidth();

            coordinates.setVisibility(View.GONE);
        }
    }

    private class RateAsDelivererTask extends NetworkTask<Shipment, Void, Shipment> {

        @Override
        protected Shipment runInBackground(Shipment[] params) throws NetworkException {
            Shipment shipment = params[0];
            return networkClient.rateShipmentAsDeliverer(shipment);
        }

        @Override
        protected void onPostExecute(Shipment result) {
            showProgress(false);
            rateAsDelivererTask = null;

            if(networkException != null){
                networkException.handleException((NetworkActivity)getActivity());
                return;
            }

            if(result != null){
                showResult(R.string.info_shipment_rated);
                deliverersRating.setIsIndicator(true);
            } else {
                showResult(R.string.error_shipment_rated);
            }
        }
    }

    private class RateAsSenderTask extends NetworkTask<Shipment, Void, Shipment> {

        @Override
        protected Shipment runInBackground(Shipment[] params) throws NetworkException {
            Shipment shipment = params[0];
            return networkClient.rateShipmentAsSender(shipment);
        }

        @Override
        protected void onPostExecute(Shipment result) {
            showProgress(false);
            rateAsSenderTask = null;

            if(networkException != null){
                networkException.handleException((NetworkActivity)getActivity());
                return;
            }

            if(result != null){
                showResult(R.string.info_shipment_rated);
                sendersRating.setIsIndicator(true);
            } else {
                showResult(R.string.error_shipment_rated);
            }
        }
    }
}
