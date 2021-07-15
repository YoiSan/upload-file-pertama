
/*
 * Copyright © 2018-present, MNK Group. All rights reserved.
 */
package com.themedimension.ivoryshop.android.activities.registerAndLogin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.themedimension.ivoryshop.android.R;
import com.themedimension.ivoryshop.android.activities.fragmentActivities.DrawerActivity;
import com.themedimension.ivoryshop.android.manager.NetworkManager;
import com.themedimension.ivoryshop.android.manager.OnNetworkCallback;
import com.themedimension.ivoryshop.android.manager.ProductListsManager;
import com.themedimension.ivoryshop.android.manager.ResponseExtractor;
import com.themedimension.ivoryshop.android.models.User;
import com.themedimension.ivoryshop.android.response.ResponseData;

import org.json.JSONArray;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener, OnNetworkCallback {
    private static final String TAG = "SignInActivity";

    private NetworkManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        init();

    }

    //Initialization for login button and register button.
    public void init() {
        Button loginButton = (Button) findViewById(R.id.login_button);
        Button registerButton = (Button) findViewById(R.id.buttonRegister);
        Button skipButton = (Button) findViewById(R.id.skipButton);
        loginButton.setOnClickListener(this);
        registerButton.setOnClickListener(this);
        skipButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.skipButton:
                //Allow the  user to browse the app without registering and/or logging-in.
                // This wil continue until the user wants to complete an order - DeliveryActivity.
//                User.getInstance().setAnon(true);
                Intent startDrawerActivity = new Intent(SignInActivity.this, DrawerActivity.class);
                startActivity(startDrawerActivity);
                break;
            case R.id.login_button:
                Intent intent = new Intent(SignInActivity.this, LoginActivity.class);
                startActivity(intent);      // This starts a new activity. When the login button is pressed, the application will take you to sign in screen.
                break;

            case R.id.buttonRegister:
                Intent intent2 = new Intent(SignInActivity.this, RegisterActivity.class);
                startActivity(intent2);   // This starts a new activity. When the register button is pressed, the application will take you to registration screen.

        }
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        User.getInstance().logout(SignInActivity.this);
//        ProductListsManager.getInstance().setWishlist(new JSONArray());
//        ProductListsManager.getInstance().setCart(new JSONArray());
//    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onSuccess(Object data, ResponseExtractor.ResponseType type) {
        ResponseData responseData = (ResponseData) data;
        final int code = responseData.getErrorCode();

        switch (code) {
            case 200:
                if (type == ResponseExtractor.ResponseType.CARTLIST) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            manager.getWishList(SignInActivity.this);
                        }
                    });
                } else {
                    Intent goToProductListScreen = new Intent(SignInActivity.this, DrawerActivity.class);
                    startActivity(goToProductListScreen);
                }
                break;

            default:
                Log.e(TAG, responseData.getDescription());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SignInActivity.this, "An error occurred", Toast.LENGTH_LONG).show();
                    }
                });
        }
    }

    @Override
    public void onFailed(Object data, ResponseExtractor.ResponseType type) {
        final ResponseData responseData = (ResponseData) data;

        Log.e(TAG, responseData.getDescription());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SignInActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
