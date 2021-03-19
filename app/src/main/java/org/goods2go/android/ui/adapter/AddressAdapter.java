package org.goods2go.android.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.goods2go.models.Address;

import org.goods2go.android.R;
import org.goods2go.android.ui.view.AddressView;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.ViewHolder>{

    public static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout layout;
        AddressView address;

        public ViewHolder(View v) {
            super(v);
            layout = v.findViewById(R.id.layout);
            address = v.findViewById(R.id.address);
        }
    }

    public interface OnClickListener{
        void onClick(Address address);
    }

    private OnClickListener listener;
    private List<Address> dataSet;

    public AddressAdapter(OnClickListener listener, List<Address> dataSet) {
        this.listener = listener;
        this.dataSet = dataSet;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_address, parent, false);
        ViewHolder vh = new ViewHolder(layout);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Address address = dataSet.get(position);
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClick(address);
            }
        });
        holder.address.setAddress(address);
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}
