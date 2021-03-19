package org.goods2go.android.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.goods2go.models.ShipmentAnnouncement;
import com.goods2go.models.ShipmentRequest;
import com.goods2go.models.util.DateTime;

import org.goods2go.android.R;
import org.goods2go.android.ui.view.AddressView;

import java.util.List;

public class OpenRequestsAdapter extends BaseExpandableListAdapter {

    public interface RevokeRequestListener{
        void onRequestRevoked(ShipmentRequest shipmentRequest);
    }

    public interface DetailViewListener{
        void onDetailView(ShipmentAnnouncement shipmentAnnouncement);
    }

    private Context context;
    private List<ShipmentRequest> dataSet;

    private RevokeRequestListener revokeRequestListener;
    private DetailViewListener detailViewListener;

    public OpenRequestsAdapter(Context context, RevokeRequestListener revokeRequestListener,
                     DetailViewListener detailViewListener, List<ShipmentRequest> dataSet) {
        this.context = context;
        this.revokeRequestListener = revokeRequestListener;
        this.detailViewListener = detailViewListener;
        this.dataSet = dataSet;
    }

    @Override
    public int getGroupCount() {
        return this.dataSet.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return 1+1;
    }

    @Override
    public Object getGroup(int i) {
        return this.dataSet.get(i).getShipmentannouncement();
    }

    @Override
    public Object getChild(int i, int i1) {
        return this.dataSet.get(i);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        ShipmentAnnouncement shipmentAnnouncement = (ShipmentAnnouncement) getGroup(i);
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.listitem_open_announcement, null);
        }

        TextView description = view.findViewById(R.id.description);
        description.setText(shipmentAnnouncement.getDescription());
        AddressView to = view.findViewById(R.id.to);
        to.setAddress(AddressView.censorAddress(shipmentAnnouncement.getDestination()));
        TextView date = view.findViewById(R.id.deliverydate);
        date.setText(DateTime.DATE_FORMAT.format(shipmentAnnouncement.getEarliestpickupdate()));

        ImageView arrow = view.findViewById(R.id.arrow);
        arrow.setImageResource(b?
                R.drawable.ic_expand_less_black_18dp:R.drawable.ic_expand_more_black_18dp);
        return view;
    }

    @Override
    public View getChildView(final int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        LayoutInflater layoutInflater = (LayoutInflater) this.context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(i1 == getChildrenCount(i)-1){
            view = layoutInflater.inflate(R.layout.listitem_to_detail, null);
            view.findViewById(R.id.detailView).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    detailViewListener.onDetailView((ShipmentAnnouncement)getGroup(i));
                }
            });
        } else {
            final ShipmentRequest shipmentRequest = (ShipmentRequest) getChild(i, i1);
            view = layoutInflater.inflate(R.layout.listitem_open_request, null);

            TextView textDeliverer = view.findViewById(R.id.text_deliverer);
            textDeliverer.setVisibility(View.GONE);
            TextView deliverer = view.findViewById(R.id.deliverer);
            deliverer.setVisibility(View.GONE);
            RatingBar rating = view.findViewById(R.id.rating_deliverer_avg);
            rating.setVisibility(View.GONE);

            TextView pickupDatetime = view.findViewById(R.id.pickup_datetime);
            TextView deliveryDatetime = view.findViewById(R.id.delivery_datetime);
            String oClock = context.getString(R.string.text_o_clock);
            pickupDatetime.setText(DateTime.DATETIME_FORMAT.format(
                    shipmentRequest.getPickupdatetime()) + " " + oClock);
            deliveryDatetime.setText(DateTime.DATETIME_FORMAT.format(
                    shipmentRequest.getDeliverydatetime()) + " " + oClock);

            Button revoke = view.findViewById(R.id.button_action);
            revoke.setText(R.string.text_revoke);
            revoke.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    revokeRequestListener.onRequestRevoked(shipmentRequest);
                }
            });
        }
        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }
}