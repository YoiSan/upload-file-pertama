/*
 * Copyright © 2018-present, MNK Group. All rights reserved.
 */

package com.themedimension.ivoryshop.android.activities.registerAndLogin;

import android.content.Intent;
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
import android.widget.Toast;

import com.themedimension.ivoryshop.android.R;
import com.themedimension.ivoryshop.android.utils.EditTextValidation;
import com.themedimension.ivoryshop.android.utils.Utils;
import com.themedimension.ivoryshop.android.activities.fragmentActivities.DrawerActivity;
import com.themedimension.ivoryshop.android.manager.NetworkManager;
import com.themedimension.ivoryshop.android.manager.OnNetworkCallback;
import com.themedimension.ivoryshop.android.manager.ProductListsManager;
import com.themedimension.ivoryshop.android.manager.ResponseExtractor;
import com.themedimension.ivoryshop.android.models.User;
import com.themedimension.ivoryshop.android.response.ResponseData;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, OnNetworkCallback {

    //region GLOBALS

    private static final String TAG = "LoginActivity";

    private EditText emailEditText;
    private EditText passwordEditText;
    private  NetworkManager mNetworkManager;

    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init();
    }

    //  Initialization for text fields and buttons
    public void init() {
        emailEditText = (EditText) findViewById(R.id.emailEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
        final Button loginButton = (Button) findViewById(R.id.login_button);
        TextView forgotPass = (TextView) findViewById(R.id.forgot_password);
        Button backButton = (Button) findViewById(R.id.simple_toolbar_back_button);


        String email = getIntent().getStringExtra("email");
        String pass = getIntent().getStringExtra("password");

        if (email != null && pass != null) {
            emailEditText.setText(email);
            passwordEditText.setText(pass);
        }

        emailEditText.setOnClickListener(this);
        passwordEditText.setOnClickListener(this);
        loginButton.setOnClickListener(this);
        forgotPass.setOnClickListener(this);
        backButton.setOnClickListener(this);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE){
                    loginButton.performClick();
                }
                return false;
            }
        });

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login_button:
                if (canLogin()){
                    String email = emailEditText.getText().toString().trim();
//                    String passwordMd5 = Utils.md5(passwordEditText.getText().toString());

                    mNetworkManager = new NetworkManager(this);
                    mNetworkManager.setListener(this);
                    mNetworkManager.loginAccount(this, email, passwordEditText.getText().toString().trim());
                }
                break;

            case R.id.forgot_password:
                Intent intent = new Intent(LoginActivity.this, PasswordRecoveryActivity.class);
                startActivityForResult(intent, 1);
                break;

            case R.id.simple_toolbar_back_button:
                finish();
                break;
        }
    }

    public boolean canLogin(){

        if (emailEditText.getText().toString().isEmpty() && passwordEditText.getText().toString().isEmpty()){
            emailEditText.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
            passwordEditText.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
            Utils.showSingleButtonAlert(this, getResources().getString(R.string.invalid_input), getResources().getString(R.string.please_complete_all_fields));
            return false;
        }

        if (!EditTextValidation.isValidEmail(emailEditText.getText().toString().trim()).equals("Success")){
            Utils.showSingleButtonAlert(this, getResources().getString(R.string.invalid_input), EditTextValidation.isValidEmail(emailEditText.getText().toString().trim()));
            emailEditText.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
            return false;
        } else {
            emailEditText.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black)));
        }

        if (!EditTextValidation.isPasswordValid(passwordEditText.getText().toString().trim()).equals("Success")){
            Utils.showSingleButtonAlert(this, getResources().getString(R.string.invalid_input), EditTextValidation.isPasswordValid(passwordEditText.getText().toString().trim()));
            passwordEditText.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
            return false;
        } else {
            passwordEditText.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.black)));
        }

        return true;
    }

    /**
     * Method for handling the onClick/pressing of phone's BACK button.
     * When the button is pressed close the drawer first and if the drawer
     * is closed close the activity.
     */
    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onSuccess(Object data, ResponseExtractor.ResponseType type) {
        ResponseData responseData = (ResponseData) data;
        int code = responseData.getErrorCode();
        final String msg = responseData.getDescription();
        switch (code) {
            case ResponseData.LOGIN:
                if (mNetworkManager != null){
                    mNetworkManager.getUser(User.getInstance().getToken());
                }

                break;
            case ResponseData.GET_USER:
//                if (mNetworkManager != null){
//                    mNetworkManager.getCart(this);
//                }
                User.save(LoginActivity.this);
                Intent intent = new Intent(LoginActivity.this, DrawerActivity.class);
                startActivity(intent);
                finish();
                break;
            case 200:
                User.save(LoginActivity.this);
                Intent startDrawerActivity = new Intent(LoginActivity.this, DrawerActivity.class);
                startActivity(startDrawerActivity);
                finish();
                break;

            default:
                if (type == ResponseExtractor.ResponseType.EXISTING_USER) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
        }
    }

    @Override
    public void onFailed(final Object data, ResponseExtractor.ResponseType type) {
        final ResponseData responseData = (ResponseData) data;
        final String msg = responseData.getDescription();

        Log.e(TAG, responseData.getDescription());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.showSingleButtonAlertWithoutTitle(LoginActivity.this, msg);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        User.getInstance().logout(LoginActivity.this);
        ProductListsManager.getInstance().resetInstance();
    }
}
