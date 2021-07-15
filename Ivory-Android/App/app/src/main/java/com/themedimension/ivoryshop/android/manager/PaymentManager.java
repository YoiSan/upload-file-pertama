/*
 * Copyright © 2018-present, MNK Group. All rights reserved.
 */

package com.themedimension.ivoryshop.android.manager;

import android.util.Log;

import com.google.gson.Gson;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.stripe.model.Charge;
import com.themedimension.ivoryshop.android.models.Order;
import com.themedimension.ivoryshop.android.utils.Utils;

import org.json.JSONException;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PaymentManager {

    //region GLOBALS

    private static final String TAG = "PaymentManager";

    private static PaymentManager mInstance = new PaymentManager();
    private OkHttpClient mClient = new OkHttpClient();
    private PaymentListener mListener;

    //endregion

    private PaymentManager() {
    }

    public static PaymentManager getInstance() {
        if (mInstance == null) {
            mInstance = new PaymentManager();
        }

        return mInstance;
    }

    public void setListener(PaymentListener listener) {
        mListener = listener;
    }

    public void makePayment(Object data, Order.PaymentMethodFlag flag) {
        if (!NetworkManager.DEMO_DATA) {
            switch (flag) {
                case PayPal:
                    RequestBody body = (RequestBody) data;
                    mClient.newCall(
                            new Request.Builder().url(Utils.PAY_PAL_PAYMENT_URL)
                                    .post(body)
                                    .build()
                    ).enqueue(
                            new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    mListener.onFailed(new Error(e.getLocalizedMessage()));
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    int code = response.code();
                                    switch (code) {
                                        case 200:
                                            mListener.onSuccess(new Error("Order placed successfully"));
                                            break;

                                        default:
                                            mListener.onFailed(new Error(String.format(Locale.US, "Error %d: Retry", code)));
                                    }
                                }
                            }
                    );
                    break;

                case Stripe:
                    makeStripePayment(data);
                    break;

                case Custom:
                    makeCustomPayment(data);
                    break;

                default:
                    try {
                        makeDeliveryPayment();
                    } catch (JSONException e) {
                        Log.e(TAG, "Error creating request: " + e.getLocalizedMessage());
                    }
                    break;
            }
        } else {
            PaymentDemoHelper.getInstance().makeDemoOrder(data, mListener, flag);
        }
    }

    private void makeCustomPayment(Object data) {
        RequestBody requestBody = (RequestBody) data;

        mClient.newCall(
                new Request.Builder()
                        .url(Utils.CUSTOM_PAYMENT_URL)
                        .post(requestBody)
                        .build()
        ).enqueue(
                new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        mListener.onFailed(new Error(e.getLocalizedMessage()));
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        int code = response.code();
                        switch (code) {
                            case 200:
                                mListener.onSuccess(new Error("Order placed successfully"));
                                break;

                            default:
                                mListener.onFailed(new Error(String.format(Locale.US, "Error %d: Retry", code)));
                        }
                    }
                }
        );
    }

    private void makeStripePayment(Object data) {
        Charge charge = (Charge) data;

        RequestBody body = new FormBody.Builder()
                .add("method", String.valueOf(Utils.PAYMENT_STRIPE))
                .add("order", Order.getInstance().createJsonInstance(ProductListsManager.getInstance().getCartItems()).toString())
                .add("source", new Gson().toJson(charge))
                .build();

        mClient.newCall(
                new Request.Builder()
                        .url(Utils.STRIPE_PAYMENT_URL)
                        .post(body)
                        .build()
        ).enqueue(
                new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        mListener.onFailed(new Error(e.getLocalizedMessage()));
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        int code = response.code();
                        switch (code) {
                            case 200:
                                mListener.onSuccess(new Error("Order placed successfully"));
                                break;

                            default:
                                mListener.onFailed(new Error(String.format(Locale.US, "Error %d: Retry", code)));
                        }
                    }
                }
        );
    }

    public void onConfirmationReceived(PaymentConfirmation confirmation) {
        RequestBody requestBody = new FormBody.Builder()
                .add("method", String.valueOf(Utils.PAYMENT_PAYPAL))
                .add("amount", confirmation.getPayment().getAmountAsLocalizedString())
                .add("confirmation", new Gson().toJson(confirmation))
                .add("payment", new Gson().toJson(confirmation.getPayment()))
                .build();

        makePayment(requestBody, Order.PaymentMethodFlag.PayPal);
    }

    private void makeDeliveryPayment() throws JSONException {
        RequestBody body = new FormBody.Builder()
                .add("order", Order.getInstance().createJsonInstance(ProductListsManager.getInstance().getCartItems()).toString())
                .add("amount", String.valueOf(Order.getInstance().getTotal()))
                .add("currency", Utils.TRANSACTION_CURRENCY)
                .build();

        mClient.newCall(
                new Request.Builder().url(Utils.DELIVERY_PAYMENT_URL)
                        .post(body)
                        .build()
        ).enqueue(
                new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        mListener.onFailed(new Error(e.getLocalizedMessage()));
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        int code = response.code();
                        switch (code) {
                            case 200:
                                mListener.onSuccess(new Error("Order placed successfully"));
                                break;

                            default:
                                mListener.onFailed(new Error(String.format(Locale.US, "Error %d: Retry", code)));
                        }
                    }
                }
        );
    }
}
