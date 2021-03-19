package org.goods2go.android.ui.fragment.deliverer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.goods2go.models.ShipmentAnnouncement;
import com.goods2go.models.ShipmentRequest;

import org.goods2go.android.R;
import org.goods2go.android.network.NetworkException;
import org.goods2go.android.network.NetworkTask;
import org.goods2go.android.ui.adapter.OpenRequestsAdapter;
import org.goods2go.android.ui.fragment.ShipmentFragment;
import org.goods2go.android.ui.fragment.ShipmentListFragment;

import java.net.HttpURLConnection;
import java.util.List;

public class OpenRequestsFragment extends ShipmentListFragment
        implements OpenRequestsAdapter.RevokeRequestListener,
        OpenRequestsAdapter.DetailViewListener{


    private GetOpenRequestsTask getOpenRequestsTask;
    private RevokeRequestTask revokeRequestTask;

    public static OpenRequestsFragment newInstance() {
        OpenRequestsFragment fragment = new OpenRequestsFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_expandable, container, false);
        progress = view.findViewById(R.id.progress);
        content = view.findViewById(R.id.content);
        noEntry = view.findViewById(R.id.no_entries);

        return view;
    }

    @Override
    protected void getList() {
        if (getOpenRequestsTask != null) {
            return;
        }
        this.showProgress(true);
        getOpenRequestsTask = new GetOpenRequestsTask();
        getOpenRequestsTask.execute();
    }

    private void displayRequestList(List<ShipmentRequest> dataSet){
        if(content instanceof ExpandableListView){
            ExpandableListView listView = (ExpandableListView) content;
            OpenRequestsAdapter adapter = new OpenRequestsAdapter(
                    getActivity(), this, this, dataSet);
            listView.setAdapter(adapter);
            if(dataSet.size() == 0) {
                content.setVisibility(View.GONE);
                noEntry.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onRequestRevoked(ShipmentRequest shipmentRequest) {
        if (revokeRequestTask != null) {
            return;
        }

        this.showProgress(true);
        revokeRequestTask = new RevokeRequestTask();
        revokeRequestTask.execute(shipmentRequest);
    }

    @Override
    public void onDetailView(ShipmentAnnouncement shipmentAnnouncement) {
        ShipmentFragment shipmentFragment = ShipmentFragment.newInstance();
        shipmentFragment.setShipmentAnnouncement(shipmentAnnouncement);
        showLowerLevelFragment(shipmentFragment, false);
    }

    private class GetOpenRequestsTask
            extends NetworkTask<Void, Void, List<ShipmentRequest>> {

        @Override
        protected List<ShipmentRequest> runInBackground(Void... voids) throws NetworkException {
            return networkClient.getOpenRequests();
        }

        @Override
        protected void onPostExecute(List<ShipmentRequest> result) {
            showProgress(false);
            getOpenRequestsTask = null;

            if(networkException != null){
                showResult(R.string.error_network);
            }

            if (result != null) {
                displayRequestList(result);
            } else {
                showResult(R.string.error_getting_data);
            }
        }
    }

    private class RevokeRequestTask extends NetworkTask<ShipmentRequest, Void, Boolean>{

        @Override
        protected Boolean runInBackground(ShipmentRequest... shipmentRequests)
                throws NetworkException {
            ShipmentRequest shipmentRequest = shipmentRequests[0];
            return networkClient.revokeShipmentRequest(shipmentRequest);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            showProgress(false);
            revokeRequestTask = null;

            if(networkException != null){
                if(networkException.httpError == HttpURLConnection.HTTP_FORBIDDEN){
                    showResult(R.string.error_forbidden);
                    return;
                }
                showResult(R.string.error_network);
            }

            if(result){
                getList();
                showResult(R.string.info_request_revoked);
            } else {
                showResult(R.string.error_request_revoked);
            }
        }
    }
}
