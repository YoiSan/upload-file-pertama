/*
 * Copyright © 2018-present, MNK Group. All rights reserved.
 */

package com.themedimension.ivoryshop.android.AsyncCardCharge;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.stripe.android.Stripe;
import com.stripe.android.model.Card;
import com.stripe.android.model.Source;
import com.stripe.android.model.SourceParams;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Charge;
import com.themedimension.ivoryshop.android.AsyncCardCharge.ServerCommunication.CreateSourceSuccessCallback;
import com.themedimension.ivoryshop.android.AsyncCardCharge.ServerCommunication.ServerRequest;
import com.themedimension.ivoryshop.android.AsyncCardCharge.ServerCommunication.ServiceHandler;
import com.themedimension.ivoryshop.android.utils.Utils;
import com.themedimension.ivoryshop.android.manager.PaymentManager;
import com.themedimension.ivoryshop.android.models.Order;

import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * Asynchronous task that creates a new Charge object and sends it to the app's server
 * for processing, following the documentation about Stripe integration.
 *
 * @see Stripe or <a href="https://stripe.com/docs">Stripe Documentation</a>
 */
public class AsyncCharge extends AsyncTask<Card, Void, Charge> implements CreateSourceSuccessCallback, ServiceHandler {

    //region GLOBALS

    private Context mContext;
    private Card mCard = null;
    private Charge mCharge = null;
    private ServerRequest mServerRequest;

    //endregion

    public AsyncCharge(Context context, Card cardObject, ServerRequest callback) {
        mContext = context;
        mCard = cardObject;
        mServerRequest = callback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //  Testing the validity of the card object.
        if (mCard == null) {
            this.cancel(false);
            Log.e(TAG, "onPreExecute: mCard is null.");
        }
    }

    @Override
    protected Charge doInBackground(Card... params) {
        Stripe stripe = new Stripe(mContext, Utils.STRIPE_PUBLISHABLE_KEY);
        SourceParams sourceParams = SourceParams.createCardParams(mCard);

        try {
            //  Server request preparation.
            Source source = stripe.createSourceSynchronous(sourceParams);

            Map<String, Object> chargeParams = new HashMap<>();
            chargeParams.put("amount", Order.getInstance().getTotal());   //  Amount needed to finish transaction.
            chargeParams.put("currency", Utils.TRANSACTION_CURRENCY);    //  Currency used for transaction.
            chargeParams.put("description", "Order charge");    //  Short description about the transaction.
            chargeParams.put("source", source.getId());     //  Payment method used.

            mCharge = Charge.create(chargeParams);  //  Resulting charge to send to server to be processed.
        } catch (com.stripe.android.exception.AuthenticationException |
                com.stripe.android.exception.InvalidRequestException |
                com.stripe.android.exception.CardException |
                com.stripe.android.exception.APIConnectionException |
                APIConnectionException |
                com.stripe.android.exception.APIException |
                AuthenticationException |
                InvalidRequestException |
                CardException |
                APIException e) {

            e.printStackTrace();
            Log.e(TAG, "doInBackground: " + e.getLocalizedMessage());
        }

        //  Return the Charge object back to app for further work.
        return mCharge;
    }

    @Override
    protected void onPostExecute(final Charge charge) {
        if (mServerRequest != null) {
            //  Setting the final Charge object before sending to server.
            new Thread(new Runnable() {
                @Override
                public void run() {
                    successCallbackReceived(charge);
                }
            }).run();

            makeServiceCall(Utils.BASE_URL, POST);
        }
    }

    @Override
    public void successCallbackReceived(Charge c) {
        mCharge = c;
    }

    /**
     * Method to make a call to the server in order to start processing the
     * transaction required to finish order.
     *
     * @param url    url connection to the server
     * @param method code to mark that the app wants to send data to server
     */
    @Override
    public void makeServiceCall(String url, int method) {
        PaymentManager.getInstance().makePayment(mCharge, Order.PaymentMethodFlag.Stripe);
    }
}
