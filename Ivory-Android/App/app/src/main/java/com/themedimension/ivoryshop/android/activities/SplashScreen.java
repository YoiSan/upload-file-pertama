/*
 * Copyright © 2018-present, MNK Group. All rights reserved.
 */
package com.themedimension.ivoryshop.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.themedimension.ivoryshop.android.R;
import com.themedimension.ivoryshop.android.activities.fragmentActivities.DrawerActivity;
import com.themedimension.ivoryshop.android.activities.registerAndLogin.RegisterActivity;
import com.themedimension.ivoryshop.android.activities.registerAndLogin.SignInActivity;
import com.themedimension.ivoryshop.android.manager.NetworkManager;
import com.themedimension.ivoryshop.android.manager.OnNetworkCallback;
import com.themedimension.ivoryshop.android.manager.ResponseExtractor;
import com.themedimension.ivoryshop.android.models.User;
import com.themedimension.ivoryshop.android.response.ResponseData;
import com.themedimension.ivoryshop.android.utils.Utils;

public class SplashScreen extends AppCompatActivity implements OnNetworkCallback {

    //region GLOBALS

    private static final String TAG = "SplashScreen";
    // The duration in milliseconds of the splash screen to be shown. 1 second = 1000 milliseconds
    private static final int SPLASH_SCREEN_SLEEP_TIME = 1000;
    private NetworkManager mNetworkManager;
    private boolean firstTime = true;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

//        User.load(this);
        Thread thread = new Thread() {
            /***
             * The sleep time for the SplashScreen may be changed.
             * SignInActivity is opened when SplashScreen finishes.
             */
            @Override
            public void run() {
                mNetworkManager = new NetworkManager(SplashScreen.this);
                mNetworkManager.setListener(SplashScreen.this);
                mNetworkManager.getProductList(SplashScreen.this);
            }
        };
        thread.run();
    }

    /**
     * SplashScreen is closed when the user presses phone's BACK button.
     */
    @Override
    public void onBackPressed() {
        finish();
    }

    /**
     * On successful retrieval of the data source, populate the product list with
     * returned data.
     *
     * @param data returned data from the server
     * @param type type of the returned data
     * @see com.themedimension.ivoryshop.android.manager.ResponseExtractor.ResponseType
     */
    @Override
    public void onSuccess(Object data, ResponseExtractor.ResponseType type) {
        final ResponseData responseData = (ResponseData) data;
        switch (responseData.getErrorCode()) {
            case ResponseData.ERROR_CODE_SUCCESS:
                if (User.getInstance().userCanLogin()) {
                    mNetworkManager.getCart(SplashScreen.this);
                } else {
                    Intent goToMainScreen = new Intent(SplashScreen.this, SignInActivity.class);
                    startActivity(goToMainScreen);
                    finish();
                }
                break;
            default:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, responseData.getDescription());
                        Toast.makeText(SplashScreen.this, responseData.getDescription(), Toast.LENGTH_SHORT).show();
                    }
                });
                break;
        }
    }

    /**
     * Define behaviour on failed data retrieval.
     *
     * @param data returned data
     * @param type returned data type
     */
    @Override
    public void onFailed(Object data, ResponseExtractor.ResponseType type) {
        final ResponseData responseData = (ResponseData) data;
        Log.d(TAG, responseData.getDescription());
        switch (type) {
            case PRODUCT_LIST:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showSingleButtonAlertWithoutTitle(SplashScreen.this, responseData.getDescription());
                    }
                });
                break;
        }
    }
}