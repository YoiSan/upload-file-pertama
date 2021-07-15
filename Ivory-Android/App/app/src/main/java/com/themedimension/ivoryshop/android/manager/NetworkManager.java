/*
  * Copyright © 2018-present, MNK Group. All rights reserved.
 */

package com.themedimension.ivoryshop.android.manager;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import com.themedimension.ivoryshop.android.R;
import com.themedimension.ivoryshop.android.utils.NetworkUtils;
import com.themedimension.ivoryshop.android.utils.Utils;
import com.themedimension.ivoryshop.android.models.Product;
import com.themedimension.ivoryshop.android.models.User;
import com.themedimension.ivoryshop.android.response.ResponseData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.mikepenz.iconics.Iconics.TAG;
import static com.themedimension.ivoryshop.android.manager.NetworkDemoHelper.getDemoData;

/**
 * Class to implement the communication between the app and the server.
 */
public class NetworkManager {

    //region GLOBALS

    /*
    * In order for the app to correctly communicate with the server,
    * <field> DEMO_DATA </field> should become false.
    *
    * The class will also need to be provided with a correct switch case.
    */
    public static final boolean DEMO_DATA = true;
    private static final int DEMO_WAIT = 2500;

    private OkHttpClient client;
    private OnNetworkCallback onNetworkCallback;
    private Activity activity;

    //endregion

    public NetworkManager(Activity activity) {
        client = new OkHttpClient();
        this.activity = activity;
    }

    public NetworkManager setListener(OnNetworkCallback onNetworkCallback) {
        this.onNetworkCallback = onNetworkCallback;
        return this;
    }

    /*
    * In order to implement the app-server communication,
    * the <code> else {...} </code> should be provided with the requested behaviour.
    */
    public synchronized void getProductList(Context context) {
        if (DEMO_DATA) {
            final ResponseData productResponse = (ResponseData)
                    getDemoData(
                            ResponseExtractor.ResponseType.PRODUCT_LIST,
                            context,
                            null,
                            null,
                            null
                    );

            JSONObject data = (JSONObject) productResponse.getResponse();
            try {
                JSONArray array = data.getJSONArray("productList");
                ProductListsManager.getInstance().setShopProducts(array);
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing response: " + e.getLocalizedMessage());
            }

            final ResponseData responseData = new ResponseData();
            responseData.setDescription("Success");
            responseData.setStatus(0);
            responseData.setErrorCode(200);

            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onNetworkCallback.onSuccess(responseData, ResponseExtractor.ResponseType.DEMO);
                        }
                    });
                }
            });
        } else {
            final Request request = new Request.Builder()
                    .url(NetworkUtils.URL_PRODUCTS)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    ResponseData responseData = new ResponseData();
                    responseData.setDescription(activity.getResources().getString(R.string.error_sending_request));
                    responseData.setErrorCode(402);
                    responseData.setStatus(0);
                    onNetworkCallback.onFailed(responseData, ResponseExtractor.ResponseType.PRODUCT_LIST);
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                    ResponseData responseData = new ResponseData();
                    Log.d(TAG, String.valueOf(response.code()));
                    switch (response.code()) {
                        case 200:
                            // case "Success"
                            try {
                                JSONObject data = new JSONObject(response.body().string());
                                JSONArray array = data.getJSONArray("productList");
                                ProductListsManager.getInstance().setShopProducts(array);

                                responseData.setDescription("Success");
                                responseData.setErrorCode(response.code());
                                responseData.setStatus(0);
                                if (onNetworkCallback != null) {
                                    onNetworkCallback.onSuccess(responseData, ResponseExtractor.ResponseType.PRODUCT_LIST);
                                }
                                break;
                            } catch (JSONException e) {
                                Log.e(TAG, "Error parsing response: " + e.getLocalizedMessage());
                                responseData.setDescription("Failure");
                                responseData.setErrorCode(response.code());
                                responseData.setStatus(0);
                                if (onNetworkCallback != null) {
                                    onNetworkCallback.onFailed(responseData, ResponseExtractor.ResponseType.PRODUCT_LIST);
                                }
                            }
                            break;

                        case 403:
                            // Case "Connection blocked by firewall"
                            responseData.setDescription(activity.getResources().getString(R.string.could_not_load_data));
                            responseData.setStatus(0);
                            responseData.setErrorCode(response.code());
                            if (onNetworkCallback != null) {
                                onNetworkCallback.onFailed(responseData, ResponseExtractor.ResponseType.PRODUCT_LIST);
                            }
                            break;
                        default:
                            // other cases
                            responseData.setDescription("Failure");
                            responseData.setStatus(0);
                            responseData.setErrorCode(response.code());
                            if (onNetworkCallback != null) {
                                onNetworkCallback.onFailed(responseData, ResponseExtractor.ResponseType.PRODUCT_LIST);
                            }
                            break;
                    }
                }
            });
        }
    }

    public synchronized void createAccount(Context context, final JSONObject user) {
        if (DEMO_DATA) {
            final ResponseData userResponse = (ResponseData)
                    getDemoData(ResponseExtractor.ResponseType.NEW_USER, context, null, null, user);
            switch (userResponse.getErrorCode()) {
                case 200:
                    onNetworkCallback.onSuccess(userResponse, ResponseExtractor.ResponseType.NEW_USER);
                    break;

                default:
                    onNetworkCallback.onFailed(userResponse, ResponseExtractor.ResponseType.NEW_USER);
            }
        } else {
            RequestBody body = null;
            JSONObject address = null;
            try {
                address = user.getJSONObject("address");
                body = new FormBody.Builder()
                        .add("email", user.getString("email"))
                        .add("password", user.getString("password"))
                        .add("c_password", user.getString("password"))
                        .add("firstName", user.getString("firstName"))
                        .add("lastName", user.getString("lastName"))
                        .add("phone", user.getString("phone"))
                        .add("city", address.getString("city"))
                        .add("country", address.getString("country"))
                        .add("district", address.getString("district"))
                        .add("number", address.getString("number"))
                        .add("postalCode", address.getString("postalCode"))
                        .add("street", address.getString("street"))
                        .build();
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "Error creating body from JSON: " + e.getLocalizedMessage());
            }

            final Request request = new Request.Builder()
                    .post(body)
                    .url(NetworkUtils.URL_REGISTER)
                    .build();

            Handler h = new Handler();
            h.post(new Runnable() {
                @Override
                public void run() {
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            ResponseData responseData = new ResponseData();
                            responseData.setDescription(activity.getResources().getString(R.string.error_sending_request));
                            responseData.setErrorCode(402);
                            responseData.setStatus(0);
                            onNetworkCallback.onFailed(responseData, ResponseExtractor.ResponseType.NEW_USER);
                        }

                        @Override
                        public void onResponse(Call call, okhttp3.Response response) throws IOException {
                            if (response != null) {
                                int responseCode = response.code();
                                ResponseData responseData = new ResponseData();
                                switch (responseCode) {
                                    case 401:
                                        // Case "Email already in use for another account"
                                        try {
                                            JSONObject jsonResponseBody = new JSONObject(response.body().string());
                                            if (jsonResponseBody.has("error")) {
                                                JSONObject errors = jsonResponseBody.getJSONObject("error");

                                                if (errors.has("email")) {
                                                    responseData.setDescription(Utils.formatError(errors.get("email").toString()));
                                                }

                                            }
                                        } catch (JSONException e) {
                                            responseData.setDescription(e.getMessage());
                                            e.printStackTrace();
                                        }
                                        responseData.setErrorCode(responseCode);
                                        responseData.setStatus(0);
                                        if (onNetworkCallback != null)
                                            onNetworkCallback.onFailed(responseData, ResponseExtractor.ResponseType.NEW_USER);
                                        break;
                                    case 200:
                                        // Case "Success"
                                        try {
                                            JSONObject jsonResponseBody = new JSONObject(response.body().string());
                                            if (jsonResponseBody.has("success")) {
                                                jsonResponseBody = jsonResponseBody.getJSONObject("success");
                                                String password = "";
                                                if (jsonResponseBody.has("password")) {
                                                    password = jsonResponseBody.getString("password");
                                                    JSONObject address = user.getJSONObject("address");

                                                    User.getInstance().setData(
                                                            user.getString("firstName"),
                                                            user.getString("lastName"),
                                                            user.getString("email"),
                                                            user.getString("phone"),
                                                            address.getString("street"),
                                                            address.getString("number"),
                                                            address.getString("postalCode"),
                                                            address.getString("district"),
                                                            address.getString("city"),
                                                            address.getString("country"),
                                                            password);

                                                    responseData.setDescription("Success");
                                                    responseData.setErrorCode(responseCode);
                                                    responseData.setStatus(0);
                                                    if (onNetworkCallback != null)
                                                        onNetworkCallback.onSuccess(responseData, ResponseExtractor.ResponseType.NEW_USER);
                                                }
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            Log.e(TAG, "Error parsing response: " + e.getLocalizedMessage());
                                            responseData.setDescription("Error parsing data");
                                            responseData.setErrorCode(responseCode);
                                            responseData.setStatus(0);
                                            if (onNetworkCallback != null) {
                                                onNetworkCallback.onFailed(e.getLocalizedMessage(), ResponseExtractor.ResponseType.NEW_USER);
                                            }
                                        }

                                        break;

                                    default:
                                        // Case "Connection blocked by firewall"
                                        responseData.setDescription("An error occured. We couldn't register your account.");
                                        responseData.setErrorCode(responseCode);
                                        responseData.setStatus(0);
                                        if (onNetworkCallback != null)
                                            onNetworkCallback.onFailed(responseData, ResponseExtractor.ResponseType.NEW_USER);
                                }
                            }
                        }
                    });
                }
            });
        }
    }

    public synchronized void loginAccount(Context context, String email, String password) {
        if (DEMO_DATA) {
            final ResponseData responseUser = (ResponseData)
                    getDemoData(
                            ResponseExtractor.ResponseType.EXISTING_USER,
                            context,
                            email,
                            password,
                            null
                    );
            switch (responseUser.getErrorCode()) {
                case 200:
                    JSONObject jsonUser = (JSONObject) responseUser.getResponse();
                    User.getInstance().instantiateUser(jsonUser);

//                    onNetworkCallback.onSuccess(responseUser, ResponseExtractor.ResponseType.EXISTING_USER);
                    this.getCart(context);
                    break;
                default:
                    onNetworkCallback.onFailed(responseUser, ResponseExtractor.ResponseType.EXISTING_USER);
            }
        } else {

            RequestBody body = new FormBody.Builder().add("email", email).add("password", password).build();

            Request request = new Request.Builder()
                    .post(body)
                    .url(NetworkUtils.URL_LOGIN)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    ResponseData responseData = new ResponseData();
                    responseData.setDescription(activity.getResources().getString(R.string.error_sending_request));
                    responseData.setErrorCode(400);
                    responseData.setStatus(0);
                    onNetworkCallback.onFailed(responseData, ResponseExtractor.ResponseType.EXISTING_USER);
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                    int code = response.code();
                    String body = response.body().string();
                    ResponseData responseData = new ResponseData();
                    switch (code) {
                        case 401:
                            // case "Invalid user" or "Wrong Password"
                            responseData.setDescription(activity.getResources().getString(R.string.invalid_user_input));
                            responseData.setStatus(0);
                            responseData.setErrorCode(code);
                            onNetworkCallback.onFailed(responseData, ResponseExtractor.ResponseType.EXISTING_USER);
                            break;

                        case 200:
                            responseData.setDescription("Success");
                            responseData.setStatus(0);
                            responseData.setErrorCode(ResponseData.LOGIN);
                            try {
                                JSONObject responseObject = new JSONObject(body);
                                if (responseObject.has("success")) {
                                    JSONObject obj = (JSONObject) responseObject.get("success");
                                    if (obj.has("token") && obj.has("id")) {
                                        User.getInstance().setToken(obj.get("token").toString());
                                        User.getInstance().setId(Integer.parseInt(obj.get("id").toString()));
                                        if (onNetworkCallback != null) {
                                            onNetworkCallback.onSuccess(responseData, ResponseExtractor.ResponseType.EXISTING_USER);
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                responseData.setDescription(activity.getResources().getString(R.string.error_parsing_response) + " " + e.getLocalizedMessage());
                                responseData.setStatus(0);
                                onNetworkCallback.onFailed(responseData, ResponseExtractor.ResponseType.EXISTING_USER);
                            }
                            break;

                        default:
                            // Usually case "Connection blocked by firewall"
                            responseData.setDescription(activity.getResources().getString(R.string.could_not_load_data));
                            responseData.setErrorCode(code);
                            responseData.setStatus(0);
                            if (onNetworkCallback != null) {
                                onNetworkCallback.onFailed(responseData, ResponseExtractor.ResponseType.EXISTING_USER);
                            }
                            break;
                    }
                }
            });
        }
    }

    public synchronized void logoutUser() {
        Request request = new Request.Builder()
                .header("Authorization", "Bearer" + " " + User.getInstance().getToken())
                .header("Accept", "application/json")
                .url(NetworkUtils.URL_LOGOUT)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure");
                if (onNetworkCallback != null) {
                    onNetworkCallback.onFailed(call.toString(), ResponseExtractor.ResponseType.EXISTING_USER);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.e(TAG, "onResponse");
                int code = response.code();
                ResponseData responseData = new ResponseData();
                switch (code) {
                    case 200:
                        try {
                            JSONObject responseObject = new JSONObject(response.body().string());
                            responseData.setDescription(responseObject.get("message").toString());
                            responseData.setStatus(0);
                            if (onNetworkCallback != null) {
                                onNetworkCallback.onSuccess(responseData, ResponseExtractor.ResponseType.EXISTING_USER);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d(TAG, "Error parsing response: " + e.getLocalizedMessage());
                            responseData.setDescription(activity.getResources().getString(R.string.error_parsing_response) + " " + e.getLocalizedMessage());
                            responseData.setStatus(0);
                            if (onNetworkCallback != null) {
                                onNetworkCallback.onFailed(responseData, ResponseExtractor.ResponseType.EXISTING_USER);
                            }
                        }
                        break;
                    default:
                        // "Connection blocked by firewall" and other cases
                        Log.d(TAG, "Error" + response.body().string());
                        responseData.setDescription(activity.getResources().getString(R.string.unknown_error_msg));
                        responseData.setErrorCode(code);
                        responseData.setStatus(0);
                        if (onNetworkCallback != null) {
                            onNetworkCallback.onFailed(responseData, ResponseExtractor.ResponseType.EXISTING_USER);
                        }
                        break;
                }
            }
        });
    }

    public synchronized void getUser(String token) {
        if (DEMO_DATA) {

        } else {
            Request request = new Request.Builder()
                    .header("Authorization", token)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .url(NetworkUtils.URL_GET_USER + "/" + User.getInstance().getId())
                    .build();

            client.newCall(request)
                    .enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e(TAG, "onFailure");
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            int code = response.code();
                            ResponseData responseData = new ResponseData();
                            switch (code) {
                                case 200:
                                    // case "Success"
                                    try {
                                        JSONObject jsonResponseBody = new JSONObject(response.body().string());
                                        if (jsonResponseBody.has("response")) {
                                            JSONObject userResponse = (JSONObject) jsonResponseBody.get("response");
                                            if (userResponse.has("address")) {
                                                JSONObject address = (JSONObject) userResponse.get("address");
                                                User.getInstance().setData(Integer.parseInt(userResponse.get("id").toString()),
                                                        userResponse.get("firstName").toString(),
                                                        userResponse.get("lastName").toString(),
                                                        userResponse.get("email").toString(),
                                                        userResponse.get("phone").toString(),
                                                        address.get("street").toString(),
                                                        address.get("number").toString(),
                                                        address.get("postalCode").toString(),
                                                        address.get("district").toString(),
                                                        address.get("city").toString(),
                                                        address.get("country").toString());

                                                responseData.setDescription("Success");
                                                responseData.setErrorCode(ResponseData.GET_USER);
                                                responseData.setStatus(0);
                                                if (onNetworkCallback != null) {
                                                    onNetworkCallback.onSuccess(responseData, ResponseExtractor.ResponseType.NEW_USER);
                                                }
                                            }

                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        Log.e(TAG, "Error parsing response: " + e.getLocalizedMessage());
                                        responseData.setDescription("Error parsing data");
                                        responseData.setErrorCode(code);
                                        responseData.setStatus(0);
                                        if (onNetworkCallback != null) {
                                            onNetworkCallback.onFailed(e.getLocalizedMessage(), ResponseExtractor.ResponseType.NEW_USER);
                                        }
                                    }
                                    break;
                                default:
                                    // Usually case "Connection blocked by firewall"
                                    responseData.setDescription("An error occurred. Please try again.");
                                    responseData.setErrorCode(code);
                                    responseData.setStatus(0);
                                    if (onNetworkCallback != null) {
                                        onNetworkCallback.onFailed(responseData, ResponseExtractor.ResponseType.NEW_USER);
                                    }
                                    break;
                            }
                        }
                    });
        }
    }

    public synchronized void resetPassword(Context context, String email) {
        if (DEMO_DATA) {
            ResponseData response = (ResponseData) getDemoData(ResponseExtractor.ResponseType.RESET_PASSWORD, context, email, null, null);
            if (response == null) {
                onNetworkCallback.onFailed(response, ResponseExtractor.ResponseType.RESET_PASSWORD);
            } else {
                switch (response.getErrorCode()) {
                    case 200:
                        onNetworkCallback.onSuccess(response, ResponseExtractor.ResponseType.RESET_PASSWORD);
                        break;
                    default:
                        onNetworkCallback.onFailed(response, ResponseExtractor.ResponseType.RESET_PASSWORD);
                        break;
                }
            }
        } else {
            RequestBody body = new FormBody.Builder()
                    .add("email", email)
                    .build();
            Request request = new Request.Builder()
                    .url(NetworkUtils.URL_RESET_PASSWORD)
                    .post(body)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    ResponseData response = new ResponseData();
                    response.setErrorCode(400);
                    response.setDescription(activity.getResources().getString(R.string.error_sending_request));
                    response.setStatus(0);
                    onNetworkCallback.onFailed(response, ResponseExtractor.ResponseType.RESET_PASSWORD);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    int code = response.code();
                    ResponseData responseData = new ResponseData();

                    switch (code) {
                        case 200:
                            try {
                                responseData.setErrorCode(code);
                                responseData.setStatus(0);
                                JSONObject responseObj = new JSONObject(response.body().string());
                                if (responseObj.has("data")) {
                                    JSONObject data = (JSONObject) responseObj.get("data");
                                    if (data.has("success")) {
                                        responseData.setDescription(data.get("success").toString());
                                    } else {
                                        responseData.setDescription("Mail sent successfully.");
                                    }
                                }

                                if (onNetworkCallback != null) {
                                    onNetworkCallback.onSuccess(responseData, ResponseExtractor.ResponseType.RESET_PASSWORD);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e(TAG, "Error parsing response: " + e.getLocalizedMessage());
                                responseData.setDescription("Error parsing response: " + e.getLocalizedMessage());
                                responseData.setErrorCode(400);
                                if (onNetworkCallback != null) {
                                    onNetworkCallback.onFailed(responseData, ResponseExtractor.ResponseType.RESET_PASSWORD);
                                }
                            }
                            break;
                        default:
                            responseData.setDescription("An error occurred. Please try again.");
                            responseData.setErrorCode(code);
                            responseData.setStatus(0);
                            if (onNetworkCallback != null) {
                                onNetworkCallback.onFailed(responseData, ResponseExtractor.ResponseType.RESET_PASSWORD);
                            }
                            break;
                    }
                }
            });
        }
    }

    public synchronized void updateUserData(JSONObject user) {
        if (DEMO_DATA) {
            ResponseData response = (ResponseData) getDemoData(ResponseExtractor.ResponseType.UPDATE, activity, null, null, User.getInstance().parseToJsonObject());
            switch (response.getErrorCode()) {
                case 200:
                    try {
                        JSONObject address;
                        address = (JSONObject) user.get("address");
                        User.getInstance().setData(Integer.parseInt(user.get("id").toString()),
                                user.get("firstName").toString(),
                                user.get("lastName").toString(),
                                user.get("email").toString(),
                                user.get("phone").toString(),
                                address.get("street").toString(),
                                address.get("number").toString(),
                                address.get("postalCode").toString(),
                                address.get("district").toString(),
                                address.get("city").toString(),
                                address.get("country").toString());
                        response.setDescription("Success");
                        response.setErrorCode(200);
                        response.setStatus(0);
                        if (onNetworkCallback != null) {
                            onNetworkCallback.onSuccess(response, ResponseExtractor.ResponseType.UPDATE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    if (onNetworkCallback != null){
                        onNetworkCallback.onFailed(response, ResponseExtractor.ResponseType.UPDATE);
                    }
            }
        } else {
            RequestBody body = null;
            try {
                JSONObject address;
                address = (JSONObject) user.get("address");
                body = new FormBody.Builder()
                        .add("firstName", user.getString("firstName"))
                        .add("lastName", user.getString("lastName"))
                        .add("phone", user.getString("phone"))
                        .add("street", address.getString("street"))
                        .add("number", address.getString("number"))
                        .add("postalCode", address.getString("postalCode"))
                        .add("city", address.getString("city"))
                        .add("country", address.getString("country"))
                        .add("district", address.getString("district"))
                        .build();

                Request request = new Request.Builder()
                        .header("Authorization", "Bearer" + " " + User.getInstance().getToken())
                        .header("Accept", "application/json")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .url(NetworkUtils.URL_GET_USER + "/" + User.getInstance().getId())
                        .put(body)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        ResponseData response = new ResponseData();
                        response.setDescription(activity.getResources().getString(R.string.error_sending_request));
                        response.setStatus(0);
                        if (onNetworkCallback != null) {
                            onNetworkCallback.onFailed(response, ResponseExtractor.ResponseType.UPDATE);
                        }

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        int code = response.code();
                        ResponseData responseData = new ResponseData();

                        switch (code) {
                            case 200:
                                // Case Success
                                try {
                                    JSONObject jsonResponseBody = new JSONObject(response.body().string());
                                    if (jsonResponseBody.has("response")){
                                        JSONObject userResponse = (JSONObject) jsonResponseBody.get("response");
                                        if (userResponse.has("address")) {
                                            JSONObject address = (JSONObject) userResponse.get("address");
                                            User.getInstance().setData(Integer.parseInt(userResponse.get("id").toString()),
                                                    userResponse.get("firstName").toString(),
                                                    userResponse.get("lastName").toString(),
                                                    userResponse.get("email").toString(),
                                                    userResponse.get("phone").toString(),
                                                    address.get("street").toString(),
                                                    address.get("number").toString(),
                                                    address.get("postalCode").toString(),
                                                    address.get("district").toString(),
                                                    address.get("city").toString(),
                                                    address.get("country").toString());

                                            responseData.setDescription("Success");
                                            responseData.setErrorCode(code);
                                            responseData.setStatus(0);
                                            if (onNetworkCallback != null) {
                                                onNetworkCallback.onSuccess(responseData, ResponseExtractor.ResponseType.NEW_USER);
                                            }
                                        }
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

//                                try {
//                                    JSONObject jsonResponseBody = new JSONObject(response.body().string());
//                                    if (jsonResponseBody.has("response")) {
//                                        JSONObject userResponse = (JSONObject) jsonResponseBody.get("response");
//                                        if (userResponse.has("address")) {
//                                            JSONObject address = (JSONObject) userResponse.get("address");
//                                            User.getInstance().setData(Integer.parseInt(userResponse.get("id").toString()),
//                                                    userResponse.get("firstName").toString(),
//                                                    userResponse.get("lastName").toString(),
//                                                    userResponse.get("email").toString(),
//                                                    userResponse.get("phone").toString(),
//                                                    address.get("street").toString(),
//                                                    address.get("number").toString(),
//                                                    address.get("postalCode").toString(),
//                                                    address.get("district").toString(),
//                                                    address.get("city").toString(),
//                                                    address.get("country").toString());
//
//                                            responseData.setDescription("Success");
//                                            responseData.setErrorCode(code);
//                                            responseData.setStatus(0);
//                                            if (onNetworkCallback != null) {
//                                                onNetworkCallback.onSuccess(responseData, ResponseExtractor.ResponseType.NEW_USER);
//                                            }
//                                        }
//
//                                    } else {
//                                        if (onNetworkCallback != null) {
//                                            responseData.setDescription("Error parsing json");
//                                            responseData.setErrorCode(code);
//                                            responseData.setStatus(0);
//                                            onNetworkCallback.onFailed(responseData, ResponseExtractor.ResponseType.UPDATE);
//                                        }
//                                    }
//                                } catch (JSONException e) {
//                                    e.printStackTrace();
//                                    if (onNetworkCallback != null) {
//                                        responseData.setDescription("Error parsing json");
//                                        onNetworkCallback.onFailed(responseData, ResponseExtractor.ResponseType.UPDATE);
//                                    }
//                                }

                                break;

                            default:
                                // Any other cases
                                responseData.setDescription("Failure");
                                if (onNetworkCallback != null) {
                                    responseData.setDescription(response.message());
                                    onNetworkCallback.onFailed(responseData, ResponseExtractor.ResponseType.UPDATE);
                                }
                                break;
                        }
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
                if (onNetworkCallback != null) {
                    onNetworkCallback.onFailed(e.getMessage(), ResponseExtractor.ResponseType.UPDATE);
                }
            }

        }
    }

    public synchronized void getWishList(Context context) {
        if (User.getInstance().isAnon()) {
            SharedPreferences sp = context.getSharedPreferences("userData", Context.MODE_PRIVATE);
            String data = sp.getString("wishlist", "");

            JSONArray array = new JSONArray();
            while (data.length() > 0) {
                int index = data.indexOf(' ');
                if (index < 0) {
                    break;
                }
                int id = Integer.parseInt(data.substring(0, index));
                array.put(id);
                data = data.substring(index + 1);
            }
            ProductListsManager.getInstance().setWishlist(array);
            ResponseData response = new ResponseData();
            response.setDescription("Success");
            response.setErrorCode(200);
            response.setStatus(0);
            onNetworkCallback.onSuccess(response, ResponseExtractor.ResponseType.WISHLIST);
        } else {
            if (DEMO_DATA) {
                ResponseData responseData = (ResponseData) NetworkDemoHelper.getDemoData(ResponseExtractor.ResponseType.WISHLIST, context, null, null, null);

                switch (responseData.getErrorCode()) {
                    case 200:
                        JSONObject data = (JSONObject) responseData.getResponse();
                        try {
                            JSONArray array = data.getJSONArray("products");
                            ProductListsManager.getInstance().setWishlist(array);
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing data: " + e.getLocalizedMessage());
                        }
                        onNetworkCallback.onSuccess(responseData, ResponseExtractor.ResponseType.WISHLIST);
                        break;

                    default:
                        onNetworkCallback.onFailed(responseData, ResponseExtractor.ResponseType.WISHLIST);
                        break;
                }
            } else {
                client.newCall(
                        new Request.Builder()
                                .url(String.format(Utils.GET_WISHLIST_URL, User.getInstance().getId()))
                                .build()
                ).enqueue(
                        new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                ResponseData responseData = new ResponseData();
                                responseData.setDescription(activity.getResources().getString(R.string.error_sending_request));
                                responseData.setErrorCode(400);
                                responseData.setStatus(0);
                                onNetworkCallback.onFailed(responseData, ResponseExtractor.ResponseType.WISHLIST);
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                String body = response.body().string();
                                int code = response.code();

                                ResponseData responseData = new ResponseData();
                                responseData.setErrorCode(code);
                                responseData.setStatus(0);
                                switch (code) {
                                    case 200:
                                        try {
                                            JSONObject data = new JSONObject(body);
                                            JSONArray array = data.getJSONArray("products");
                                            ProductListsManager.getInstance().setWishlist(array);
                                            responseData.setDescription("Success");
                                        } catch (JSONException e) {
                                            Log.e(TAG, "Error parsing response: " + e.getLocalizedMessage());
                                            responseData.setDescription("Error parsing response: " + e.getLocalizedMessage());
                                            responseData.setErrorCode(400);
                                        }
                                        onNetworkCallback.onSuccess(responseData, ResponseExtractor.ResponseType.WISHLIST);
                                        break;

                                    default:
                                        String msg = String.format(Locale.US, "Error: %d: " + body, response.code());
                                        responseData.setDescription(msg);
                                        onNetworkCallback.onFailed(responseData, ResponseExtractor.ResponseType.WISHLIST);
                                }
                            }
                        }
                );
            }
        }
    }

    public synchronized void getCart(final Context context) {
        if (DEMO_DATA) {
            if (User.getInstance().isAnon()) {
                SharedPreferences sp = context.getSharedPreferences("userData", Context.MODE_PRIVATE);
                String data = sp.getString("cart", "");
                JSONObject object = null;
                try {
                    object = new JSONObject(data);

                    JSONArray array = object.getJSONArray("products");
                    ProductListsManager.getInstance().setCart(array);

                    ResponseData response = new ResponseData();
                    response.setDescription("Success");
                    response.setErrorCode(200);
                    response.setStatus(0);
                    this.updateWishlist(context);
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing sharedPrefs data: " + e.getLocalizedMessage());
                    ProductListsManager.getInstance().setCart(new JSONArray());

                    ResponseData response = new ResponseData();
                    response.setDescription("Error parsing sharedPrefs data: " + e.getLocalizedMessage());
                    response.setErrorCode(200);
                    response.setStatus(0);
                    onNetworkCallback.onSuccess(response, ResponseExtractor.ResponseType.CARTLIST);
                }
            } else {
                ResponseData responseData = (ResponseData) NetworkDemoHelper.getDemoData(ResponseExtractor.ResponseType.CARTLIST, context, null, null, null);
                int code = responseData.getErrorCode();
                switch (code) {
                    case 200:
                        JSONObject jsonUser = (JSONObject) responseData.getResponse();
                        try {
                            JSONArray products = jsonUser.getJSONArray("products");
                            ProductListsManager.getInstance().setCart(products);

                            this.getWishList(context);
                        } catch (JSONException e) {
                            Log.e(TAG, e.getLocalizedMessage());

                            responseData.setDescription(e.getLocalizedMessage());
                            responseData.setErrorCode(400);
                            onNetworkCallback.onFailed(responseData, ResponseExtractor.ResponseType.CARTLIST);
                        }
                        break;

                    case 401:
                        onNetworkCallback.onFailed(responseData, ResponseExtractor.ResponseType.CARTLIST);
                        break;

                    default:
                        onNetworkCallback.onFailed(responseData, ResponseExtractor.ResponseType.CARTLIST);
                        break;
                }
            }
        } else {
            Request request = new Request.Builder()
                    .header("Authorization", User.getInstance().getToken())
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .url(NetworkUtils.URL_PRODUCTS + "/" + User.getInstance().getId())
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    ResponseData response = new ResponseData();
                    response.setErrorCode(400);
                    response.setDescription(activity.getResources().getString(R.string.error_sending_request));
                    response.setStatus(0);
                    onNetworkCallback.onFailed(response, ResponseExtractor.ResponseType.CARTLIST);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String body = response.body().string();
                    int code = response.code();
                    ResponseData responseData = new ResponseData();
                    responseData.setErrorCode(code);
                    responseData.setStatus(0);
                    switch (code) {
                        case 200:
                            try {
                                JSONObject data = new JSONObject(body);
                                JSONArray array = data.getJSONArray("products");
                                ProductListsManager.getInstance().setCart(array);

                                responseData.setDescription("Success");
                                getWishList(context);
                            } catch (JSONException e) {
                                Log.e(TAG, "Error parsing response: " + e.getLocalizedMessage());
                                responseData.setDescription("Error parsing response: " + e.getLocalizedMessage());
                                responseData.setErrorCode(400);
                                onNetworkCallback.onFailed(responseData, ResponseExtractor.ResponseType.CARTLIST);
                            }
                            break;

                        default:
                            responseData.setDescription("Unknown error");
                            onNetworkCallback.onFailed(responseData, ResponseExtractor.ResponseType.CARTLIST);
                    }
                }
            });
        }
//        }
    }

    public synchronized void updateCart(Context context) {
        if (User.getInstance().isAnon()) {
//            SharedPreferences sp = context.getSharedPreferences("userData", Context.MODE_PRIVATE);
//            SharedPreferences.Editor ed = sp.edit();
//
//            JSONObject object = ProductListsManager.getInstance().getJsonCartList();
//
//            ed.putString("cart", object.toString());
//            ed.apply();

            ResponseData responseData = new ResponseData();
            responseData.setErrorCode(200);
            responseData.setDescription("Success");
            responseData.setStatus(0);
            this.updateWishlist(context);
        } else {
            if (DEMO_DATA) {
                ResponseData responseData = (ResponseData) NetworkDemoHelper.getDemoData(ResponseExtractor.ResponseType.UPDATE_CART, context, null, null, null);
                switch (responseData.getErrorCode()) {
                    case 200:
                        this.updateWishlist(context);
                        break;

                    default:
                        onNetworkCallback.onFailed(responseData, ResponseExtractor.ResponseType.UPDATE_CART);
                        break;
                }
            } else {
                JSONObject jsonData = ProductListsManager.getInstance().getJsonCartList();
                try {
                    client.newCall(
                            new Request.Builder()
                                    .url(Utils.UPDATE_CART_URL)
                                    .post(
                                            new FormBody.Builder()
                                                    .add("id", jsonData.getString("id"))
                                                    .add("list", jsonData.getString("products"))
                                                    .build()
                                    )
                                    .build()
                    ).enqueue(
                            new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    ResponseData response = new ResponseData();
                                    response.setDescription(activity.getResources().getString(R.string.error_sending_request));
                                    response.setErrorCode(400);
                                    response.setStatus(0);
                                    onNetworkCallback.onFailed(response, ResponseExtractor.ResponseType.UPDATE_CART);
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    int code = response.code();
                                    ResponseData responseData = new ResponseData();
                                    responseData.setErrorCode(code);
                                    responseData.setStatus(0);
                                    switch (response.code()) {
                                        case 200:
                                            responseData.setDescription("Success");
                                            onNetworkCallback.onSuccess(responseData, ResponseExtractor.ResponseType.UPDATE_CART);
                                            break;

                                        default:
                                            responseData.setDescription("Failure");
                                            onNetworkCallback.onFailed(responseData, ResponseExtractor.ResponseType.UPDATE_CART);
                                            break;
                                    }
                                }
                            }
                    );
                    this.updateWishlist(context);
                } catch (JSONException e) {
                    Log.e(TAG, "JSON parse error: " + e.getLocalizedMessage());
                    ResponseData responseData = new ResponseData();
                    responseData.setDescription("JSON parse error: " + e.getLocalizedMessage());
                    responseData.setStatus(0);
                    responseData.setErrorCode(400);
                    onNetworkCallback.onFailed(responseData, ResponseExtractor.ResponseType.UPDATE_CART);
                }
            }
        }
    }

    public synchronized void updateWishlist(Context context) {
        if (User.getInstance().isAnon()) {
            SharedPreferences sp = context.getSharedPreferences("userData", Context.MODE_PRIVATE);
            SharedPreferences.Editor ed = sp.edit();

            ArrayList<Product> list = ProductListsManager.getInstance().getFavoriteProducts();
            String ids = "";
            for (Product p : list) {
                ids += p.getProductId() + " ";
            }
            ed.putString("wishlist", ids);
            ed.apply();

            ResponseData responseData = new ResponseData();
            responseData.setErrorCode(200);
            responseData.setDescription("Success");
            responseData.setStatus(0);
            onNetworkCallback.onSuccess(responseData, ResponseExtractor.ResponseType.UPDATE_WISHLIST);
        } else {
            if (DEMO_DATA) {
                ResponseData responseData = (ResponseData) NetworkDemoHelper.getDemoData(ResponseExtractor.ResponseType.UPDATE_WISHLIST, context, null, null, null);
                switch (responseData.getErrorCode()) {
                    case 200:
                        if (onNetworkCallback != null) {
                            onNetworkCallback.onSuccess(responseData, ResponseExtractor.ResponseType.UPDATE_WISHLIST);
                            Log.e(TAG, "updateWishlist: finished");
                        }
                        break;

                    default:
                        onNetworkCallback.onFailed(responseData, ResponseExtractor.ResponseType.UPDATE_WISHLIST);
                        Log.e(TAG, "updateWishlist: finished");
                        break;
                }
            } else {
                JSONObject jsonData = ProductListsManager.getInstance().getJsonWishList();
                try {
                    client.newCall(
                            new Request.Builder()
                                    .url(Utils.UPDATE_WISHLIST_URL)
                                    .post(
                                            new FormBody.Builder()
                                                    .add("id", jsonData.getString("id"))
                                                    .add("list", jsonData.getString("products"))
                                                    .build()
                                    )
                                    .build()
                    ).enqueue(
                            new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    ResponseData responseData = new ResponseData();
                                    responseData.setErrorCode(400);
                                    responseData.setDescription(activity.getResources().getString(R.string.error_sending_request));
                                    responseData.setStatus(0);
                                    onNetworkCallback.onFailed(responseData, ResponseExtractor.ResponseType.UPDATE_WISHLIST);
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    int code = response.code();
                                    ResponseData responseData = new ResponseData();
                                    responseData.setErrorCode(code);
                                    responseData.setStatus(0);
                                    switch (code) {
                                        case 200:
                                            responseData.setDescription("Success");
                                            onNetworkCallback.onSuccess(responseData, ResponseExtractor.ResponseType.UPDATE_WISHLIST);
                                            break;

                                        default:
                                            responseData.setDescription("Unknown error");
                                            onNetworkCallback.onFailed(responseData, ResponseExtractor.ResponseType.UPDATE_WISHLIST);
                                            break;
                                    }
                                }
                            }
                    );
                } catch (JSONException e) {
                    Log.e(TAG, "JSON parse error: " + e.getLocalizedMessage());

                    ResponseData responseData = new ResponseData();
                    responseData.setErrorCode(400);
                    responseData.setDescription("JSON parse error: " + e.getLocalizedMessage());
                    responseData.setStatus(0);
                    onNetworkCallback.onFailed(responseData, ResponseExtractor.ResponseType.UPDATE_WISHLIST);
                }
            }
        }
    }
}
