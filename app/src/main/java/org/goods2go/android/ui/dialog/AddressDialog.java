package org.goods2go.android.ui.dialog;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.goods2go.models.Address;

import org.goods2go.android.R;

public class AddressDialog extends AbstractDialogFragment {

    public static final String TAG = "AddressDialog";
    public static final String ADDRESS_KEY = "address";
    private static final String FIELD_NUMBER = "fieldNumber";
    private static final String TITLE = "title";

    public interface AddressSetListener{
        void onAddressSet(Address address, int fieldNumber);
    }

    /**  0 firstName 1 lastName
     *   2 street    3 streetNumber
     *   4 postcode  5 city
     *   6 country
     */
    private EditText[] editData;

    private AddressSetListener addressSetListener;

    public static AddressDialog newInstance(Address address, int fieldNumber, int titleResId) {
        AddressDialog fragment = new AddressDialog();
        Bundle args = new Bundle();
        args.putInt(FIELD_NUMBER, fieldNumber);
        args.putInt(TITLE, titleResId);
        if(address != null){
            args.putSerializable(ADDRESS_KEY, address);
        }
        fragment.setArguments(args);
        return fragment;
    }

    public static AddressDialog newInstance(Address address, int titleResId) {
        return newInstance(address, -1, titleResId);
    }

    @Override
    protected void onAttachCompatible(Context context) {
        try {
            addressSetListener = (AddressSetListener) context;
        } catch (ClassCastException e) {
            Fragment target = getTargetFragment();
            if(target != null){
                addressSetListener = (AddressSetListener) target;
            } else {
                throw new ClassCastException(getTargetFragment().toString()
                        + " must implement AddressSetListener");
            }
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_address, null);

        editData = new EditText[7];
        editData[0] = view.findViewById(R.id.edit_firstname);
        editData[1] = view.findViewById(R.id.edit_lastname);
        editData[2] = view.findViewById(R.id.edit_street);
        editData[3] = view.findViewById(R.id.edit_number);
        editData[4] = view.findViewById(R.id.edit_postcode);
        editData[5] = view.findViewById(R.id.edit_city);
        editData[6] = view.findViewById(R.id.edit_country);

        setAddress((Address)getArguments().getSerializable(ADDRESS_KEY));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setTitle(getArguments().getInt(TITLE));
        builder.setPositiveButton(R.string.text_save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                final AlertDialog setAddress = (AlertDialog) dialogInterface;
                setAddress.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if(attemptSetAddress()){
                                    setAddress.dismiss();
                                }
                            }
                        });
            }
        });

        return dialog;
    }

    private void setAddress(Address address){
        if(address != null){
            editData[0].setText(address.getFirstname());
            editData[1].setText(address.getLastname());
            editData[2].setText(address.getStreet());
            editData[3].setText(address.getStreetno());
            editData[4].setText(address.getPostcode());
            editData[5].setText(address.getCity());
            editData[6].setText(address.getCountry());
        }
    }

    private boolean attemptSetAddress(){
        boolean cancel = false;
        View focusView = null;

        /*  0 firstName 1 lastName
         *  2 street    3 streetNumber
         *  4 postcode  5 city
         *  6 country
         */
        String[] data = new String[editData.length];
        for(int i=editData.length-1; i>=0; i--){
            editData[i].setError(null);
            data[i] = editData[i].getText().toString();
            if(TextUtils.isEmpty(data[i])){
                editData[i].setError(getString(R.string.error_field_required));
                cancel = true;
                focusView = editData[i];
            }
        }
        if(cancel) {
            focusView.requestFocus();
            return false;
        } else {
            Address address = new Address(data[1], data[0],
                    data[3], data[2], data[4], data[5], data[6]);
            addressSetListener.onAddressSet(address, getArguments().getInt(FIELD_NUMBER, -1));
            return true;
        }
    }
}
