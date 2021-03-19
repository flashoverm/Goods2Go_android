package org.goods2go.android.geo;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.goods2go.models.ShipmentAnnouncement;
import com.goods2go.models.util.DateTime;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import org.goods2go.android.R;
import org.goods2go.android.ui.view.AddressView;

public class MarkerAdapter implements GoogleMap.InfoWindowAdapter {

    private Activity activity;

    public MarkerAdapter(Activity activity){
        this.activity = activity;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        Object tag = marker.getTag();
        if(tag == null){
            return null;
        }

        View v = activity.getLayoutInflater().inflate(R.layout.layout_marker_infowindow, null);

        TextView title = v.findViewById(R.id.title);
        AddressView to = v.findViewById(R.id.to);
        TextView pickup_from = v.findViewById(R.id.pickup_from);
        TextView deliver_until = v.findViewById(R.id.deliver_until);


        ShipmentAnnouncement announcement = (ShipmentAnnouncement) tag;
        if(announcement != null){
            title.setText(marker.getTitle());
            to.setTightAddress(AddressView.censorAddress(announcement.getDestination()));
            pickup_from.setText(DateTime.DATE_FORMAT.format(announcement.getEarliestpickupdate()));
            deliver_until.setText(DateTime.DATE_FORMAT.format(announcement.getLatestdeliverydate()));
        }
        return v;
    }
}
