/*
 * Copyright © 2018-present, MNK Group. All rights reserved.
 */
package com.themedimension.ivoryshop.android.activities.registerAndLogin;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.themedimension.ivoryshop.android.R;
import com.themedimension.ivoryshop.android.manager.NetworkManager;
import com.themedimension.ivoryshop.android.manager.OnNetworkCallback;
import com.themedimension.ivoryshop.android.manager.ResponseExtractor;
import com.themedimension.ivoryshop.android.response.ResponseData;
import com.themedimension.ivoryshop.android.utils.EditTextValidation;
import com.themedimension.ivoryshop.android.utils.Utils;

public class PasswordRecoveryActivity extends AppCompatActivity implements OnNetworkCallback {

    //region GLOBALS

    private static final String TAG = "PasswordRecoveryActivity";
    private EditText email;

    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        init();
    }

    /**
     * In this method <field>email</field> is compared with user email address.
     * If both match, then the password is changed and stored
     */
    public void init() {
        final NetworkManager manager = new NetworkManager(this);
        manager.setListener(this);

        TextView passwordRecoveryTitle = (TextView) findViewById(R.id.simple_toolbar_title);
        email = (EditText) findViewById(R.id.password_recovery_email);
        final Button recoverButton = (Button) findViewById(R.id.recover_password_button);
        Button backToLoginButton = (Button) findViewById(R.id.simple_toolbar_back_button);


        passwordRecoveryTitle.setText(getResources().getString(R.string.password_recovery_title));
        recoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (canRecoverPassword()) {
                    if (NetworkManager.DEMO_DATA) {
                        Utils.showSingleButtonAlertWithoutTitle(PasswordRecoveryActivity.this, getResources().getString(R.string.option_not_available_for_demo));
                        return;
                    }
                    manager.setListener(PasswordRecoveryActivity.this).resetPassword(PasswordRecoveryActivity.this, email.getText().toString());
                }

            }
        });
        backToLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        email.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    recoverButton.performClick();
                }
                return false;
            }
        });
    }

    public boolean canRecoverPassword() {
        if (!EditTextValidation.isValidEmail(email.getText().toString().trim()).equals("Success")) {
            Utils.showSingleButtonAlert(this, getResources().getString(R.string.invalid_input), EditTextValidation.isValidEmail(email.getText().toString().trim()));
            email.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
            return false;
        } else {
            email.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black)));
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onSuccess(final Object data, ResponseExtractor.ResponseType type) {
        finish();
    }

    @Override
    public void onFailed(final Object data, ResponseExtractor.ResponseType type) {
        final ResponseData responseData = (ResponseData) data;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.showSingleButtonAlertWithoutTitle(PasswordRecoveryActivity.this, responseData.getDescription());
            }
        });
    }
}
