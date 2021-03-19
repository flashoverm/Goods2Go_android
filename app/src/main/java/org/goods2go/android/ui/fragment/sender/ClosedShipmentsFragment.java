package org.goods2go.android.ui.fragment.sender;

import com.goods2go.models.Shipment;

import org.goods2go.android.R;
import org.goods2go.android.network.NetworkException;
import org.goods2go.android.network.NetworkTask;
import org.goods2go.android.ui.activity.NetworkActivity;
import org.goods2go.android.ui.fragment.ShipmentListFragment;

import java.util.List;

public class ClosedShipmentsFragment extends ShipmentListFragment {

    private GetClosedShipmentsTask getClosedShipmentsTask;

    public static ClosedShipmentsFragment newInstance() {
        ClosedShipmentsFragment fragment = new ClosedShipmentsFragment();
        return fragment;
    }

    @Override
    protected void getList() {
        if (getClosedShipmentsTask != null) {
            return;
        }
        this.showProgress(true);
        getClosedShipmentsTask = new GetClosedShipmentsTask();
        getClosedShipmentsTask.execute();
    }

    private class GetClosedShipmentsTask extends NetworkTask<Void, Void, List<Shipment>> {

        @Override
        protected List<Shipment> runInBackground(Void... voids) throws NetworkException {
            return networkClient.getClosedSenderShipments();
        }

        @Override
        protected void onPostExecute(List<Shipment> result) {
            showProgress(false);
            getClosedShipmentsTask = null;

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
