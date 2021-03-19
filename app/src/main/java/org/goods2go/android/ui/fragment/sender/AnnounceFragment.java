package org.goods2go.android.ui.fragment.sender;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.goods2go.models.Address;
import com.goods2go.models.ShipmentAnnouncement;
import com.goods2go.models.util.DateTime;
import com.goods2go.models.util.GeoCoordinates;
import com.google.android.gms.maps.model.LatLng;

import org.goods2go.android.Goods2GoApplication;
import org.goods2go.android.R;
import org.goods2go.android.geo.GeocodingClient;
import org.goods2go.android.network.NetworkException;
import org.goods2go.android.network.NetworkTask;
import org.goods2go.android.ui.activity.NetworkActivity;
import org.goods2go.android.ui.dialog.AddressDialog;
import org.goods2go.android.ui.dialog.AddressHistoryDialog;
import org.goods2go.android.ui.dialog.PickDateDialog;
import org.goods2go.android.ui.fragment.NetworkFragment;
import org.goods2go.android.ui.view.DialogEditText;
import org.goods2go.android.ui.view.SizePickerView;

import java.net.HttpURLConnection;
import java.util.Date;

public class AnnounceFragment extends NetworkFragment
        implements PickDateDialog.DateSetListener, AddressDialog.AddressSetListener,
        AddressHistoryDialog.AddressSelectedListener{

    private static final int Description = 0;
    private static final int Price = 1;
    private static final int Destination = 2;
    private static final int Source = 3;

    private static final int DeliverUntil = 4;
    private static final int PickupFrom = 5;
    private static final int PickupUntil = 6;

    private EditText[] editData;
    private DialogEditText editDeliverUntil;
    private DialogEditText editPickupFrom;
    private DialogEditText editPickupUntil;
    private SizePickerView sizePickerView;

    private Address sourceAddress;
    private Date pickupUntil;
    private Date pickupFrom;
    private Address destinationAddress;
    private ImageView addressHistory;
    private Date deliverUntil;

    private AddAnnouncementTask addAnnouncementTask;

    public static AnnounceFragment newInstance() {
        AnnounceFragment fragment = new AnnounceFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sender_announce, container, false);
        progress = view.findViewById(R.id.progress);
        content = view.findViewById(R.id.content);

        initViewElements(content);
        return view;
    }

    private void initViewElements(View view) {
        editData = new EditText[4];

        editData[Source] = view.findViewById(R.id.source);
        editData[Source].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddressDialog(sourceAddress, Source, R.string.text_source_address);
            }
        });

        editData[Destination] = view.findViewById(R.id.destination);
        editData[Destination].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddressDialog(destinationAddress, Destination, R.string.text_destination_address);
            }
        });

        addressHistory = view.findViewById(R.id.address_history);
        addressHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddressDialog();
            }
        });

        editData[Price] = view.findViewById(R.id.price);
        editData[Description] = view.findViewById(R.id.description);

        editDeliverUntil = view.findViewById(R.id.deliver_until);
        editDeliverUntil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker(DeliverUntil, R.string.text_deliver_until);
            }
        });
        editPickupFrom = view.findViewById(R.id.pickup_from);
        editPickupFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker(PickupFrom, R.string.text_pickup_from);
            }
        });

        editPickupUntil = view.findViewById(R.id.pickup_until);
        editPickupUntil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker(PickupUntil, R.string.text_pickup_until);
            }
        });

        sizePickerView = view.findViewById(R.id.sizepicker);
        sizePickerView.setSizes(Goods2GoApplication.getShipmentSizes(getActivity()));

        Button publish = view.findViewById(R.id.button_publish);
        publish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptPublish();
            }
        });

        Address address = Goods2GoApplication.getCurrentUser(getActivity()).getDefaultaddress();
        if(address != null){
            onAddressSet(address, Source);
        }
    }

    private void showAddressDialog(){
        AddressHistoryDialog dialog = AddressHistoryDialog.newInstance();
        dialog.setTargetFragment(this, Destination);
        dialog.show(getFragmentManager(), AddressHistoryDialog.TAG);
    }

    private void showAddressDialog(Address address, int fieldNumber, int titleResId){
        AddressDialog dialog = AddressDialog.newInstance(address, fieldNumber, titleResId);
        dialog.setTargetFragment(this, fieldNumber);
        dialog.show(getFragmentManager(), AddressDialog.TAG);
    }

    private void showDatePicker(int fieldNumber, int titleResId){
        PickDateDialog dialog = PickDateDialog.newInstance(fieldNumber, titleResId);
        dialog.setTargetFragment(this, fieldNumber);
        dialog.show(getFragmentManager(), PickDateDialog.TAG);
    }

    @Override
    public void onAddressSelected(Address address){
        onAddressSet(address, Destination);
    }

    @Override
    public void onAddressSet(Address address, int fieldNumber) {
        switch (fieldNumber) {
            case Source:
                sourceAddress = address;
                editData[Source].setText(address.getAddressAsString());
                break;
            case Destination:
                destinationAddress = address;
                editData[Destination].setText(address.getAddressAsString());
                break;
        }
    }

    @Override
    public void onDateSet(Date date, int fieldNumber) {
        switch (fieldNumber){
            case PickupFrom:
                pickupFrom = date;
                editPickupFrom.setText(DateTime.DATE_FORMAT.format(pickupFrom));
                break;
            case PickupUntil:
                pickupUntil = date;
                editPickupUntil.setText(DateTime.DATE_FORMAT.format(pickupUntil));
                break;
            case DeliverUntil:
                deliverUntil = date;
                editDeliverUntil.setText(DateTime.DATE_FORMAT.format(deliverUntil));
                break;
        }
    }

    private void attemptPublish() {
        if (addAnnouncementTask != null) {
            return;
        }

        boolean cancel = false;
        View focusView = null;

        editPickupUntil.setError(null);
        editDeliverUntil.setError(null);

        String[] data = new String[editData.length];
        float price = 0.0f;
        for(int i=editData.length-1; i>=0; i--){
            editData[i].setError(null);
            data[i] = editData[i].getText().toString();
            if(TextUtils.isEmpty(data[i])){
                editData[i].setError(getString(R.string.error_field_required));
                cancel = true;
                focusView = editData[i];
            } else if (i == Price){
                price = Float.parseFloat(data[i]);
                if(price < 0){
                    editData[i].setError(getString(R.string.error_price_negative));
                    cancel = true;
                    focusView = editData[i];
                }
            }
        }
        if(!cancel){
            if(pickupFrom.compareTo(pickupUntil) > 0){
                editPickupUntil.setError(getString(R.string.error_pickupfrom_before_until));
                focusView = editPickupUntil;
                cancel = true;
            }
            if(pickupUntil.compareTo(deliverUntil) > 0){
                editDeliverUntil.setError(getString(R.string.error_deliver_before_pickup));
                focusView = editDeliverUntil;
                cancel = true;
            }
        }

        if(cancel) {
            focusView.requestFocus();
        } else {
            ShipmentAnnouncement shipmentAnnouncement = new ShipmentAnnouncement(
                    data[Description],
                    null,    //is set on backend
                    sourceAddress,
                    destinationAddress,
                    sizePickerView.getShipmentSize(),
                    price,
                    pickupFrom,
                    pickupUntil,
                    deliverUntil
            );

            showProgress(true);
            addAnnouncementTask = new AddAnnouncementTask();
            addAnnouncementTask.execute(shipmentAnnouncement);
        }
    }

    private class AddAnnouncementTask
            extends NetworkTask<ShipmentAnnouncement, Void, ShipmentAnnouncement> {

        @Override
        protected ShipmentAnnouncement runInBackground(ShipmentAnnouncement... params)
                throws NetworkException {
            ShipmentAnnouncement shipmentAnnouncement = params[0];

            //determining coordinates of source
            LatLng source = GeocodingClient
                    .getCoordinatesFromStreet(shipmentAnnouncement.getSource());
            //determining coordinates of destination
            LatLng destination = GeocodingClient
                    .getCoordinatesFromStreet(shipmentAnnouncement.getDestination());

            if(source == null){
                throw new NetworkException("Could not get coordinates to addresses", -1);
            }
            if(destination == null){
                throw new NetworkException("Could not get coordinates to addresses", -2);
            }

            shipmentAnnouncement.setAproxsourcecoordinates(
                    GeoCoordinates.toString(source.latitude, source.longitude));
            shipmentAnnouncement.setAproxdestinationcoordinates(
                    GeoCoordinates.toString(destination.latitude, destination.longitude));
            return networkClient.announceShipment(shipmentAnnouncement);
        }

        @Override
        protected void onPostExecute(ShipmentAnnouncement result) {
            showProgress(false);
            addAnnouncementTask = null;

            if(networkException != null){
                if(networkException.httpError == HttpURLConnection.HTTP_NOT_FOUND){
                    showResult(R.string.error_size_not_existing);
                    return;
                } else if(networkException.httpError == -1){
                    showResult(R.string.error_source_address);
                    return;
                } else if(networkException.httpError == -2){
                    showResult(R.string.error_destination_address);
                    return;
                } else {
                    networkException.handleException((NetworkActivity)getActivity());
                    return;
                }
            }

            if(result != null){
                for(EditText edit : editData){
                    edit.setText("");
                }
                editDeliverUntil.setText("");
                editPickupFrom.setText("");
                editPickupUntil.setText("");
                showResult(R.string.info_announcement_published);
            } else {
                showResult(R.string.error_announcement_publish);
            }
        }
    }
}
