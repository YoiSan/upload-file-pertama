/*
 * Copyright © 2018-present, MNK Group. All rights reserved.
 */
package com.themedimension.ivoryshop.android.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

import com.themedimension.ivoryshop.android.R;
import com.themedimension.ivoryshop.android.manager.NetworkManager;
import com.themedimension.ivoryshop.android.manager.OnNetworkCallback;
import com.themedimension.ivoryshop.android.manager.ResponseExtractor;
import com.themedimension.ivoryshop.android.response.ResponseData;

public class LegalActivity extends AppCompatActivity implements OnNetworkCallback {

    //region GLOBALS

    private static final String TAG = "LegalActivity";

    private NetworkManager manager;

    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_legal_and_terms);

        Button backButton = (Button) findViewById(R.id.legalBackButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(0);
                finish();
            }
        });

        WebView termConditions = (WebView) findViewById(R.id.termConditionsWV);

        /**
         *Insert desired URL for  WebView to load.
         * Terms&Conditions and Privacy Policy
         */
        String url = getResources().getString(R.string.terms_and_conditions_rss_feed_url);
        termConditions.loadUrl(url);
    }

    /**
     * Activity is closed.
     * Return to DrawerActivity.
     */
    @Override
    public void onBackPressed() {
        setResult(0);
        finish();
    }

    @Override
    protected void onDestroy() {
        manager = new NetworkManager(LegalActivity.this).setListener(LegalActivity.this);
        manager.updateCart(LegalActivity.this);
        super.onDestroy();
    }

    @Override
    public void onSuccess(Object data, ResponseExtractor.ResponseType type) {
        ResponseData responseData = (ResponseData) data;
        int code = responseData.getErrorCode();

        switch (code) {
            case 200:
                Log.d(TAG, "onSuccess: Successful request");
                break;

            default:
                Log.e(TAG, responseData.getDescription());
        }
    }

    @Override
    public void onFailed(Object data, ResponseExtractor.ResponseType type) {
        ResponseData responseData = (ResponseData) data;
        Log.e(TAG, responseData.getDescription());
    }
}
