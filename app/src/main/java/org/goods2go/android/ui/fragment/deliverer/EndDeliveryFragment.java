package org.goods2go.android.ui.fragment.deliverer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.goods2go.models.Shipment;
import com.goods2go.models.util.DateTime;
import com.goods2go.models.util.GeoCoordinates;
import com.google.android.gms.maps.model.LatLng;

import org.goods2go.android.R;
import org.goods2go.android.geo.GeoClient;
import org.goods2go.android.network.NetworkException;
import org.goods2go.android.network.NetworkTask;
import org.goods2go.android.ui.activity.NetworkActivity;
import org.goods2go.android.ui.fragment.LowerLevelFragment;
import org.goods2go.android.ui.view.AddressView;
import org.goods2go.android.ui.view.ShipmentDetailView;

public class EndDeliveryFragment extends LowerLevelFragment {

    public static EndDeliveryFragment newInstance() {
        EndDeliveryFragment fragment = new EndDeliveryFragment();
        return fragment;
    }

    private Shipment shipment;

    private Button buttonDeliver;

    private DeliverDeliveryTask deliverDeliveryTask;

    public void setShipment(Shipment shipment) {
        this.shipment = shipment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_deliverer_end, container, false);
        progress = view.findViewById(R.id.progress);
        content = view.findViewById(R.id.content);

        buttonDeliver = view.findViewById(R.id.button_deliver);
        buttonDeliver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showProgress(true);
                GeoClient.getLocation(getActivity(), new GeoClient.LocationListener() {
                    @Override
                    public void onLocationUpdated(LatLng latLng) {
                        if (deliverDeliveryTask != null) {
                            return;
                        }
                        deliverDeliveryTask = new DeliverDeliveryTask();
                        deliverDeliveryTask.execute(latLng);
                    }

                    @Override
                    public void onError(int errorCode) {
                        showProgress(false);
                        showResult(GeoClient.getErrorMessage(errorCode));
                    }
                });

            }
        });

        setTitle(R.string.text_end_shipment);
        displayEndShipment(view);
        return view;
    }

    private void displayEndShipment(View view){
        ShipmentDetailView shipmentDetailView = view.findViewById(R.id.shipment_detail);
        shipmentDetailView.setShipment(shipment);
        shipmentDetailView.showState();

        AddressView destination = view.findViewById(R.id.destination);
        TextView negPickupUntil = view.findViewById(R.id.delivery_datetime);

        destination.setAddress(shipment.getDestination());
        negPickupUntil.setText(DateTime.DATETIME_FORMAT.format(shipment.getNegdeliverydatetime()));
    }

    private class DeliverDeliveryTask extends NetworkTask<LatLng, Void, Boolean> {

        @Override
        protected Boolean runInBackground(LatLng... params) throws NetworkException {
            LatLng coordinates = params[0];
            shipment.setDeliverycoordinates(
                    GeoCoordinates.toString(coordinates.latitude, coordinates.longitude));
            return networkClient.deliverShipment(shipment);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            showProgress(false);
            deliverDeliveryTask = null;

            if(networkException != null){
                networkException.handleException((NetworkActivity)getActivity());
                return;
            }

            if (result) {
                showResult(R.string.info_delivery_ended);
                backToPreviousFragment();
            } else {
                showResult(R.string.error_delivery_ended);
            }
        }
    }
}
