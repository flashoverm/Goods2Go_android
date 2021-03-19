package org.goods2go.android.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.goods2go.models.Shipment;

import org.goods2go.android.R;
import org.goods2go.android.ui.adapter.ShipmentAdapter;

import java.util.List;

public abstract class ShipmentListFragment extends NetworkFragment
        implements ShipmentAdapter.DetailViewListener{

    protected TextView noEntry;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_list_recycleview, container, false);
        progress = view.findViewById(R.id.progress);
        content = view.findViewById(R.id.list);
        noEntry = view.findViewById(R.id.no_entries);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getList();
    }

    protected abstract void getList();

    protected void displayList(List<Shipment> result){
        if (result.size() > 0 && content instanceof RecyclerView) {
            RecyclerView recyclerView = (RecyclerView) content;
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.addItemDecoration(
                    new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
            ShipmentAdapter adapter = new ShipmentAdapter(getActivity(), this, result);
            recyclerView.setAdapter(adapter);
        } else {
            content.setVisibility(View.GONE);
            noEntry.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDetailView(Shipment shipment) {
        ShipmentFragment shipmentFragment = ShipmentFragment
                .newInstance();
        shipmentFragment.setShipment(shipment);
        showLowerLevelFragment(shipmentFragment, false);
    }
}
