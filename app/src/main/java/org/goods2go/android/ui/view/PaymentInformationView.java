package org.goods2go.android.ui.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.goods2go.models.PaymentInformation;
import com.goods2go.models.enums.PaymentType;

import org.goods2go.android.R;
import org.goods2go.android.ui.adapter.EnumAdapter;

public class PaymentInformationView extends LinearLayout{

    private View view;

    private Spinner editPaymentType;
    private LinearLayout paypalLayout;
    private EditText editPaypal;
    private LinearLayout bankTransferLayout;
    private EditText editAccountHolder;
    private EditText editIban;

    public PaymentInformationView(Context context) {
        super(context);
        initLayout();
    }

    public PaymentInformationView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initLayout();
    }

    private void initLayout(){
        view = inflate(getContext(), R.layout.layout_payment_information, this);

        paypalLayout = view.findViewById(R.id.paypal_layout);
        bankTransferLayout = view.findViewById(R.id.banktransfer_layout);

        editPaymentType = view.findViewById(R.id.edit_payment_type);
        editPaymentType.setAdapter(new EnumAdapter(getContext(), PaymentType.values()));
        editPaymentType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setPaymentView((PaymentType)editPaymentType.getSelectedItem());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        editPaypal = view.findViewById(R.id.edit_paypal);
        editAccountHolder = view.findViewById(R.id.edit_account_holder);
        editIban = view.findViewById(R.id.edit_iban);
    }

    private void setPaymentView(PaymentType type){
        if(type.equals(PaymentType.BANKTRANSFER)){
            bankTransferLayout.setVisibility(View.VISIBLE);
            paypalLayout.setVisibility(View.GONE);
        } else if(type.equals(PaymentType.PAYPAL)){
            bankTransferLayout.setVisibility(View.GONE);
            paypalLayout.setVisibility(View.VISIBLE);
        }
    }

    public PaymentInformation getPaymentInformation() {
        editPaypal.setError(null);
        editAccountHolder.setError(null);
        editIban.setError(null);

        boolean cancel = false;
        View focusView = null;

        PaymentType type = (PaymentType)editPaymentType.getSelectedItem();
        String accountHolder = editAccountHolder.getText().toString();
        String identifier = "";

        if(type.equals(PaymentType.PAYPAL)){
            identifier = editPaypal.getText().toString();
            if(TextUtils.isEmpty(identifier)){
                editPaypal.setError(getContext().getString(R.string.error_field_required));
                focusView = editPaypal;
                cancel = true;
            }
        }
        else if(type.equals(PaymentType.BANKTRANSFER)){
            identifier = editIban.getText().toString();
            if(TextUtils.isEmpty(identifier)){
                editIban.setError(getContext().getString(R.string.error_field_required));
                focusView = editIban;
                cancel = true;
            }
            if(TextUtils.isEmpty(accountHolder)){
                editAccountHolder.setError(getContext().getString(R.string.error_field_required));
                focusView = editAccountHolder;
                cancel = true;
            }
        }

        if(cancel){
            focusView.requestFocus();
            return null;
        } else {
            PaymentInformation paymentInformation = new PaymentInformation(type, identifier);
            if(type.equals(PaymentType.BANKTRANSFER)) {
                paymentInformation.setName(accountHolder);
            }

            return paymentInformation;
        }
    }

    public void setPaymentInformation(PaymentInformation paymentInformation){
        if(paymentInformation != null){
            editPaymentType.setSelection(paymentInformation.getPaymentType().ordinal());

            if(paymentInformation.getPaymentType().equals(PaymentType.PAYPAL)){
                editPaypal.setText(paymentInformation.getIdentifier());
            } else if(paymentInformation.getPaymentType().equals(PaymentType.BANKTRANSFER)){
                editAccountHolder.setText(paymentInformation.getName());
                editIban.setText(paymentInformation.getIdentifier());
            }
        }

    }
}
