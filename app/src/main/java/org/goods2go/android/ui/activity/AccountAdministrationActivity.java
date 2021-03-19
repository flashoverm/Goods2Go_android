package org.goods2go.android.ui.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.goods2go.models.Address;
import com.goods2go.models.PaymentInformation;
import com.goods2go.models.User;
import com.goods2go.models.enums.Role;

import org.goods2go.android.Goods2GoApplication;
import org.goods2go.android.R;
import org.goods2go.android.network.NetworkException;
import org.goods2go.android.network.NetworkTask;
import org.goods2go.android.ui.dialog.AddressDialog;
import org.goods2go.android.ui.view.PaymentInformationView;

public class AccountAdministrationActivity extends NetworkActivity
        implements AddressDialog.AddressSetListener{

    //private EditText editOldPassword;
    private EditText editNewPassword;
    private EditText editPasswordProof;
    private EditText editDisplayName;
    private EditText editDefaultAddress;

    private PaymentInformationView paymentInformationView;

    private Address currentAddress;

    private PasswordChangeTask passwordChangeTask = null;
    private UserDataChangeTask userDataChangeTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_administration);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initViewElements();
    }

    private void initViewElements(){
        progress = findViewById(R.id.account_admin_progress);
        content = findViewById(R.id.account_admin_form);
        //editOldPassword = findViewById(R.id.edit_old_password);
        editNewPassword = findViewById(R.id.edit_new_password);
        editPasswordProof = findViewById(R.id.edit_proof_password);
        editDisplayName = findViewById(R.id.edit_display_name);
        editDefaultAddress = findViewById(R.id.edit_default_address);

        editDefaultAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddressDialog dialog = AddressDialog.newInstance(
                        currentAddress, R.string.text_default_address);
                dialog.show(getFragmentManager(), "AddressDialog");
            }
        });

        Button changePassword = findViewById(R.id.button_save_password);
        Button changeUserData = findViewById(R.id.button_save_user_data);

        changeUserData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptUserDataChange();
            }
        });

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptPasswordChange();
            }
        });

        User current = Goods2GoApplication.getCurrentUser(this);

        TextView email = findViewById(R.id.email);
        email.setText(current.getEmail());

        currentAddress = current.getDefaultaddress();
        if(currentAddress != null){
            displayAddress(currentAddress);
        }
        String currentDisplayName = current.getDisplayname();
        if(currentDisplayName != null && !currentDisplayName.equals("")){
            editDisplayName.setText(currentDisplayName);
        }

        initPaymentLayout(current);
    }

    private void initPaymentLayout(User current){
        paymentInformationView = findViewById(R.id.payment_information);
        if(current.getRole().equals(Role.DELIVERER)){
            paymentInformationView.setVisibility(View.VISIBLE);
            paymentInformationView.setPaymentInformation(current.getPaymentInformation());
        } else {
            paymentInformationView.setVisibility(View.GONE);
        }
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

    private void attemptPasswordChange(){
        if (passwordChangeTask != null) {
            return;
        }

        //editOldPassword.setError(null);
        editNewPassword.setError(null);
        editPasswordProof.setError(null);

        //String oldPassword = editOldPassword.getText().toString();
        String newPassword = editNewPassword.getText().toString();
        String passwordProof = editPasswordProof.getText().toString();
        boolean cancel = false;
        View focusView = null;

        if(TextUtils.isEmpty(passwordProof)){
            editPasswordProof.setError(getString(R.string.error_field_required));
            cancel = true;
            focusView = editPasswordProof;
        } else if(!newPassword.equals(passwordProof)){
            editPasswordProof.setError(getString(R.string.error_incorrect_proof));
            cancel = true;
            focusView = editPasswordProof;
        }
        if(TextUtils.isEmpty(newPassword)){
            editNewPassword.setError(getString(R.string.error_field_required));
            cancel = true;
            focusView = editNewPassword;
        }
        /*
        if(TextUtils.isEmpty(oldPassword)){
            editOldPassword.setError(getString(R.string.error_field_required));
            cancel = true;
            focusView = editOldPassword;
        }
        */

        if(cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            User user = Goods2GoApplication.getCurrentUser(this);
            user.setPassword(newPassword);
            passwordChangeTask = new PasswordChangeTask();
            passwordChangeTask.execute(user);
        }
    }

    private void attemptUserDataChange(){
        if (userDataChangeTask != null) {
            return;
        }
        boolean cancel = false;
        View focusView = null;

        editDisplayName.setError(null);

        String displayName = editDisplayName.getText().toString();
        PaymentInformation paymentInformation = null;

        if(paymentInformationView.getVisibility() == View.VISIBLE){
            paymentInformation = paymentInformationView.getPaymentInformation();
            if(paymentInformation == null){
                return;
            }
        }

        if(TextUtils.isEmpty(displayName)){
            editDisplayName.setError(getString(R.string.error_field_required));
            focusView = editDisplayName;
            cancel = true;
        }

        if(cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);

            User update = Goods2GoApplication.getCurrentUser(this);
            update.setDisplayname(displayName);
            update.setDefaultaddress(currentAddress);
            update.setPaymentInformation(paymentInformation);

            userDataChangeTask = new UserDataChangeTask();
            userDataChangeTask.execute(update);
        }
    }

    @Override
    public void onAddressSet(Address address, int fieldNumber) {
        currentAddress = address;
        displayAddress(currentAddress);
    }

    private void displayAddress(Address address){
        editDefaultAddress.setText(address.getAddressAsString());
    }

    private class PasswordChangeTask extends NetworkTask<User, Void, User> {

        @Override
        protected User runInBackground(User... params) throws NetworkException {
            User user = params[0];
            //String oldPassword = strings[1];
            return networkClient.changePassword(user);
        }

        @Override
        protected void onPostExecute(User result) {
            showProgress(false);
            passwordChangeTask = null;

            if(networkException != null){
                networkException.handleException(AccountAdministrationActivity.this);
                return;
            }
            if(result != null){
                Goods2GoApplication.setCurrentUser(AccountAdministrationActivity.this, result);
                showResult(R.string.info_update_ok);
            } else {
                showResult(R.string.error_update);
            }
        }
    }

    private class UserDataChangeTask extends NetworkTask<User, Void, User> {

        @Override
        protected User runInBackground(User... params) throws NetworkException {
            User user = params[0];
            return networkClient.changeUserData(user);
        }

        @Override
        protected void onPostExecute(User result) {
            showProgress(false);
            userDataChangeTask = null;

            if(networkException != null){
                networkException.handleException(AccountAdministrationActivity.this);
                return;
            }
            if (result != null) {
                Goods2GoApplication.setCurrentUser(AccountAdministrationActivity.this, result);
                showResult(R.string.info_update_ok);
            } else {
                showResult(R.string.error_update);
            }
        }
    }
}
