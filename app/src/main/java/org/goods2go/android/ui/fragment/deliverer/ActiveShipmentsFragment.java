package org.goods2go.android.ui.fragment.deliverer;

import com.goods2go.models.Shipment;

import org.goods2go.android.R;
import org.goods2go.android.network.NetworkException;
import org.goods2go.android.network.NetworkTask;
import org.goods2go.android.ui.activity.NetworkActivity;
import org.goods2go.android.ui.fragment.ShipmentListFragment;

import java.util.List;

public class ActiveShipmentsFragment extends ShipmentListFragment {

    private GetActiveShipmentsTask getActiveShipmentsTask;

    public static ActiveShipmentsFragment newInstance() {
        ActiveShipmentsFragment fragment = new ActiveShipmentsFragment();
        return fragment;
    }

    @Override
    protected void getList() {
        if (getActiveShipmentsTask != null) {
            return;
        }
        this.showProgress(true);
        getActiveShipmentsTask = new GetActiveShipmentsTask();
        getActiveShipmentsTask.execute();
    }

    private class GetActiveShipmentsTask extends NetworkTask<Void, Void, List<Shipment>> {

        @Override
        protected List<Shipment> runInBackground(Void... voids) throws NetworkException {
            return networkClient.getActiveDelivererShipments();
        }

        @Override
        protected void onPostExecute(List<Shipment> result) {
            showProgress(false);
            getActiveShipmentsTask = null;

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
