package org.goods2go.android.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.goods2go.models.Shipment;
import com.goods2go.models.util.DateTime;

import org.goods2go.android.R;
import org.goods2go.android.ui.view.AddressView;
import org.goods2go.android.util.StringLookup;

import java.util.List;

public class ShipmentAdapter extends RecyclerView.Adapter<ShipmentAdapter.ViewHolder> {

    static class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout relativeLayout;
        TextView description;
        TextView status;
        AddressView to;
        TextView date;

        ViewHolder(View v) {
            super(v);
            relativeLayout = v.findViewById(R.id.layout);
            description = v.findViewById(R.id.description);
            status = v.findViewById(R.id.status);
            to = v.findViewById(R.id.to);
            date = v.findViewById(R.id.deliverydate);
        }
    }

    public interface DetailViewListener{
        void onDetailView(Shipment shipment);
    }

    private Context context;
    private DetailViewListener detailViewListener;
    private List<Shipment> dataSet;

    public ShipmentAdapter(Context context, DetailViewListener detailViewListener,
                           List<Shipment> dataSet) {
        this.context = context;
        this.detailViewListener = detailViewListener;
        this.dataSet = dataSet;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_shipment, parent, false);
        return new ViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Shipment shipment = dataSet.get(position);
        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detailViewListener.onDetailView(shipment);
            }
        });
        holder.description.setText(shipment.getDescription());
        holder.status.setVisibility(View.VISIBLE);
        holder.status.setText(
                " (" + context.getString(StringLookup.getIdFrom(shipment.getStatus())) + ")");
        holder.to.setAddress(shipment.getDestination());
        holder.date.setText(DateTime.DATE_FORMAT.format(shipment.getNegdeliverydatetime()));
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}
