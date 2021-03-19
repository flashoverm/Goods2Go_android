package org.goods2go.android.ui.fragment.deliverer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.goods2go.models.Address;
import com.goods2go.models.ShipmentSubscription;
import com.goods2go.models.util.DateTime;
import com.goods2go.models.util.GeoCoordinates;
import com.google.android.gms.maps.model.LatLng;

import org.goods2go.android.Goods2GoApplication;
import org.goods2go.android.R;
import org.goods2go.android.geo.GeoClient;
import org.goods2go.android.geo.GeocodingClient;
import org.goods2go.android.network.NetworkClient;
import org.goods2go.android.network.NetworkException;
import org.goods2go.android.network.NetworkTask;
import org.goods2go.android.ui.activity.NetworkActivity;
import org.goods2go.android.ui.dialog.AbstractDialogFragment;
import org.goods2go.android.ui.dialog.PickDateDialog;
import org.goods2go.android.ui.view.DialogEditText;
import org.goods2go.android.ui.view.SizePickerView;

import java.net.HttpURLConnection;
import java.util.Date;

public class SubscribeShipmentsFragment extends AbstractDialogFragment
        implements PickDateDialog.DateSetListener{
//Has to extend dialog fragment because the filter dialog extends this fragment

    private NetworkClient networkClient;

    private static final int DeliverUntil = 4;
    private static final int PickupFrom = 5;

    protected static final int RADIUS_MAX = 100;
    protected static final int RADIUS_INIT = 50;

    protected View progress;
    protected View content;

    protected DialogEditText editPickupFrom;
    protected DialogEditText editDeliverUntil;
    protected SizePickerView sizePickerView;
    protected EditText editSourceCity;
    protected EditText editSourcePostcode;
    protected EditText editDestinationCity;
    protected EditText editDestinationPostcode;
    protected SeekBar editRadius;
    protected TextView radiusView;
    protected ImageView buttonLocation;

    protected LatLng location;

    protected ShipmentSubscription criteria;

    protected GetCoordinatesTask getCoordinatesTask;
    protected GetLocationAddressTask getLocationAddressTask;
    private SubscribeShipmentsTask subscribeShipmentsTask;

    public static SubscribeShipmentsFragment newInstance() {
        SubscribeShipmentsFragment fragment = new SubscribeShipmentsFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        networkClient = Goods2GoApplication.getNetworkClient(getActivity());

        initCriteria();

        View view = initView();
        view.findViewById(R.id.button_layout).setVisibility(View.VISIBLE);
        view.findViewById(R.id.button_subscribe).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupCriteria();
            }
        });
        return view;
    }

    protected void initCriteria(){
        criteria = new ShipmentSubscription();
        criteria.setRadius(RADIUS_INIT);
        criteria.setDestinationradius(RADIUS_INIT);
    }

    protected View initView(){
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_deliverer_subscribe, null);
        progress = view.findViewById(R.id.progress);
        content = view.findViewById(R.id.content);

        editPickupFrom = view.findViewById(R.id.pickup_from);
        editPickupFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker(PickupFrom, R.string.text_pickup_from);
            }
        });
        editDeliverUntil = view.findViewById(R.id.deliver_until);
        editDeliverUntil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker(DeliverUntil, R.string.text_deliver_until);
            }
        });
        editSourceCity = view.findViewById(R.id.source_city);
        editSourcePostcode = view.findViewById(R.id.source_postcode);
        editDestinationCity = view.findViewById(R.id.destination_city);
        editDestinationPostcode = view.findViewById(R.id.destination_postcode);
        sizePickerView = view.findViewById(R.id.sizepicker);
        sizePickerView.setSizes(Goods2GoApplication.getShipmentSizes(getActivity()));

        radiusView = view.findViewById(R.id.radius_value);
        radiusView.setText(RADIUS_INIT + " " + getString(R.string.text_radius_unit));
        editRadius = view.findViewById(R.id.radius);
        editRadius.setMax(RADIUS_MAX);
        editRadius.setProgress(RADIUS_INIT);
        editRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                radiusView.setText(i + " " + getString(R.string.text_radius_unit));
                criteria.setRadius(i);
                criteria.setDestinationradius(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        buttonLocation = view.findViewById(R.id.location);
        buttonLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocation();
            }
        });
        return view;
    }

    protected void setupCriteria(){
        if(getCoordinatesTask != null){
            return;
        }
        boolean cancel = false;
        View focusView = null;

        String pickupFromString = editPickupFrom.getText();
        String deliverUntilString = editDeliverUntil.getText();
        String sourceCity = editSourceCity.getText().toString();
        String destinationCity = editDestinationCity.getText().toString();
        String sourcePostcode = editSourcePostcode.getText().toString();
        String destinationPostcode = editDestinationPostcode.getText().toString();

        editPickupFrom.setError(null);
        editDeliverUntil.setError(null);
        editSourceCity.setError(null);
        editSourcePostcode.setError(null);
        editDestinationCity.setError(null);
        editDestinationPostcode.setError(null);

        if(TextUtils.isEmpty(destinationCity) && TextUtils.isEmpty(destinationPostcode)
            && TextUtils.isEmpty(sourceCity) && TextUtils.isEmpty(sourcePostcode)){
            editSourceCity.setError(getString(R.string.error_min_source_or_dest));
            focusView = editSourcePostcode;
            cancel = true;

        } else {
            if((TextUtils.isEmpty(destinationCity) && !TextUtils.isEmpty(destinationPostcode))){
                editDestinationCity.setError(getString(R.string.error_field_required));
                focusView = editDestinationCity;
                cancel = true;
            } else if(!TextUtils.isEmpty(destinationCity) && TextUtils.isEmpty(destinationPostcode)){
                editDestinationPostcode.setError(getString(R.string.error_field_required));
                focusView = editDestinationPostcode;
                cancel = true;
            }
            if(TextUtils.isEmpty(sourceCity) && !TextUtils.isEmpty(sourcePostcode)){
                editSourceCity.setError(getString(R.string.error_field_required));
                focusView = editSourceCity;
                cancel = true;
            } else if(!TextUtils.isEmpty(sourceCity) && TextUtils.isEmpty(sourcePostcode)){
                editSourcePostcode.setError(getString(R.string.error_field_required));
                focusView = editSourcePostcode;
                cancel = true;
            }
        }

        if(TextUtils.isEmpty(deliverUntilString)){
            editDeliverUntil.setError(getString(R.string.error_field_required));
            focusView = editDeliverUntil;
            cancel = true;
        }
        if(TextUtils.isEmpty(pickupFromString)){
            editPickupFrom.setError(getString(R.string.error_field_required));
            focusView = editPickupFrom;
            cancel = true;
        }

        if(!cancel){
            if(criteria.getPickupfrom().compareTo(criteria.getDeliveruntil()) > 0){
                editDeliverUntil.setError(getString(R.string.error_deliver_before_pickup));
                focusView = editDeliverUntil;
                cancel = true;
            }
        }

        if(cancel) {
            focusView.requestFocus();
        } else {
            Address[] addresses;
            Address destination = null;
            Address source = null;
            if(!TextUtils.isEmpty(destinationCity) && !TextUtils.isEmpty(destinationPostcode)){
                destination = new Address(destinationPostcode, destinationCity);
            }
            criteria.setDestination(destination);
            criteria.setDestinationcoordinates(null);

            if(location != null){
                //Use location coordinates
                criteria.setSourcecoordinates(
                        GeoCoordinates.toString(location.latitude, location.longitude));
                addresses = new Address[]{destination};
            } else {
                if(!TextUtils.isEmpty(sourcePostcode) && !TextUtils.isEmpty(sourceCity)){
                    source = new Address(sourcePostcode, sourceCity);
                }
                addresses = new Address[]{destination, source};
                criteria.setSource(source);
                criteria.setSourcecoordinates(null);
            }

            criteria.setMaxsize(sizePickerView.getShipmentSize());
            showProgress(true);
            getCoordinatesTask = new GetCoordinatesTask();
            getCoordinatesTask.execute(addresses);
        }
    }

    protected void applyCriteria(){
        if(subscribeShipmentsTask != null){
            return;
        }
        subscribeShipmentsTask = new SubscribeShipmentsTask();
        subscribeShipmentsTask.execute();
    }

    protected void showDatePicker(int fieldNumber, int titleResId){
        PickDateDialog dialog = PickDateDialog.newInstance(fieldNumber, titleResId);
        dialog.setTargetFragment(this, fieldNumber);
        dialog.show(getFragmentManager(), PickDateDialog.TAG);
    }

    protected void getLocation(){
        showProgress(true);
        org.goods2go.android.geo.GeoClient.getLocation(getActivity(), new org.goods2go.android.geo.GeoClient.LocationListener() {
            @Override
            public void onLocationUpdated(LatLng latLng) {
                location = latLng;
                getLocationAddressTask = new GetLocationAddressTask();
                getLocationAddressTask.execute(latLng);
            }

            @Override
            public void onError(int errorCode) {
                showProgress(false);
                showResult(GeoClient.getErrorMessage(errorCode));
            }
        });
    }

    @Override
    public void onDateSet(Date date, int fieldNumber) {
        switch (fieldNumber){
            case PickupFrom:
                criteria.setPickupfrom(date);
                editPickupFrom.setText(DateTime.DATE_FORMAT.format(date));
                break;
            case DeliverUntil:
                criteria.setDeliveruntil(date);
                editDeliverUntil.setText(DateTime.DATE_FORMAT.format(date));
                break;
        }
    }

    protected void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        content.setVisibility(show ? View.GONE : View.VISIBLE);
        content.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                content.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        progress.setVisibility(show ? View.VISIBLE : View.GONE);
        progress.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                progress.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    protected void showResult(int resId){
        ((NetworkActivity)getActivity()).showResult(resId);
    }

    @Override
    protected void onAttachCompatible(Context context) {

    }

    private class GetCoordinatesTask extends NetworkTask<Address, Void, Integer> {

        @Override
        protected Integer runInBackground(Address... addresses) throws NetworkException {
            if(addresses[0] != null){
                LatLng destination = GeocodingClient.getCoordinatesFromPostcode(addresses[0]);
                if(destination != null){
                    criteria.setDestinationcoordinates(
                            GeoCoordinates.toString(destination.latitude, destination.longitude));
                } else {
                    return 1;
                }
            } else if(addresses.length != 2){
                return 2;
            }
            if(addresses.length == 2){
                if(addresses[1] != null){
                    LatLng source = GeocodingClient.getCoordinatesFromPostcode(addresses[1]);
                    if(source != null){
                        criteria.setSourcecoordinates(
                                GeoCoordinates.toString(source.latitude, source.longitude));
                    } else {
                        return 3;
                    }
                } else {
                    return 4;
                }
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer error) {
            getCoordinatesTask = null;

            if(networkException != null){
                showResult(R.string.error_network);
            }

            if (error == 0) {
                applyCriteria();
            } else if(error == 1){
                //To not found
                showResult(R.string.error_destination_address);

            } else if(error == 3){
                //From not found
                showResult(R.string.error_source_address);
            } else if(error == 2){
                applyCriteria();
                //To not set
            } else if(error == 4){
                applyCriteria();
                //From not set
            }
        }
    }

    private class GetLocationAddressTask extends NetworkTask<LatLng, Void, Address> {

        @Override
        protected Address runInBackground(LatLng[] params) throws NetworkException {
            LatLng location = params[0];
            return GeocodingClient.getPostcodeFromCoordinates(location);
        }

        @Override
        protected void onPostExecute(Address result) {
            getLocationAddressTask = null;
            showProgress(false);

            if(networkException != null){
                showResult(R.string.error_network);
            }

            if(result != null){
                editSourceCity.setText(result.getCity());
                editSourcePostcode.setText(result.getPostcode());
                criteria.setSource(result);
            } else {
                showResult(R.string.error_location_address);
            }
        }
    }

    private class SubscribeShipmentsTask
            extends NetworkTask<Void, Void, ShipmentSubscription> {

        @Override
        protected ShipmentSubscription runInBackground(Void... aVoid) throws NetworkException {
            return networkClient.subscribeShipments(criteria);
        }

        @Override
        protected void onPostExecute(ShipmentSubscription result) {
            subscribeShipmentsTask = null;
            showProgress(false);

            if(networkException != null){
                if(networkException.httpError == HttpURLConnection.HTTP_NOT_FOUND){
                    showResult(R.string.error_size_not_existing);
                    return;
                }
                networkException.handleException((NetworkActivity)getActivity());
                return;
            }

            if(result != null){
                showResult(R.string.info_subscription_sent);
            } else {
                showResult(R.string.error_subscription_sent);
            }
        }
    }
}
