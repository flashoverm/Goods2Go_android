package org.goods2go.android.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.goods2go.models.PaymentInformation;
import com.goods2go.models.enums.IdentType;

import org.goods2go.android.R;
import org.goods2go.android.ui.view.IdentificationView;
import org.goods2go.android.ui.view.PaymentInformationView;

public class BecomeDelivererDialog extends AbstractDialogFragment {

    public interface BecomeDelivererListener{
        void onBecomeDeliverer(IdentType identType, String identificationNumber,
                               PaymentInformation paymentInformation) ;
    }

    private IdentificationView identificationView;
    private PaymentInformationView paymentInformationView;

    private BecomeDelivererListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_become_deliverer, null);

        identificationView = dialogView.findViewById(R.id.identification);
        paymentInformationView = dialogView.findViewById(R.id.payment_information);

        builder.setView(dialogView)
                .setTitle(getResources().getString(R.string.text_become_deliverer))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        BecomeDelivererDialog.this.getDialog().cancel();
                    }
                });
        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                final AlertDialog becomeDeliverer = (AlertDialog) dialogInterface;
                becomeDeliverer.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if(attemptBecomeDeliverer()){
                                    becomeDeliverer.dismiss();
                                }
                            }
                        });
            }
        });
        return dialog;
    }

    private boolean attemptBecomeDeliverer(){
        boolean cancel = false;
        View focusView = null;

        PaymentInformation paymentInformation = paymentInformationView.getPaymentInformation();
        if(paymentInformation == null){
            return false;
        }

        String identification = identificationView.getIdentification();
        if(identification == null){
            return false;
        }

        if(cancel) {
            focusView.requestFocus();
            return false;
        } else {
            listener.onBecomeDeliverer(
                    identificationView.getIdentificationType(),
                    identification,
                    paymentInformation);
            return true;
        }
    }

    @Override
    protected void onAttachCompatible(Context context) {
        try {
            listener = (BecomeDelivererListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement BecomeDelivererListener");
        }
    }
}
