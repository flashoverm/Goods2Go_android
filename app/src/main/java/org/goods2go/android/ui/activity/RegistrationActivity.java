package org.goods2go.android.ui.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.goods2go.models.PaymentInformation;
import com.goods2go.models.User;

import org.goods2go.android.R;
import org.goods2go.android.network.NetworkException;
import org.goods2go.android.network.NetworkTask;
import org.goods2go.android.ui.view.IdentificationView;
import org.goods2go.android.ui.view.PaymentInformationView;

import java.net.HttpURLConnection;

public class RegistrationActivity extends NetworkActivity {

    private EditText editMail;
    private EditText editDisplayName;
    private EditText editPassword;
    private EditText editPasswordProof;

    private CheckBox editBecomeDeliverer;
    private IdentificationView identificationView;
    private PaymentInformationView paymentInformationView;

    private RegistrationTask registrationTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progress = findViewById(R.id.registration_progress);
        content = findViewById(R.id.registration_form);
        editMail = findViewById(R.id.edit_mail);
        editDisplayName = findViewById(R.id.edit_display_name);
        editPassword = findViewById(R.id.edit_password);
        editPasswordProof = findViewById(R.id.edit_proof_password);

        editBecomeDeliverer = findViewById(R.id.become_deliverer);
        final View delivererForm = findViewById(R.id.become_deliverer_form);

        editBecomeDeliverer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    delivererForm.setVisibility(View.VISIBLE);
                } else {
                    delivererForm.setVisibility(View.GONE);
                }
            }
        });

        Button register = findViewById(R.id.button_sign_up);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegistration();
            }
        });

        paymentInformationView = findViewById(R.id.payment_information);
        identificationView = findViewById(R.id.identification);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void attemptRegistration(){
        if (registrationTask != null) {
            return;
        }

        editMail.setError(null);
        editPassword.setError(null);
        editPasswordProof.setError(null);

        String mail = editMail.getText().toString();
        String password = editPassword.getText().toString();
        String passwordProof = editPasswordProof.getText().toString();
        String displayName = editDisplayName.getText().toString();

        boolean becomeDeliverer;
        String identification = null;
        PaymentInformation paymentInformation = null;

        boolean cancel = false;
        View focusView = null;

        if(becomeDeliverer = editBecomeDeliverer.isChecked()){

            paymentInformation = paymentInformationView.getPaymentInformation();
            if(paymentInformation == null){
                return;
            }

            identification = identificationView.getIdentification();
            if(identification == null){
                return;
            }
        }

        if(TextUtils.isEmpty(passwordProof)){
            editPasswordProof.setError(getString(R.string.error_field_required));
            cancel = true;
            focusView = editPasswordProof;
        } else if(!password.equals(passwordProof)){
            editPasswordProof.setError(getString(R.string.error_incorrect_proof));
            cancel = true;
            focusView = editPasswordProof;
        }
        if(TextUtils.isEmpty(password)){
            editPassword.setError(getString(R.string.error_field_required));
            cancel = true;
            focusView = editPassword;
        }

        if(TextUtils.isEmpty(mail)){
            editMail.setError(getString(R.string.error_field_required));
            cancel = true;
            focusView = editMail;
        } else if(!isEmailValid(mail)){
            editMail.setError(getString(R.string.error_invalid_email));
            editMail.requestFocus();
        }

        if(cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            User user;
            if(becomeDeliverer){
                user = new User(mail, password,
                        identificationView.getIdentificationType(), identification);
                user.setPaymentInformation(paymentInformation);
            } else {
                user = new User(mail, password);
            }
            if(!TextUtils.isEmpty(displayName)){
                user.setDisplayname(displayName);
            }
            registrationTask = new RegistrationTask();
            registrationTask.execute(user);
        }
    }


    private class RegistrationTask extends NetworkTask<User, Void, Boolean> {

        @Override
        protected Boolean runInBackground(User... params) throws NetworkException {
            User user = params[0];
            networkClient.signUp(user);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            registrationTask = null;
            showProgress(false);

            if(networkException != null){
                if(networkException.httpError == HttpURLConnection.HTTP_CONFLICT){
                    editMail.setError(getString(R.string.error_email_existing));
                    editMail.requestFocus();
                    return;
                }
                networkException.handleException(RegistrationActivity.this);
                return;
            }

            if(result){
                showResult(R.string.info_registration_sent);
                RegistrationActivity.this.finish();
            } else {
                showResult(R.string.error_registration);
            }
        }
    }
}
