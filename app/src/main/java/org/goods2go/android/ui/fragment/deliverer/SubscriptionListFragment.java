package org.goods2go.android.ui.fragment.deliverer;

import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.goods2go.models.ShipmentSubscription;

import org.goods2go.android.R;
import org.goods2go.android.network.NetworkException;
import org.goods2go.android.network.NetworkTask;
import org.goods2go.android.ui.activity.NetworkActivity;
import org.goods2go.android.ui.adapter.SubscriptionAdapter;
import org.goods2go.android.ui.fragment.ShipmentListFragment;

import java.util.List;

public class SubscriptionListFragment extends ShipmentListFragment
        implements SubscriptionAdapter.DetailViewListener {

    private GetSubscriptionsTask getSubscriptionsTask;

    public static ActiveShipmentsFragment newInstance() {
        ActiveShipmentsFragment fragment = new ActiveShipmentsFragment();
        return fragment;
    }

    @Override
    protected void getList(){
        if (getSubscriptionsTask != null) {
            return;
        }
        this.showProgress(true);
        getSubscriptionsTask = new GetSubscriptionsTask();
        getSubscriptionsTask.execute();
    }

    protected void displaySubscriptionList(List<ShipmentSubscription> result){
        if (result.size() > 0 && content instanceof RecyclerView) {
            RecyclerView recyclerView = (RecyclerView) content;
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.addItemDecoration(
                    new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
            SubscriptionAdapter adapter = new SubscriptionAdapter(this, result);
            recyclerView.setAdapter(adapter);
        } else {
            content.setVisibility(View.GONE);
            noEntry.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDetailView(ShipmentSubscription subscription) {
        SubscriptionFragment subscriptionFragment = SubscriptionFragment.newInstance();
        subscriptionFragment.setShipmentSubscription(subscription);
        showLowerLevelFragment(subscriptionFragment, false);
    }

    private class GetSubscriptionsTask extends NetworkTask<Void, Void, List<ShipmentSubscription>> {

        @Override
        protected List<ShipmentSubscription> runInBackground(Void... voids) throws NetworkException {
            return networkClient.getShipmentSubscriptions();
        }

        @Override
        protected void onPostExecute(List<ShipmentSubscription> result) {
            showProgress(false);
            getSubscriptionsTask = null;

            if(networkException != null){
                networkException.handleException((NetworkActivity)getActivity());
                return;
            }

            if (result != null) {
                displaySubscriptionList(result);
            } else {
                showResult(R.string.error_getting_data);
            }
        }
    }
}
