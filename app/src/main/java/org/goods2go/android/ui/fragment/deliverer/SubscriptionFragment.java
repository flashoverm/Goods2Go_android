package org.goods2go.android.ui.fragment.deliverer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.goods2go.models.ShipmentSubscription;
import com.goods2go.models.util.DateTime;

import org.goods2go.android.R;
import org.goods2go.android.network.NetworkException;
import org.goods2go.android.network.NetworkTask;
import org.goods2go.android.ui.activity.NetworkActivity;
import org.goods2go.android.ui.fragment.LowerLevelFragment;
import org.goods2go.android.ui.view.AddressView;
import org.goods2go.android.ui.view.ShipmentSizeView;

public class SubscriptionFragment extends LowerLevelFragment {

    public static SubscriptionFragment newInstance() {
        SubscriptionFragment fragment = new SubscriptionFragment();
        return fragment;
    }

    private AddressView from;
    private AddressView to;
    private TextView departure;
    private TextView arrival;
    private TextView radius;
    private ShipmentSizeView shipmentSizeView;

    private ShipmentSubscription shipmentSubscription;

    private RevokeSubscriptionTask revokeSubscriptionTask;

    public void setShipmentSubscription(ShipmentSubscription shipmentSubscription){
        this.shipmentSubscription = shipmentSubscription;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_deliverer_subscription, container, false);
        progress = view.findViewById(R.id.progress);
        content = view.findViewById(R.id.content);

        from = view.findViewById(R.id.from);
        to = view.findViewById(R.id.to);
        departure = view.findViewById(R.id.departure);
        arrival = view.findViewById(R.id.arrival);
        radius = view.findViewById(R.id.radius);
        shipmentSizeView = view.findViewById(R.id.max_size);

        from.setAddress(shipmentSubscription.getSource());
        to.setAddress(shipmentSubscription.getDestination());
        departure.setText(DateTime.DATE_FORMAT.format(shipmentSubscription.getPickupfrom()));
        arrival.setText(DateTime.DATE_FORMAT.format(shipmentSubscription.getDeliveruntil()));
        radius.setText(shipmentSubscription.getRadius() +" "+ getString(R.string.text_radius_unit));
        shipmentSizeView.setShipmentSize(shipmentSubscription.getMaxsize());

        Button revokeSubscription = view.findViewById(R.id.button_revoke);
        revokeSubscription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(revokeSubscriptionTask != null){
                    return;
                }
                showProgress(true);
                revokeSubscriptionTask = new RevokeSubscriptionTask();
                revokeSubscriptionTask.execute();
            }
        });

        setTitle(R.string.text_subscription_detail);
        return view;
    }

    private class RevokeSubscriptionTask extends NetworkTask<Void, Void, Boolean> {

        @Override
        protected Boolean runInBackground(Void... aVoid) throws NetworkException {
            return networkClient.revokeShipmentSubscription(shipmentSubscription);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            showProgress(false);
            revokeSubscriptionTask = null;

            if(networkException != null){
                networkException.handleException((NetworkActivity)getActivity());
                return;
            }

            if(result){
                showResult(R.string.info_subscription_revoked);
                backToPreviousFragment();
            } else {
                showResult(R.string.error_subscription_revoked);
            }
        }
    }
}
