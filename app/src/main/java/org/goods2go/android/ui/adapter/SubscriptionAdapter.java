package org.goods2go.android.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.goods2go.models.ShipmentSubscription;
import com.goods2go.models.util.DateTime;

import org.goods2go.android.R;
import org.goods2go.android.ui.view.AddressView;

import java.util.List;

public class SubscriptionAdapter extends RecyclerView.Adapter<SubscriptionAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout relativeLayout;
        AddressView to;
        AddressView from;
        TextView departure;
        TextView arrival;

        public ViewHolder(View v) {
            super(v);
            relativeLayout = v.findViewById(R.id.layout);
            to = v.findViewById(R.id.to);
            from = v.findViewById(R.id.from);
            departure = v.findViewById(R.id.departure);
            arrival = v.findViewById(R.id.arrival);
        }
    }

    public interface DetailViewListener{
        void onDetailView(ShipmentSubscription shipmentSubscription);
    }

    private DetailViewListener detailViewListener;
    private List<ShipmentSubscription> dataSet;

    public SubscriptionAdapter(DetailViewListener detailViewListener, List<ShipmentSubscription> dataSet) {
        this.detailViewListener = detailViewListener;
        this.dataSet = dataSet;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_subscription, parent, false);
        ViewHolder vh = new ViewHolder(layout);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final ShipmentSubscription subscription = dataSet.get(position);
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detailViewListener.onDetailView(subscription);
            }
        });
        holder.from.setTightAddress(subscription.getSource());
        holder.to.setTightAddress(subscription.getDestination());
        holder.departure.setText(DateTime.DATE_FORMAT.format(subscription.getPickupfrom()));
        holder.arrival.setText(DateTime.DATE_FORMAT.format(subscription.getDeliveruntil()));
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

}
