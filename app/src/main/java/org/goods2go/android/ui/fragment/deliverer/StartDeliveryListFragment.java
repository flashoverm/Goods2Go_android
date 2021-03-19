package org.goods2go.android.ui.fragment.deliverer;

import com.goods2go.models.Shipment;

import org.goods2go.android.R;
import org.goods2go.android.network.NetworkException;
import org.goods2go.android.network.NetworkTask;
import org.goods2go.android.ui.activity.NetworkActivity;
import org.goods2go.android.ui.adapter.ShipmentAdapter;
import org.goods2go.android.ui.fragment.ShipmentListFragment;

import java.util.List;

public class StartDeliveryListFragment extends ShipmentListFragment
        implements ShipmentAdapter.DetailViewListener{

    private GetPendingShipmentsTask getPendingShipmentsTask;

    public static StartDeliveryListFragment newInstance() {
        StartDeliveryListFragment fragment = new StartDeliveryListFragment();
        return fragment;
    }

    @Override
    protected void getList() {
        if (getPendingShipmentsTask != null) {
            return;
        }
        this.showProgress(true);
        getPendingShipmentsTask = new GetPendingShipmentsTask();
        getPendingShipmentsTask.execute();
    }

    @Override
    public void onDetailView(Shipment shipment) {
        StartDeliveryFragment beginDelivery = StartDeliveryFragment.newInstance();
        beginDelivery.setShipment(shipment);
        showLowerLevelFragment(beginDelivery, false);
    }

    private class GetPendingShipmentsTask extends NetworkTask<Void, Void, List<Shipment>> {

        @Override
        protected List<Shipment> runInBackground(Void... voids) throws NetworkException {
            return networkClient.getPendingDelivererShipments();
        }

        @Override
        protected void onPostExecute(List<Shipment> result) {
            showProgress(false);
            getPendingShipmentsTask = null;

            if(networkException != null){
                networkException.handleException((NetworkActivity)getActivity());
                return;
            }

            if (result != null) {
                displayList(result);
            } else {
                showResult(R.string.error_getting_data);
            }
        }
    }
}
