package org.goods2go.android.ui.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.goods2go.models.Shipment;
import com.goods2go.models.ShipmentAnnouncement;

import org.goods2go.android.R;
import org.goods2go.android.util.StringLookup;

public class ShipmentDetailView extends LinearLayout {

    private View view;

    private Shipment shipment;
    private ShipmentAnnouncement announcement;

    public ShipmentDetailView(Context context) {
        super(context);
        view = inflate(context, R.layout.layout_shipment_details, this);
    }

    public ShipmentDetailView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        view = inflate(context, R.layout.layout_shipment_details, this);
    }

    public void setShipment(Shipment shipment) {
        this.shipment = shipment;
        TextView description = view.findViewById(R.id.detail_description);
        ShipmentSizeView size = view.findViewById(R.id.detail_size);
        TextView price = view.findViewById(R.id.detail_price);

        description.setText(shipment.getDescription());
        size.setShipmentSize(shipment.getSize());
        price.setText(shipment.getPrice() + " " + getContext().getString(R.string.text_currency));
    }

    public void setAnnouncement(ShipmentAnnouncement announcement) {
        this.announcement = announcement;
        TextView description = view.findViewById(R.id.detail_description);
        ShipmentSizeView size = view.findViewById(R.id.detail_size);
        TextView price = view.findViewById(R.id.detail_price);

        description.setText(announcement.getDescription());
        size.setShipmentSize(announcement.getSize());
        price.setText(announcement.getPrice() + " " + getContext().getString(R.string.text_currency));

        RelativeLayout stateLayout = view.findViewById(R.id.layout_state);
        stateLayout.setVisibility(VISIBLE);

        TextView status = view.findViewById(R.id.detail_state);
        status.setText(StringLookup.getIdFrom(announcement.getStatus()));
    }

    public void showState() {
        if(shipment != null || announcement != null){
            RelativeLayout stateLayout = view.findViewById(R.id.layout_state);
            stateLayout.setVisibility(VISIBLE);

            TextView status = view.findViewById(R.id.detail_state);
            if(shipment != null){
                status.setText(StringLookup.getIdFrom(shipment.getStatus()));
            } else {
                status.setText(StringLookup.getIdFrom(announcement.getStatus()));
            }
        }
    }

    public void showUser(boolean sender) {
        if(shipment != null || announcement != null){
            RelativeLayout userLayout = view.findViewById(R.id.layout_user);
            userLayout.setVisibility(VISIBLE);

            TextView textUser = view.findViewById(R.id.detail_text_user);
            TextView userView = view.findViewById(R.id.detail_user);
            RatingBar userAvgRating = view.findViewById(R.id.detail_rating_user);
            if(shipment != null){
                if(sender){
                    textUser.setText(R.string.text_sender);
                    userView.setText(shipment.getSender().getDisplayNameOrMail());
                    userAvgRating.setRating(shipment.getSender().getSenderrating());
                } else {
                    textUser.setText(R.string.text_deliverer);
                    userView.setText(shipment.getDeliverer().getDisplayNameOrMail());
                    userAvgRating.setRating(shipment.getDeliverer().getDelivererrating());
                }
            }
            if(announcement != null){
                textUser.setText(R.string.text_sender);
                userView.setText(announcement.getSender().getDisplayNameOrMail());
                userAvgRating.setRating(announcement.getSender().getSenderrating());
            }
        }

    }
}
