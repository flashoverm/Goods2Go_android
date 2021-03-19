package org.goods2go.android.ui.fragment.deliverer;

import com.goods2go.models.Shipment;

import org.goods2go.android.R;
import org.goods2go.android.network.NetworkException;
import org.goods2go.android.network.NetworkTask;
import org.goods2go.android.ui.adapter.ShipmentAdapter;
import org.goods2go.android.ui.fragment.ShipmentListFragment;

import java.util.List;

public class EndDeliveryListFragment extends ShipmentListFragment
        implements ShipmentAdapter.DetailViewListener{

    private GetActiveShipmentsTask getActiveShipmentsTask;

    public static EndDeliveryListFragment newInstance() {
        EndDeliveryListFragment fragment = new EndDeliveryListFragment();
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

    @Override
    public void onDetailView(Shipment shipment) {
        EndDeliveryFragment endDelivery = EndDeliveryFragment.newInstance();
        endDelivery.setShipment(shipment);
        showLowerLevelFragment(endDelivery, false);
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
                showResult(R.string.error_network);
            }

            if (result != null) {
                displayList(result);
            } else {
                showResult(R.string.error_getting_data);
            }
        }
    }
}
