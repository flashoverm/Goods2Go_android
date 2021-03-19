package org.goods2go.android.ui.fragment.deliverer;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.goods2go.models.Shipment;
import com.goods2go.models.util.DateTime;

import org.goods2go.android.R;
import org.goods2go.android.network.NetworkException;
import org.goods2go.android.network.NetworkTask;
import org.goods2go.android.ui.activity.NetworkActivity;
import org.goods2go.android.ui.fragment.LowerLevelFragment;
import org.goods2go.android.ui.view.AddressView;
import org.goods2go.android.ui.view.ShipmentDetailView;


public class StartDeliveryFragment extends LowerLevelFragment
        implements ScanFragment.ScanResultListener{

    public static StartDeliveryFragment newInstance() {
        StartDeliveryFragment fragment = new StartDeliveryFragment();
        return fragment;
    }

    private Shipment shipment;

    private Button buttonScan;

    private PickupDeliveryTask pickupDeliveryTask;

    public void setShipment(Shipment shipment){
        this.shipment = shipment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_deliverer_start, container, false);
        progress = view.findViewById(R.id.progress);
        content = view.findViewById(R.id.content);

        buttonScan = view.findViewById(R.id.button_scan);
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment scanFragment = ScanFragment.newInstance(StartDeliveryFragment.this);
                showLowerLevelFragment(scanFragment, true);
            }
        });

        setTitle(R.string.text_start_shipment);
        displayStartShipment(view);
        return view;
    }

    private void displayStartShipment(View view){
        ShipmentDetailView shipmentDetailView = view.findViewById(R.id.shipment_detail);
        shipmentDetailView.setShipment(shipment);
        shipmentDetailView.showState();

        AddressView source = view.findViewById(R.id.source);
        TextView negPickupDateTime = view.findViewById(R.id.pickup_datetime);

        source.setAddress(shipment.getSource());
        negPickupDateTime.setText(DateTime.DATETIME_FORMAT.format(shipment.getNegpickupdatetime()));
    }

    @Override
    public void onScanResult(String qrCode) {
        this.shipment.setQrstring(qrCode);

        if(pickupDeliveryTask != null){
            return;
        }
        showProgress(true);
        pickupDeliveryTask = new PickupDeliveryTask();
        pickupDeliveryTask.execute();
    }

    private class PickupDeliveryTask extends NetworkTask<Void, Void, Boolean> {

        @Override
        protected Boolean runInBackground(Void... aVoid) throws NetworkException {
            return networkClient.pickupShipment(shipment);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            showProgress(false);
            pickupDeliveryTask = null;

            if(networkException != null){
                networkException.handleException((NetworkActivity)getActivity());
                return;
            }

            if(result){
                showResult(R.string.info_delivery_started);
                backToPreviousFragment();
            } else {
                showResult(R.string.error_delivery_started);
            }
        }
    }
}
