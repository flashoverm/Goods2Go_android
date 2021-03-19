package org.goods2go.android.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import org.goods2go.android.Goods2GoApplication;
import org.goods2go.android.R;

public class RoleChooseActivity extends AppCompatActivity {

    public static final String IS_DELIVERER_KEY = "User_is_deliverer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_choose);

        Intent intent = getIntent();
        if(!intent.getBooleanExtra(IS_DELIVERER_KEY, false)){
            switchToSenderActivity();
        }

        Button sender = findViewById(R.id.button_sender);
        sender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchToSenderActivity();
            }
        });
        Button deliverer = findViewById(R.id.button_deliverer);
        deliverer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RoleChooseActivity.this, DelivererActivity.class);
                RoleChooseActivity.this.startActivity(intent);
                RoleChooseActivity.this.finish();
            }
        });
    }

    private void switchToSenderActivity(){
        Intent intent = new Intent(this, SenderActivity.class);
        this.startActivity(intent);
        this.finish();
    }


    @Override
    public void onBackPressed() {
        Goods2GoApplication.logout(this);
    }
}
