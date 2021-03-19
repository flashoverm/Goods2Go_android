package org.goods2go.android.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.goods2go.models.enums.IdentType;
import com.goods2go.models.enums.PaymentType;

import org.goods2go.android.util.StringLookup;

public class EnumAdapter extends ArrayAdapter {

    public EnumAdapter(@NonNull Context context, @NonNull Object[] objects) {
        super(context, android.R.layout.simple_spinner_item, objects);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView view = (TextView) super.getDropDownView(position, convertView, parent);
        return getEnumView(position, view);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView view = (TextView) super.getView(position, convertView, parent);
        return getEnumView(position, view);
    }

    private View getEnumView(int position, TextView view){
        Class<?> classType = getItem(position).getClass();
        if(classType.equals(IdentType.class)){
            view.setText(StringLookup.getIdFrom(IdentType.class.cast(getItem(position))));
        } else if(classType.equals(PaymentType.class)){
            view.setText(StringLookup.getIdFrom(PaymentType.class.cast(getItem(position))));
        }
        view.setPadding(30, 30, 30, 30);
        view.setTextSize(16);
        return view;
    }
}
