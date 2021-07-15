/*
 * Copyright © 2018-present, MNK Group. All rights reserved.
 */

package com.themedimension.ivoryshop.android.manager;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.stripe.model.Charge;
import com.themedimension.ivoryshop.android.models.Order;
import com.themedimension.ivoryshop.android.response.ResponseData;
import com.themedimension.ivoryshop.android.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import okhttp3.FormBody;
import okhttp3.RequestBody;

class PaymentDemoHelper {

    //region GLOBALS

    private static final String TAG = "PaymentDemoHelper";

    private static PaymentDemoHelper mInstance = new PaymentDemoHelper();

    //endregion

    private PaymentDemoHelper() {
    }

    public static PaymentDemoHelper getInstance() {
        if (mInstance == null) {
            mInstance = new PaymentDemoHelper();
        }

        return mInstance;
    }

    void makeDemoOrder(Object data, PaymentListener listener, Order.PaymentMethodFlag flag) {
        switch (flag) {
            case PayPal:
                makeDemoPayPalOrder(data, listener);
                break;

            case Stripe:
                makeDemoStripeOrder(data, listener);
                break;

            case Custom:
                makeDemoCustomOrder(data, listener);
                break;

            default:
                makeDemoDeliveryOrder(listener);
                break;
        }
    }

    private void makeDemoDeliveryOrder(PaymentListener listener) {
        RequestBody body = new FormBody.Builder()
                .add("order", Order.getInstance().createJsonInstance(ProductListsManager.getInstance().getCartItems()).toString())
                .add("amount", String.valueOf(Order.getInstance().getTotal()))
                .add("currency", Utils.TRANSACTION_CURRENCY)
                .build();

        Context context = (Context) listener;

        File file = new File(context.getExternalFilesDir(null), "demo_orders.json");
        if (!file.exists()) {
            try {
                file.createNewFile();
                file.setWritable(true);
                file.setReadable(true);

                JSONObject newList = new JSONObject();
                JSONArray array = new JSONArray();
                array.put(body.toString());

                newList.put("orders", array);

                FileWriter writer = new FileWriter(file);
                writer.write(newList.toString());
                writer.flush();
                writer.close();
            } catch (IOException e) {
                Log.e(TAG, "create demo file: " + e.getLocalizedMessage());
            } catch (JSONException e) {
                Log.e(TAG, "parse error: " + e.getLocalizedMessage());
            }

        } else {
            try {
                FileInputStream fis = new FileInputStream(file);
                FileWriter writer = new FileWriter(file);

                BufferedReader reader = new BufferedReader(new InputStreamReader(fis));

                String buffer = "";
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer += line;
                }

                JSONObject savedData = new JSONObject(buffer);
                JSONArray orders = savedData.getJSONArray("orders");
                orders.put(body.toString());
                savedData.put("orders", orders);

                writer.write(savedData.toString());
                writer.flush();
                writer.close();
            } catch (FileNotFoundException e) {
                Log.e(TAG, "open demo file: " + e.getLocalizedMessage());
            } catch (JSONException e) {
                Log.e(TAG, "parse error: " + e.getLocalizedMessage());
            } catch (IOException e) {
                Log.e(TAG, "read from demo file: " + e.getLocalizedMessage());
            }

        }

        ResponseData<Error> response = new ResponseData<Error>();
        response.setErrorCode(200);
        response.setResponse(new Error("Order placed successfully"));
        response.setStatus(0);
        response.setDescription("Success");

        listener.onSuccess(response);
    }

    private void makeDemoCustomOrder(Object data, PaymentListener listener) {
        RequestBody body = (RequestBody) data;

        Context context = (Context) listener;

        File file = new File(context.getExternalFilesDir(null), "demo_orders.json");
        if (!file.exists()) {
            try {
                file.createNewFile();
                file.setWritable(true);
                file.setReadable(true);

                JSONObject newList = new JSONObject();
                JSONArray array = new JSONArray();
                array.put(body.toString());

                newList.put("orders", array);

                FileOutputStream fos = new FileOutputStream(file);
                fos.write(newList.toString().getBytes());
                fos.flush();
                fos.close();
            } catch (IOException e) {
                Log.e(TAG, "create demo file: " + e.getLocalizedMessage());
            } catch (JSONException e) {
                Log.e(TAG, "parse error: " + e.getLocalizedMessage());
            }

        } else {
            try {
                FileInputStream fis = new FileInputStream(file);
                FileOutputStream fos = new FileOutputStream(file);

                BufferedReader reader = new BufferedReader(new InputStreamReader(fis));

                String buffer = "";
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer += line;
                }

                JSONObject savedData = new JSONObject(buffer);
                JSONArray orders = savedData.getJSONArray("orders");
                orders.put(body.toString());
                savedData.put("orders", orders);

                fos.write(savedData.toString().getBytes());
                fos.flush();
                fos.close();
            } catch (JSONException e) {
                Log.e(TAG, "read from file: " + e.getLocalizedMessage());
            } catch (IOException e) {
                Log.e(TAG, "open demo file: " + e.getLocalizedMessage());
            }

        }

        ResponseData<Error> response = new ResponseData<Error>();
        response.setErrorCode(200);
        response.setResponse(new Error("Order placed successfully"));
        response.setStatus(0);
        response.setDescription("Success");

        listener.onSuccess(response);
    }

    private void makeDemoStripeOrder(Object data, PaymentListener listener) {
        Charge charge = (Charge) data;

        RequestBody body = new FormBody.Builder()
                .add("method", String.valueOf(Utils.PAYMENT_STRIPE))
                .add("order", Order.getInstance().createJsonInstance(ProductListsManager.getInstance().getCartItems()).toString())
                .add("source", new Gson().toJson(charge))
                .build();

        Context context = (Context) listener;

        File file = new File(context.getExternalFilesDir(null), "demo_orders.json");
        if (!file.exists()) {
            try {
                file.createNewFile();
                file.setWritable(true);
                file.setReadable(true);

                JSONObject newList = new JSONObject();
                JSONArray array = new JSONArray();
                array.put(body.toString());

                newList.put("orders", array);

                FileOutputStream fos = new FileOutputStream(file);
                fos.write(newList.toString().getBytes());
                fos.flush();
                fos.close();
            } catch (IOException e) {
                Log.e(TAG, "create demo file: " + e.getLocalizedMessage());
            } catch (JSONException e) {
                Log.e(TAG, "Parse error: " + e.getLocalizedMessage());
            }
        } else {
            FileInputStream fis = null;
            FileOutputStream fos = null;
            try {
                fis = new FileInputStream(file);
                fos = new FileOutputStream(file);

                BufferedReader reader = new BufferedReader(new InputStreamReader(fis));

                String buffer = "";
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer += line;
                }

                JSONObject savedData = new JSONObject(buffer);
                JSONArray orders = savedData.getJSONArray("orders");
                orders.put(body.toString());
                savedData.put("orders", orders);

                fos.write(savedData.toString().getBytes());
                fos.flush();
                fos.close();
            } catch (JSONException e) {
                Log.e(TAG, "read from file: " + e.getLocalizedMessage());
            } catch (IOException e) {
                Log.e(TAG, "open demo file: " + e.getLocalizedMessage());
            }
        }

        ResponseData<Error> response = new ResponseData<Error>();
        response.setErrorCode(200);
        response.setResponse(new Error("Order placed successfully"));
        response.setStatus(0);
        response.setDescription("Success");

        listener.onSuccess(response);
    }

    private void makeDemoPayPalOrder(Object data, PaymentListener listener) {
        RequestBody body = (RequestBody) data;

        Context context = (Context) listener;

        File file = new File(context.getExternalFilesDir(null), "demo_orders.json");
        if (!file.exists()) {
            try {
                file.createNewFile();
                file.setWritable(true);
                file.setReadable(true);
            } catch (IOException e) {
                Log.e(TAG, "create demo file: " + e.getLocalizedMessage());
            }

            JSONObject newList = new JSONObject();
            JSONArray array = new JSONArray();
            array.put(body.toString());

            try {
                newList.put("orders", array);
            } catch (JSONException e) {
                Log.e(TAG, "put in json: " + e.getLocalizedMessage());
            }

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                fos.write(newList.toString().getBytes());
                fos.flush();
                fos.close();
            } catch (IOException e) {
                Log.e(TAG, "open demo file: " + e.getLocalizedMessage());
            }
        } else {
            FileInputStream fis = null;
            FileOutputStream fos = null;
            try {
                fis = new FileInputStream(file);
                fos = new FileOutputStream(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(fis));

                String buffer = "";
                String line;
                try {
                    while ((line = reader.readLine()) != null) {
                        buffer += line;
                    }
                } catch (IOException e) {
                    Log.e(TAG, "read from demo file: " + e.getLocalizedMessage());
                }

                JSONObject savedData;
                try {
                    savedData = new JSONObject(buffer);
                    JSONArray orders = savedData.getJSONArray("orders");
                    orders.put(body.toString());
                    savedData.put("orders", orders);
                    fos.write(savedData.toString().getBytes());
                } catch (JSONException e) {
                    Log.e(TAG, "parse error: " + e.getLocalizedMessage());
                }

                fos.close();
                fos.flush();
            } catch (FileNotFoundException e) {
                Log.e(TAG, "open demo file: " + e.getLocalizedMessage());
            } catch (IOException e) {
                Log.e(TAG, "write to demo file: " + e.getLocalizedMessage());
            }
        }

        ResponseData<Error> response = new ResponseData<Error>();
        response.setErrorCode(200);
        response.setResponse(new Error("Order placed successfully"));
        response.setStatus(0);
        response.setDescription("Success");

        listener.onSuccess(response);
    }
}
