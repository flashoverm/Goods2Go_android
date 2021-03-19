package org.goods2go.android.ui.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.goods2go.models.ShipmentSize;

import org.goods2go.android.R;

import java.util.List;

public class SizePickerView extends LinearLayout {

    private View view;

    private SeekBar editSize;
    private LinearLayout sizetags;
    private TextView description;
    private List<ShipmentSize> sizes;
    private ShipmentSize size;

    public SizePickerView(Context context) {
        super(context);
    }

    public SizePickerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setSizes(List<ShipmentSize> sizes){
        this.sizes = sizes;
        if(sizes != null){
            initView();
        }
    }

    private void initView(){
        if(sizes.size() > 0){
            size = sizes.get(0);
        }
        view = inflate(getContext(), R.layout.layout_size_picker, this);
        description = view.findViewById(R.id.size_description);
        description.setText(getDescription(getContext(), size));
        editSize = view.findViewById(R.id.size_picker);
        initSizeTags(sizes);
        editSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                size = sizes.get(i);
                description.setText(getDescription(getContext(), size));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void initSizeTags(List<ShipmentSize> sizes){
        editSize.setMax(sizes.size()-1);
        LinearLayout.LayoutParams layoutParam = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1f);
        layoutParam.width =0;
        layoutParam.weight = 1;
        sizetags = view.findViewById(R.id.size_tags);

        ShipmentSize size = sizes.get(0);
        TextView view = new TextView(getContext());
        view.setText(size.getName());
        view.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        sizetags.addView(view);

        for(int i=1; i<sizes.size(); i++){
            size = sizes.get(i);
            TextView additionalView = new TextView(getContext());
            additionalView.setText(size.getName());
            additionalView.setLayoutParams(layoutParam);
            additionalView.setTextAlignment(TextView.TEXT_ALIGNMENT_VIEW_END);
            sizetags.addView(additionalView);
        }
    }

    public ShipmentSize getShipmentSize(){
        return size;
    }

    public void setShipmentSize(ShipmentSize shipmentSize){
        if(sizes != null){
            editSize.setProgress(sizes.indexOf(shipmentSize));//getShipmentSizeID(shipmentSize));
        }
    }

    /*
    public static List<ShipmentSize> getShipmentSizes(){
        ArrayList<ShipmentSize> list = new ArrayList<>();
        list.add(new ShipmentSize("XS", "cm/kg", 10, 10, 10, 0.5f, 0.25f));
        list.add(new ShipmentSize("S", "cm/kg", 10, 10, 10, 0.5f, 0.25f));
        list.add(new ShipmentSize("M", "cm/kg", 25, 25, 25, 1, 0.5f));
        list.add(new ShipmentSize("L", "cm/kg", 50, 50, 50, 2, 1));
        list.add(new ShipmentSize("XL", "cm/kg", 100, 100, 100, 5, 2));
        list.add(new ShipmentSize("XXL", "cm/kg", 100, 100, 100, 5, 2));
        return list;
    }
    */

    public static String getDescription(Context context, ShipmentSize shipmentSize){
        return context.getString(R.string.text_until)
                + " "
                + (int)shipmentSize.getMaxlength()
                + " x "
                + (int)shipmentSize.getMaxwidth()
                + " x "
                + (int)shipmentSize.getMaxheight()
                + "cm, "
                + (int)shipmentSize.getMaxweight()
                + "kg";
    }
}
