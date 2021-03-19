package org.goods2go.android.ui.fragment.sender;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.goods2go.models.Shipment;
import com.goods2go.models.ShipmentAnnouncement;
import com.goods2go.models.ShipmentRequest;

import org.goods2go.android.R;
import org.goods2go.android.network.NetworkException;
import org.goods2go.android.network.NetworkTask;
import org.goods2go.android.ui.activity.NetworkActivity;
import org.goods2go.android.ui.adapter.OpenAnnouncementAdapter;
import org.goods2go.android.ui.fragment.ShipmentFragment;
import org.goods2go.android.ui.fragment.ShipmentListFragment;

import java.util.List;

public class OpenAnnouncementsFragment extends ShipmentListFragment
        implements OpenAnnouncementAdapter.AcceptRequestListener,
        OpenAnnouncementAdapter.DetailViewListener{

    private GetOpenAnnouncementsTask getOpenAnnouncementsTask;
    private AcceptRequestTask acceptRequestTask;

    private long expandId = -1;

    public static OpenAnnouncementsFragment newInstance() {
        OpenAnnouncementsFragment fragment = new OpenAnnouncementsFragment();
        return fragment;
    }

    public void expandSpecificAnnouncement(long announcementId){
        this.expandId = announcementId;
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
        if (getOpenAnnouncementsTask != null) {
            return;
        }
        this.showProgress(true);
        getOpenAnnouncementsTask = new GetOpenAnnouncementsTask();
        getOpenAnnouncementsTask.execute();
    }

    private void displayAnnouncementList(List<ShipmentAnnouncement> dataSet){
        if(content instanceof ExpandableListView){
            ExpandableListView listView = (ExpandableListView) content;
            OpenAnnouncementAdapter adapter = new OpenAnnouncementAdapter(
                    getActivity(), this, this, dataSet);
            listView.setAdapter(adapter);
            if(dataSet.size() == 0) {
                content.setVisibility(View.GONE);
                noEntry.setVisibility(View.VISIBLE);
            } else {
                if(expandId != -1){
                    for(ShipmentAnnouncement announcement : dataSet){
                        if(announcement.getId() == expandId){
                            listView.expandGroup(dataSet.indexOf(announcement));
                        }
                    }
                    expandId = -1;
                }
            }
        }
    }

    @Override
    public void onRequestAccepted(ShipmentRequest shipmentRequest) {
        if (acceptRequestTask != null) {
            return;
        }

        this.showProgress(true);
        acceptRequestTask = new AcceptRequestTask();
        acceptRequestTask.execute(shipmentRequest);
    }

    @Override
    public void onDetailView(ShipmentAnnouncement shipmentAnnouncement) {
        ShipmentFragment shipmentFragment = ShipmentFragment.newInstance();
        shipmentFragment.setShipmentAnnouncement(shipmentAnnouncement);
        showLowerLevelFragment(shipmentFragment, false);
    }

    private class GetOpenAnnouncementsTask
            extends NetworkTask<Void, Void, List<ShipmentAnnouncement>> {

        @Override
        protected List<ShipmentAnnouncement> runInBackground(Void... voids) throws NetworkException {
            return networkClient.getOpenAnnouncements();
        }

        @Override
        protected void onPostExecute(List<ShipmentAnnouncement> result) {
            showProgress(false);
            getOpenAnnouncementsTask = null;

            if(networkException != null){
                showResult(R.string.error_network);
            }

            if (result != null) {
                displayAnnouncementList(result);
            } else {
                showResult(R.string.error_getting_data);
            }
        }
    }

    private class AcceptRequestTask extends NetworkTask<ShipmentRequest, Void, Shipment>{

        @Override
        protected Shipment runInBackground(ShipmentRequest... shipmentRequests)
                throws NetworkException {
            ShipmentRequest shipmentRequest = shipmentRequests[0];
            return networkClient.acceptShipmentRequest(shipmentRequest);
        }

        @Override
        protected void onPostExecute(Shipment result) {
            showProgress(false);
            acceptRequestTask = null;

            if(networkException != null){
                networkException.handleException((NetworkActivity)getActivity());
                return;
            }

            if(result != null){
                getList();
                showResult(R.string.info_request_accepted);
            } else {
                showResult(R.string.error_request_accept);
            }
        }
    }
}
