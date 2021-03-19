package org.goods2go.android.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.goods2go.models.ShipmentSize;

import org.goods2go.android.R;

public class ShipmentSizeView extends RelativeLayout {

    private View view;

    public ShipmentSizeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        view = inflate(context, R.layout.layout_shipment_size, this);
    }

    public ShipmentSizeView(Context context) {
        super(context);
        view = inflate(context, R.layout.layout_shipment_size, this);
    }

    public void setShipmentSize(ShipmentSize shipmentSize){
        TextView sizeName = view.findViewById(R.id.size_name);
        TextView size = view.findViewById(R.id.size_descr);

        size.setText(SizePickerView.getDescription(getContext(),shipmentSize));
        sizeName.setText(shipmentSize.getName());
    }

}
