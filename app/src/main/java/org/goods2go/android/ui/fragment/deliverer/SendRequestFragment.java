package org.goods2go.android.ui.fragment.deliverer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.goods2go.models.ShipmentAnnouncement;
import com.goods2go.models.ShipmentRequest;
import com.goods2go.models.util.DateTime;

import org.goods2go.android.R;
import org.goods2go.android.network.NetworkException;
import org.goods2go.android.network.NetworkTask;
import org.goods2go.android.ui.activity.NetworkActivity;
import org.goods2go.android.ui.dialog.PickDateDialog;
import org.goods2go.android.ui.dialog.PickTimeDialog;
import org.goods2go.android.ui.fragment.LowerLevelFragment;
import org.goods2go.android.ui.view.AddressView;
import org.goods2go.android.ui.view.DialogEditText;
import org.goods2go.android.ui.view.ShipmentDetailView;

import java.util.Date;

public class SendRequestFragment extends LowerLevelFragment
        implements PickTimeDialog.TimeSetListener, PickDateDialog.DateSetListener {

    private static final int KEY_PICKUP = 0;
    private static final int KEY_DELIVER = 1;

    private Button sendRequest;

    private DialogEditText editPickupDate;
    private EditText editPickupTime;
    private DialogEditText editDeliverDate;
    private EditText editDeliverTime;

    private long pickupDate;
    private long pickupTime = 0;
    private long deliverDate;
    private long deliverTime = 0;

    private ShipmentAnnouncement announcement;
    private SendRequestTask sendRequestTask;

    public static SendRequestFragment newInstance() {
        SendRequestFragment fragment = new SendRequestFragment();
        return fragment;
    }

    public void setShipmentAnnouncement(ShipmentAnnouncement announcement){
        this.announcement = announcement;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_deliverer_request, container, false);
        progress = view.findViewById(R.id.progress);
        content = view.findViewById(R.id.content);

        editPickupDate = view.findViewById(R.id.pickup_date);
        editPickupDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker(KEY_PICKUP, R.string.text_pickup_datetime);
            }
        });
        editPickupTime = view.findViewById(R.id.pickup_time);
        editPickupTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePicker(KEY_PICKUP, R.string.text_pickup_datetime);
            }
        });
        editDeliverDate = view.findViewById(R.id.deliver_date);
        editDeliverDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker(KEY_DELIVER, R.string.text_deliver_datetime);
            }
        });
        editDeliverTime = view.findViewById(R.id.deliver_time);
        editDeliverTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePicker(KEY_DELIVER, R.string.text_deliver_datetime);
            }
        });

        sendRequest = view.findViewById(R.id.button_send_request);
        sendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRequest();
            }
        });

        setTitle(R.string.text_send_request);

        initAnnouncementView(view);
        return view;
    }

    private void initAnnouncementView(View view){
        ShipmentDetailView shipmentDetailView = view.findViewById(R.id.shipment_detail);
        shipmentDetailView.setAnnouncement(announcement);
        shipmentDetailView.showUser(true);

        AddressView source = view.findViewById(R.id.source);
        TextView pickupFrom = view.findViewById(R.id.earliest_pickup_date);
        TextView pickupUntil = view.findViewById(R.id.latest_pickup_date);

        source.setAddress(AddressView.censorAddress(announcement.getSource()));
        pickupFrom.setText(DateTime.DATE_FORMAT.format(announcement.getEarliestpickupdate()));
        pickupUntil.setText(DateTime.DATE_FORMAT.format(announcement.getLatestpickupdate()));

        AddressView destination = view.findViewById(R.id.destination);
        TextView deliverUntil = view.findViewById(R.id.latest_delivery_date);

        destination.setAddress(AddressView.censorAddress(announcement.getDestination()));
        deliverUntil.setText(DateTime.DATE_FORMAT.format(announcement.getLatestdeliverydate()));
    }

    private void showDatePicker(int fieldNumber, int titleResId){
        PickDateDialog dialog = PickDateDialog.newInstance(fieldNumber, titleResId);
        dialog.setTargetFragment(this, fieldNumber);
        dialog.show(getFragmentManager(), PickDateDialog.TAG);
    }

    private void showTimePicker(int fieldNumber, int titleResId){
        PickTimeDialog dialog = PickTimeDialog.newInstance(fieldNumber, titleResId);
        dialog.setTargetFragment(this, fieldNumber);
        dialog.show(getFragmentManager(), PickTimeDialog.TAG);
    }

    @Override
    public void onTimeSet(int hours, int minutes, int fieldNumber) {
        String text = "";
        if(hours >= 10){
            text += hours;
        } else {
            text += "0" + hours;
        }
        text += ":";
        if(minutes >= 10){
            text += minutes;
        } else {
            text += "0" + minutes;
        }
        text  += " " + getString(R.string.text_o_clock);

        long millis = (hours * 60 + minutes) * 60 * 1000;

        switch (fieldNumber){
            case KEY_PICKUP:
                pickupTime = millis;
                editPickupTime.setText(text);
                break;
            case KEY_DELIVER:
                deliverTime = millis;
                editDeliverTime.setText(text);
                break;
        }
    }

    @Override
    public void onDateSet(Date date, int fieldNumber) {
        switch (fieldNumber){
            case KEY_PICKUP:
                pickupDate = date.getTime();
                editPickupDate.setText(DateTime.DATE_FORMAT.format(date));
                break;
            case KEY_DELIVER:
                deliverDate = date.getTime();
                editDeliverDate.setText(DateTime.DATE_FORMAT.format(date));
                break;
        }
    }

    private void attemptRequest(){
        if(sendRequestTask != null){
            return;
        }

        editDeliverDate.setError(null);
        editDeliverTime.setError(null);
        editPickupDate.setError(null);
        editPickupTime.setError(null);

        boolean cancel = false;
        View focusView = null;

        /*
        if(deliverTime == 0){
            editDeliverTime.setError(getString(R.string.error_field_required));
            cancel = true;
            focusView = editDeliverTime;
        }
        */
        if(deliverDate == 0){
            editDeliverDate.setError(getString(R.string.error_field_required));
            cancel = true;
            focusView = editDeliverDate;
        }
        /*
        if(pickupTime == 0){
            editPickupTime.setError(getString(R.string.error_field_required));
            cancel = true;
            focusView = editPickupTime;
        }
        */
        if(pickupDate == 0){
            editPickupDate.setError(getString(R.string.error_field_required));
            cancel = true;
            focusView = editPickupDate;
        }

        Date pickupDateTime = null;
        Date deliverDateTime = null;

        if(!cancel){
            pickupDateTime = new Date(pickupDate + pickupTime);
            deliverDateTime = new Date(deliverDate + deliverTime);

            if(pickupDateTime.compareTo(deliverDateTime) > 0){
                //TODO Fehlermeldung
                editDeliverDate.setError("Datumsangaben ungültig");
                focusView = editDeliverDate;
                cancel = true;
            }

            //TODO check if dates are in range of the announcement
        }

        if(cancel){
            focusView.requestFocus();
        } else {
            showProgress(true);
            sendRequestTask = new SendRequestTask();
            sendRequestTask.execute(new ShipmentRequest(
                    announcement,
                    null,       //is set on backend
                    pickupDateTime,
                    deliverDateTime));
        }
    }


    private class SendRequestTask extends NetworkTask<ShipmentRequest, Void, ShipmentRequest> {

        @Override
        protected ShipmentRequest runInBackground(ShipmentRequest... shipmentRequests)
                throws NetworkException {
            ShipmentRequest request = shipmentRequests[0];
            return networkClient.sendShipmentRequest(request);
        }

        @Override
        protected void onPostExecute(ShipmentRequest result) {
            showProgress(false);
            sendRequestTask = null;

            if(networkException != null){
                networkException.handleException((NetworkActivity)getActivity());
                return;
            }

            if(result != null){
                showResult(R.string.info_request_sent);
                backToPreviousFragment();
            } else {
                showResult(R.string.error_request_sent);
            }
        }
    }
}
