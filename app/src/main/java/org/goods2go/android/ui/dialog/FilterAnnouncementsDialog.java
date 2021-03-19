package org.goods2go.android.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.goods2go.models.ShipmentSubscription;
import com.goods2go.models.util.DateTime;

import org.goods2go.android.R;
import org.goods2go.android.geo.GeoClient;
import org.goods2go.android.ui.fragment.deliverer.SubscribeShipmentsFragment;

public class FilterAnnouncementsDialog extends SubscribeShipmentsFragment {

    public static final String TAG = "FilterDialog";
    private static final String FILTER_KEY = "criteria";

    public interface ApplyFilterListener{
        void onFilterApplied(ShipmentSubscription announcementFilter);
    }

    private AlertDialog dialog;
    private ApplyFilterListener applyFilterListener;

    public static FilterAnnouncementsDialog newInstance(ShipmentSubscription filter) {
        Bundle args = new Bundle();
        if(filter != null){
            args.putSerializable(FILTER_KEY, filter);
        }
        FilterAnnouncementsDialog fragment = new FilterAnnouncementsDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void onAttachCompatible(Context context) {
        try {
            applyFilterListener = (ApplyFilterListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(getTargetFragment().toString()
                    + " must implement ApplyFilterListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = initView();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setTitle(R.string.text_filter)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });

        dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                setupCriteria();
                            }
                        });
            }
        });

        setFilter((ShipmentSubscription)getArguments().getSerializable(FILTER_KEY));

        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
    }

    private void setFilter(ShipmentSubscription filter){
        if(filter == null){
            initCriteria();
        } else {
            this.criteria = filter;
            editPickupFrom.setText(DateTime.DATE_FORMAT.format(filter.getPickupfrom()));
            editDeliverUntil.setText(DateTime.DATE_FORMAT.format(filter.getDeliveruntil()));
            sizePickerView.setShipmentSize(filter.getMaxsize());
            if(filter.getSource() != null){
                editSourceCity.setText(filter.getSource().getCity());
                editSourcePostcode.setText(filter.getSource().getPostcode());

            } else if(filter.getSourcecoordinates() != null) {
                editSourceCity.setText(R.string.text_own_location);
                editSourcePostcode.setText("");
            }
            if(filter.getDestination() != null){
                editDestinationCity.setText(filter.getDestination().getCity());
                editDestinationPostcode.setText(filter.getDestination().getPostcode());
            }
            editRadius.setProgress(filter.getRadius());
            if(criteria.getSourcecoordinates() != null)
            this.location = GeoClient.getLatLng(criteria.getSourcecoordinates());
        }
    }

    @Override
    protected void applyCriteria() {
        showProgress(false);
        applyFilterListener.onFilterApplied(criteria);
        dialog.dismiss();
    }
}
