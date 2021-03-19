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


public class OpenAnnouncementAdapter extends BaseExpandableListAdapter {

    public interface AcceptRequestListener{
        void onRequestAccepted(ShipmentRequest shipmentRequest);
    }

    public interface DetailViewListener{
        void onDetailView(ShipmentAnnouncement shipmentAnnouncement);
    }

    private Context context;
    private List<ShipmentAnnouncement> dataSet;

    private AcceptRequestListener acceptRequestListener;
    private DetailViewListener detailViewListener;

    public OpenAnnouncementAdapter(Context context, AcceptRequestListener acceptRequestListener,
                   DetailViewListener detailViewListener, List<ShipmentAnnouncement> dataSet) {
        this.context = context;
        this.acceptRequestListener = acceptRequestListener;
        this.detailViewListener = detailViewListener;
        this.dataSet = dataSet;
    }

    @Override
    public int getGroupCount() {
        return this.dataSet.size();
    }

    @Override
    public int getChildrenCount(int i) {
        ShipmentAnnouncement announcement = this.dataSet.get(i);
        if(announcement.getShipmentrequests() != null){
            return this.dataSet.get(i).getShipmentrequests().size()+1;
        } else {
            return 1;
        }
    }

    @Override
    public Object getGroup(int i) {
        return this.dataSet.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return this.dataSet.get(i).getShipmentrequests().get(i1);
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
        to.setAddress(shipmentAnnouncement.getDestination());
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

            TextView deliverer = view.findViewById(R.id.deliverer);
            deliverer.setText(shipmentRequest.getDeliverer().getDisplayNameOrMail());
            RatingBar rating = view.findViewById(R.id.rating_deliverer_avg);
            rating.setRating(shipmentRequest.getDeliverer().getDelivererrating());

            TextView pickupDatetime = view.findViewById(R.id.pickup_datetime);
            TextView deliveryDatetime = view.findViewById(R.id.delivery_datetime);
            String oClock = context.getString(R.string.text_o_clock);
            pickupDatetime.setText(DateTime.DATETIME_FORMAT.format(
                    shipmentRequest.getPickupdatetime()) + " " + oClock);
            deliveryDatetime.setText(DateTime.DATETIME_FORMAT.format(
                    shipmentRequest.getDeliverydatetime()) + " " + oClock);

            Button accept = view.findViewById(R.id.button_action);
            accept.setText(R.string.text_accept);
            accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    acceptRequestListener.onRequestAccepted(shipmentRequest);
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
