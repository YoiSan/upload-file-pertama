/*
 * Copyright © 2018-present, MNK Group. All rights reserved.
 */
package com.themedimension.ivoryshop.android.manager;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.JsonParser;
import com.themedimension.ivoryshop.android.R;
import com.themedimension.ivoryshop.android.utils.Utils;
import com.themedimension.ivoryshop.android.models.User;
import com.themedimension.ivoryshop.android.response.ResponseData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Random;

import static com.mikepenz.iconics.Iconics.TAG;

/**
 * Class to simulate the behaviour of app-server interaction.
 */
class NetworkDemoHelper {

    //region GLOBALS

    private static final String FILE_NAME_USERS = "demo_users.json";
    private static final String FILE_NAME_FAVORITES = "demo_favorites.json";
    private static final String FILE_NAME_CART = "demo_cart.json";
    private static final String FILE_ORDERS = "demo_orders.json";

    //endregion

    static Object getDemoData(ResponseExtractor.ResponseType responseType, Context context, @Nullable String extra1, @Nullable String extra2, @Nullable JSONObject extra3) {
        switch (responseType) {
            case PRODUCT_LIST:
                return loadDemoProductList(context);

            case NEW_USER:
                return loadDemoNewUser(context, extra3);

            case EXISTING_USER:
                return loadDemoExistingUser(context, extra1, extra2);

            case RESET_PASSWORD:
                return resetDemoPassword(context, extra1);

            case UPDATE:
                return changeDemoUserData(context, extra3);

            case WISHLIST:
                return getDemoFavoriteProductIds(context, User.getInstance().getId());

            case CARTLIST:
                return getDemoCart(context, User.getInstance().getId());

            case UPDATE_CART:
                return updateDemoCart(context, User.getInstance().getId());

            case UPDATE_WISHLIST:
                return updateDemoWishList(context, User.getInstance().getId());

            case PLACE_ORDER:
                return placeDemoOrder(context, extra3);
        }
        return null;
    }

    //region USER REQUESTS

    private synchronized static ResponseData loadDemoNewUser(Context context, JSONObject data) {
        ResponseData responseData = new ResponseData();
        String msg = "Success";

        File f = new File(context.getExternalFilesDir(null), "users.json");
        if (f.exists()) {
            try {
                Object obj = new JsonParser().parse(new FileReader(f));

                JSONObject jsonData = new JSONObject(obj.toString());
                JSONArray jsonUsers = jsonData.getJSONArray("users");
                for (int i = 0; i < jsonUsers.length(); i++) {
                    JSONObject savedUser = jsonUsers.getJSONObject(i);

                    if (savedUser.getString("email").contentEquals(data.getString("email"))) {
                        msg = "Email already used";
                        responseData.setErrorCode(201);
                        responseData.setDescription(msg);
                        responseData.setStatus(0);
                        return responseData;
                    }
                }

                data.put("id", createDemoId(jsonUsers));
                jsonUsers.put(data);
                jsonData.put("users", jsonUsers);

                PrintWriter writer = new PrintWriter(f);
                writer.write(jsonData.toString());
                writer.close();

                responseData.setErrorCode(200);
                responseData.setDescription(msg);
                responseData.setStatus(0);
                return responseData;
            } catch (FileNotFoundException e) {
                msg = "Error opening file: " + e.getLocalizedMessage();
                responseData.setErrorCode(402);
                responseData.setDescription(msg);
                responseData.setStatus(0);
                Log.e(TAG, msg);
                return responseData;
            } catch (JSONException e) {
                responseData.setErrorCode(400);
                responseData.setDescription(msg);
                responseData.setStatus(0);
                msg = "Error parsing file content: " + e.getLocalizedMessage();
                Log.e(TAG, msg);
                return responseData;
            }
        } else {
            try {
                f.createNewFile();
                f.setReadable(true);
                f.setWritable(true);

                InputStream is = context.getResources().openRawResource(R.raw.users);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String buffer = "";
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer += line;
                }

                JSONObject jsonData = new JSONObject(buffer);
                JSONArray array = jsonData.getJSONArray("users");

                for (int i = 0; i < array.length(); i++) {
                    JSONObject savedUser = array.getJSONObject(i);
                    if (savedUser.getString("email").contentEquals(data.getString("email"))) {
                        msg = "Email already used";
                        responseData.setErrorCode(400);
                        responseData.setDescription(msg);
                        responseData.setStatus(0);
                    }
                }

                data.put("id", createDemoId(array));
                array.put(data);
                jsonData.put("users", array);
                PrintWriter writer = new PrintWriter(f);
                writer.write(jsonData.toString());
                writer.close();

                responseData.setErrorCode(200);
                responseData.setDescription(msg);
                responseData.setStatus(0);
                return responseData;
            } catch (IOException e) {
                msg = "Error creating file: " + e.getLocalizedMessage();
                responseData.setErrorCode(401);
                responseData.setDescription(msg);
                responseData.setStatus(0);
                Log.e(TAG, msg);
                return responseData;
            } catch (JSONException e) {
                msg = "Error creating content: " + e.getLocalizedMessage();
                responseData.setErrorCode(400);
                responseData.setDescription(msg);
                responseData.setStatus(0);
                Log.e(TAG, msg);
                return responseData;
            }
        }
    }

    private synchronized static ResponseData<JSONObject> loadDemoExistingUser(Context context, String email, String pass) {
        Log.e(TAG, "loadDemoExistingUser: ");

        ResponseData<JSONObject> responseData = new ResponseData<JSONObject>();
        String msg = "Success";

        File f = new File(context.getExternalFilesDir(null), "users.json");
        if (f.exists()) {
            try {
                FileInputStream fis = new FileInputStream(f);
                DataInputStream in = new DataInputStream(fis);
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String buffer = "";
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer += line;
                }

                JSONObject jsonData = new JSONObject(buffer);
                JSONArray array = jsonData.getJSONArray("users");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject savedUser = array.getJSONObject(i);
                    if (savedUser.getString("email").contentEquals(email) && savedUser.getString("password").contentEquals(pass)) {
                        responseData.setErrorCode(200);
                        responseData.setDescription(msg);
                        responseData.setStatus(0);
                        responseData.setResponse(savedUser);
                        return responseData;
                    } else if (savedUser.getString("email").contentEquals(email)) {
                        msg = "Invalid email / password";
                        responseData.setErrorCode(201);
                        responseData.setDescription(msg);
                        responseData.setStatus(0);
                        return responseData;
                    }
                }

                msg = "User doesn't exist.";
                responseData.setErrorCode(201);
                responseData.setDescription(msg);
                responseData.setStatus(0);
                return responseData;
            } catch (FileNotFoundException e) {
                msg = "Error opening file: " + e.getLocalizedMessage();
                responseData.setErrorCode(402);
                responseData.setDescription(msg);
                responseData.setStatus(0);
                return responseData;
            } catch (IOException e) {
                msg = "Error reading file content: " + e.getLocalizedMessage();
                responseData.setErrorCode(401);
                responseData.setDescription(msg);
                responseData.setStatus(0);
                return responseData;
            } catch (JSONException e) {
                msg = "Error parsing content: " + e.getLocalizedMessage();
                responseData.setErrorCode(400);
                responseData.setDescription(msg);
                responseData.setStatus(0);
                return responseData;
            }
        } else {
            try {
                f.createNewFile();
                f.setReadable(true);
                f.setWritable(true);

                InputStream is = context.getResources().openRawResource(R.raw.users);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String buffer = "";
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer += line;
                }

                JSONObject rawData = new JSONObject(buffer);

                JSONArray jsonArray = rawData.getJSONArray("users");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject savedUser = jsonArray.getJSONObject(i);
                    if (savedUser.getString("email").contentEquals(email) && savedUser.getString("password").contentEquals(pass)) {
                        msg = "Success";
                        responseData.setErrorCode(200);
                        responseData.setDescription(msg);
                        responseData.setStatus(0);
                        responseData.setResponse(savedUser);

                        PrintWriter writer = new PrintWriter(f);
                        writer.write(rawData.toString());
                        writer.close();
                        return responseData;
                    } else if (savedUser.getString("email").contentEquals(email)) {
                        msg = "Invalid email / password";
                        responseData.setErrorCode(201);
                        responseData.setDescription(msg);
                        responseData.setStatus(0);

                        PrintWriter writer = new PrintWriter(f);
                        writer.write(rawData.toString());
                        writer.close();
                        return responseData;
                    }
                }

                msg = "User doesn't exist.";
                responseData.setErrorCode(201);
                responseData.setDescription(msg);
                responseData.setStatus(0);

                PrintWriter writer = new PrintWriter(f);
                writer.write(rawData.toString());
                writer.close();
                return responseData;
            } catch (IOException e) {
                msg = "Error creating file: " + e.getLocalizedMessage();
                responseData.setErrorCode(403);
                responseData.setDescription(msg);
                responseData.setStatus(0);
                return responseData;
            } catch (JSONException e) {
                msg = "Error parsing file content: " + e.getLocalizedMessage();
                responseData.setErrorCode(401);
                responseData.setDescription(msg);
                responseData.setStatus(0);
                return responseData;
            }
        }
    }

    private synchronized static ResponseData resetDemoPassword(Context context, String email) {
        ResponseData responseData = new ResponseData();
        String msg = "Success";

        File f = new File(context.getExternalFilesDir(null), "users.json");
        if (f.exists()) {
            try {
                Object obj = new JsonParser().parse(new FileReader(f));

                JSONObject data = new JSONObject(obj.toString());
                JSONArray array = data.getJSONArray("users");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject savedUser = array.getJSONObject(i);
                    if (savedUser.getString("email").contentEquals(email)) {
                        savedUser.put("password", Utils.md5("pass12345"));

                        array.put(i, savedUser);
                        data.put("users", array);

                        PrintWriter writer = new PrintWriter(f);
                        writer.write(data.toString());
                        writer.close();

                        responseData.setErrorCode(200);
                        responseData.setDescription(msg);
                        responseData.setStatus(0);
                        return responseData;
                    }
                }

                msg = "Email doesn't exist.";
                responseData.setErrorCode(402);
                responseData.setDescription(msg);
                responseData.setStatus(0);
                return responseData;
            } catch (FileNotFoundException e) {
                msg = "Error opening file: " + e.getLocalizedMessage();
                responseData.setErrorCode(402);
                responseData.setDescription(msg);
                responseData.setStatus(0);
                return responseData;
            } catch (JSONException e) {
                msg = "Error parsing content: " + e.getLocalizedMessage();
                responseData.setErrorCode(400);
                responseData.setDescription(msg);
                responseData.setStatus(0);
                return responseData;
            }

        } else {
            try {
                f.createNewFile();
                f.setWritable(true);
                f.setReadable(true);

                InputStream is = context.getResources().openRawResource(R.raw.users);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                String buffer = "";
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer += line;
                }

                JSONObject data = new JSONObject(buffer);
                JSONArray array = data.getJSONArray("users");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject savedUser = array.getJSONObject(i);
                    if (savedUser.getString("email").contentEquals(email)) {
                        savedUser.put("password", Utils.md5("pass12345"));

                        array.put(i, savedUser);
                        data.put("users", array);

                        PrintWriter writer = new PrintWriter(f);
                        writer.write(data.toString());
                        writer.close();

                        responseData.setErrorCode(200);
                        responseData.setDescription(msg);
                        responseData.setStatus(0);
                        return responseData;
                    }
                }
                PrintWriter writer = new PrintWriter(f);
                writer.write(data.toString());
                writer.close();

                msg = "Invalid email";
                responseData.setErrorCode(402);
                responseData.setDescription(msg);
                responseData.setStatus(0);
                return responseData;
            } catch (FileNotFoundException e) {
                msg = "Error creating file: " + e.getLocalizedMessage();
                responseData.setErrorCode(403);
                responseData.setDescription(msg);
                responseData.setStatus(0);
                return responseData;
            } catch (IOException e) {
                msg = "Error reading content from file: " + e.getLocalizedMessage();
                responseData.setErrorCode(401);
                responseData.setDescription(msg);
                responseData.setStatus(0);
                return responseData;
            } catch (JSONException e) {
                msg = "Error parsing content: " + e.getLocalizedMessage();
                responseData.setErrorCode(400);
                responseData.setDescription(msg);
                responseData.setStatus(0);
                return responseData;
            }

        }
    }

    private synchronized static ResponseData changeDemoUserData(Context context, JSONObject data) {
        ResponseData responseData = new ResponseData();
        String msg = "Success";

        File f = new File(context.getExternalFilesDir(null), "users.txt");
        try {
            FileInputStream fis = new FileInputStream(f);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));

            String buffer = "";
            String line;
            while ((line = reader.readLine()) != null) {
                buffer += line;
            }

            JSONObject jsonData = new JSONObject(buffer);
            JSONArray array = jsonData.getJSONArray("users");
            for (int i = 0; i < array.length(); i++) {
                JSONObject savedUser = array.getJSONObject(i);
                if (savedUser.getInt("id") == data.getInt("id")) {
                    array.put(i, data);
                    jsonData.put("users", array);

                    PrintWriter writer = new PrintWriter(f);
                    writer.write(jsonData.toString());
                    writer.close();

                    responseData.setErrorCode(200);
                    responseData.setDescription(msg);
                    responseData.setStatus(0);
                    return responseData;
                }
            }

            msg = "User not found";
            responseData.setErrorCode(402);
            responseData.setDescription(msg);
            responseData.setStatus(0);
            return responseData;
        } catch (FileNotFoundException e) {
            msg = "Error opening file: " + e.getLocalizedMessage();
            responseData.setErrorCode(402);
            responseData.setDescription(msg);
            responseData.setStatus(0);
            return responseData;
        } catch (IOException e) {
            msg = "Error reading content from file: " + e.getLocalizedMessage();
            responseData.setErrorCode(403);
            responseData.setDescription(msg);
            responseData.setStatus(0);
            return responseData;
        } catch (JSONException e) {
            msg = "Error parsing content of file: " + e.getLocalizedMessage();
            responseData.setErrorCode(401);
            responseData.setDescription(msg);
            responseData.setStatus(0);
            return responseData;
        }

    }

    //endregion

    //region RETRIEVE LISTS

    private synchronized static ResponseData<JSONObject> getDemoFavoriteProductIds(Context context, int id) {
        ResponseData<JSONObject> responseData = new ResponseData<JSONObject>();
        String msg = "Success";

        File f = new File(context.getExternalFilesDir(null), FILE_NAME_FAVORITES);
        if (f.exists()) {
            try {
//                FileInputStream fis = new FileInputStream(f);
//                BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
//                String buffer = "";
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    buffer += line;
//                }
                Object obj = new JsonParser().parse(new FileReader(f));
                JSONObject data = new JSONObject(obj.toString());
                JSONArray array = data.getJSONArray("users");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject savedData = array.getJSONObject(i);
                    if (savedData.getInt("id") == id) {
                        responseData.setErrorCode(200);
                        responseData.setDescription(msg);
                        responseData.setStatus(0);
                        responseData.setResponse(savedData);
                        return responseData;
                    }
                }

                JSONObject newData = new JSONObject();
                newData.put("id", id);
                newData.put("products", new JSONArray());
                array.put(newData);
                data.put("users", array);

                PrintWriter writer = new PrintWriter(f);
                writer.write("");
                writer.write(data.toString());
                writer.close();

                responseData.setErrorCode(200);
                responseData.setDescription(msg);
                responseData.setStatus(0);
                responseData.setResponse(newData);
                return responseData;
            } catch (FileNotFoundException e) {
                msg = "Error opening file: " + e.getLocalizedMessage();
                responseData.setErrorCode(402);
                responseData.setDescription(msg);
                responseData.setStatus(0);
                return responseData;
            } catch (JSONException e) {
                msg = "Error parsing content: " + e.getLocalizedMessage();
                responseData.setErrorCode(400);
                responseData.setDescription(msg);
                responseData.setStatus(0);
                return responseData;
            }
        } else {
            JSONObject newData = new JSONObject();
            JSONArray newArray = new JSONArray();
            newArray.put(-1);
            try {
                f.createNewFile();
                f.setReadable(true);
                f.setWritable(true);

                InputStream is = context.getResources().openRawResource(R.raw.wishlist);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String buffer = "";
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer += line;
                }

                JSONObject savedJson = new JSONObject(buffer);
                JSONArray savedArray = savedJson.getJSONArray("users");
                for(int i = 0; i < savedArray.length(); i++) {
                    JSONObject savedUser = savedArray.getJSONObject(i);
                    if(savedUser.getInt("id") == id) {
                        PrintWriter writer = new PrintWriter(f);
                        writer.write("");
                        writer.write(savedJson.toString());
                        writer.close();

                        responseData.setDescription(msg);
                        responseData.setErrorCode(200);
                        responseData.setStatus(0);
                        responseData.setResponse(savedUser);
                        return responseData;
                    }
                }


                newData.put("id", id);
                newData.put("products", newArray);

                savedArray.put(newData);
                savedJson.put("users", savedArray);

                PrintWriter writer = new PrintWriter(f);
                writer.write("");
                writer.write(savedJson.toString());
                writer.close();

                responseData.setErrorCode(200);
                responseData.setDescription(msg);
                responseData.setStatus(0);
                responseData.setResponse(newData);
                return responseData;
            } catch (JSONException e) {
                msg = "Error creating new entry in file: " + e.getLocalizedMessage();
                responseData.setErrorCode(404);
                responseData.setDescription(msg);
                responseData.setStatus(0);
                return responseData;
            } catch (IOException e) {
                msg = "Error creating file: " + e.getLocalizedMessage();
                responseData.setErrorCode(403);
                responseData.setDescription(msg);
                responseData.setStatus(0);
                return responseData;
            }
        }
    }

    private synchronized static ResponseData<JSONObject> getDemoCart(Context context, int id) {
        ResponseData<JSONObject> responseData = new ResponseData<JSONObject>();
        String msg = "Success";

        File f = new File(context.getExternalFilesDir(null), FILE_NAME_CART);
        if (f.exists()) {
            try {
                Object obj = new JsonParser().parse(new FileReader(f));

                JSONObject data = new JSONObject(obj.toString());
                JSONArray array = data.getJSONArray("users");

                for (int i = 0; i < array.length(); i++) {
                    JSONObject savedData = array.getJSONObject(i);
                    if (savedData.getInt("id") == id) {
                        responseData.setErrorCode(200);
                        responseData.setDescription(msg);
                        responseData.setStatus(0);
                        responseData.setResponse(savedData);
                        return responseData;
                    }
                }
                JSONObject newData = new JSONObject();
                newData.put("id", id);
                newData.put("products", new JSONArray());
                array.put(newData);
                data.put("users", array);

                PrintWriter writer = new PrintWriter(f);
                writer.write("");
                writer.write(data.toString());
                writer.close();

                responseData.setErrorCode(200);
                responseData.setDescription(msg);
                responseData.setStatus(0);
                responseData.setResponse(newData);
                return responseData;
            } catch (FileNotFoundException e) {
                msg = "Error opening file: " + e.getLocalizedMessage();
                responseData.setErrorCode(402);
                responseData.setDescription(msg);
                responseData.setStatus(0);
                return responseData;
            } catch (JSONException e) {
                Log.e(TAG, "Parse error: ");
                System.out.println(e.getLocalizedMessage());

                msg = "Error parsing content: " + e.getLocalizedMessage();
                responseData.setErrorCode(400);
                responseData.setDescription(msg);
                responseData.setStatus(0);
                return responseData;
            }
        } else {
            try {
                InputStream is = context.getResources().openRawResource(R.raw.cart);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String buffer = "";
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer += line;
                }

                JSONObject savedJson = new JSONObject(buffer);

                f.createNewFile();
                f.setReadable(true);
                f.setWritable(true);

                JSONArray array = savedJson.getJSONArray("users");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject savedUser = array.getJSONObject(i);
                    if (savedUser.getInt("id") == id) {
                        PrintWriter writer = new PrintWriter(f);
                        writer.write("");
                        writer.write(savedJson.toString());
                        writer.close();

                        responseData.setDescription(msg);
                        responseData.setErrorCode(200);
                        responseData.setStatus(0);
                        responseData.setResponse(savedUser);
                        return responseData;
                    }
                }

                JSONObject newUser = new JSONObject();
                JSONArray newArray = new JSONArray();
                newUser.put("id", id);
                newUser.put("products", newArray);

                array.put(newUser);
                savedJson.put("users", array);

                PrintWriter writer = new PrintWriter(f);
                writer.write("");
                writer.write(savedJson.toString());
                writer.close();

                responseData.setErrorCode(200);
                responseData.setDescription(msg);
                responseData.setStatus(0);
                responseData.setResponse(newUser);
                return responseData;
            } catch (JSONException e) {
                Log.e(TAG, "Parse error: ");
                System.out.println(e.getLocalizedMessage());

                msg = "Error creating new entry in file: " + e.getLocalizedMessage();
                responseData.setErrorCode(404);
                responseData.setDescription(msg);
                responseData.setStatus(0);
                return responseData;
            } catch (IOException e) {
                msg = "Error creating file: " + e.getLocalizedMessage();
                responseData.setErrorCode(403);
                responseData.setDescription(msg);
                responseData.setStatus(0);
                return responseData;
            }
        }
    }

    //endregion

    //region UPDATE LISTS

    private synchronized static ResponseData updateDemoCart(Context context, int id) {
        Log.e(TAG, "updateDemoCart: ");

        ResponseData responseData = new ResponseData();
        String msg = "Success";

        File f = new File(context.getExternalFilesDir(null), FILE_NAME_CART);
        try {
            JSONObject newData = ProductListsManager.getInstance().getJsonCartList();
            Object obj = new JsonParser().parse(new FileReader(f));

            JSONObject data = new JSONObject(obj.toString());
            JSONArray array = data.getJSONArray("users");
            for (int i = 0; i < array.length(); i++) {
                JSONObject savedData = array.getJSONObject(i);
                if (id == savedData.getInt("id")) {
                    array.put(i, newData);
                    data.put("users", array);

                    PrintWriter writer = new PrintWriter(f);
                    writer.write("");
                    writer.write(data.toString());
                    writer.close();

                    responseData.setDescription(msg);
                    responseData.setErrorCode(200);
                    responseData.setStatus(0);
                    return responseData;
                }
            }
            array.put(newData);
            data.put("users", array);

            PrintWriter writer = new PrintWriter(f);
            writer.write("");
            writer.write(data.toString());
            writer.close();

            responseData.setDescription(msg);
            responseData.setErrorCode(200);
            responseData.setStatus(0);
            return responseData;
        } catch (FileNotFoundException e) {
            msg = "Error opening file: " + e.getLocalizedMessage();
            responseData.setDescription(msg);
            responseData.setErrorCode(402);
            responseData.setStatus(0);
            return responseData;
        } catch (JSONException e) {
            msg = "Error parsing content from file: " + e.getLocalizedMessage();
            responseData.setDescription(msg);
            responseData.setErrorCode(403);
            responseData.setStatus(0);
            return responseData;
        }
    }

    private synchronized static ResponseData updateDemoWishList(Context context, int id) {
        ResponseData responseData = new ResponseData();
        String msg = "Success";

        File f = new File(context.getExternalFilesDir(null), FILE_NAME_FAVORITES);
        try {
            JSONObject newData = ProductListsManager.getInstance().getJsonWishList();

            FileInputStream fis = new FileInputStream(f);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String buffer = "";
            String line;
            while((line = reader.readLine()) != null) {
                buffer += line;
            }

            JSONObject data = new JSONObject(buffer);
            JSONArray array = data.getJSONArray("users");
            for (int i = 0; i < array.length(); i++) {
                JSONObject savedData = array.getJSONObject(i);
                if (id == savedData.getInt("id")) {
                    array.put(i, newData);
                    data.put("users", array);

                    PrintWriter writer = new PrintWriter(f);
                    writer.write("");
                    writer.write(data.toString());
                    writer.close();

                    responseData.setDescription(msg);
                    responseData.setErrorCode(200);
                    responseData.setStatus(0);
                    return responseData;
                }
            }

            array.put(newData);
            data.put("users", array);

            PrintWriter writer = new PrintWriter(f);
            writer.write("");
            writer.write(data.toString());
            writer.close();

            responseData.setDescription(msg);
            responseData.setErrorCode(200);
            responseData.setStatus(0);
            return responseData;
        } catch (FileNotFoundException e) {
            msg = "Error opening file: " + e.getLocalizedMessage();
            responseData.setDescription(msg);
            responseData.setErrorCode(402);
            responseData.setStatus(0);
            return responseData;
        } catch (JSONException e) {
            msg = "Error parsing content from file: " + e.getLocalizedMessage();
            responseData.setDescription(msg);
            responseData.setErrorCode(403);
            responseData.setStatus(0);
            return responseData;
        } catch (IOException e) {
            Log.e(TAG, "Error reading file: " + e.getLocalizedMessage() );
            responseData.setErrorCode(400);
            return responseData;
        }
    }

    //endregion

    //region DEMO_PRODUCTS

    /**
     * Method to load the data existent in the server's database.
     *
     * @param context is the Context in which data will be loaded
     * @return response is the carrier of the database data bundle
     */
    private synchronized static ResponseData loadDemoProductList(Context context) {
        ResponseData response = new ResponseData();

        InputStream raw = context.getResources().openRawResource(R.raw.base_json);
        BufferedReader rd = new BufferedReader(new InputStreamReader(raw));

        String jsonData = "";
        String line;
        try {
            while ((line = rd.readLine()) != null) {
                jsonData += line;
            }

            JSONObject data = new JSONObject(jsonData);

            response.setStatus(ResponseData.STATUS_CODE_SUCCESS);
            response.setDescription("Success");
            response.setErrorCode(ResponseData.ERROR_CODE_SUCCESS);
            response.setResponse(data);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error reading JSON data: " + e.getLocalizedMessage());
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing response: " + e.getLocalizedMessage());
        }

        return response;
    }

    //endregion

    //region ORDER CREATION

    private static ResponseData<Error> placeDemoOrder(Context context, JSONObject jsonOrder) {
        File file = new File(context.getExternalFilesDir(null), FILE_ORDERS);
        if (!file.exists()) {
            int errorCode;
            try {
                file.createNewFile();
                file.setReadable(true);
                file.setWritable(true);

                JSONObject orderList = new JSONObject();
                orderList.put("orders", new JSONArray());

                PrintWriter writer = new PrintWriter(file);
                writer.write("");
                writer.write(orderList.toString());
                writer.close();

                ResponseData<Error> response = new ResponseData<Error>();
                response.setErrorCode(200);
                response.setDescription("Order placed successfully");
                response.setStatus(0);
                return response;
            } catch (IOException e) {
                Log.e(TAG, "Error creating file: " + e.getLocalizedMessage());
                errorCode = 101;
            } catch (JSONException e) {
                Log.e(TAG, "Error initializing orderList: " + e.getLocalizedMessage());
                errorCode = 102;
            }

            ResponseData<Error> response = new ResponseData<Error>();
            response.setErrorCode(errorCode);
            response.setStatus(0);
            response.setDescription("An error occurred. Retry");

            return response;
        } else {
            int errorCode;
            try {
                FileInputStream fis = new FileInputStream(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(fis));

                String buffer = "";
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer += line;
                }

                JSONObject jsonOrders = new JSONObject(buffer);
                JSONArray jsonOrderInstanceArray = jsonOrders.getJSONArray("orders");

                jsonOrderInstanceArray.put(jsonOrder);
                jsonOrders.put("orders", jsonOrderInstanceArray);

                PrintWriter writer = new PrintWriter(file);
                writer.write("");
                writer.write(jsonOrders.toString());
                writer.close();

                ResponseData<Error> response = new ResponseData<Error>();
                response.setErrorCode(200);
                response.setDescription("Order placed successfully");
                response.setStatus(0);

                return response;
            } catch (IOException e) {
                Log.e(TAG, "Error reading from file: " + e.getLocalizedMessage());
                errorCode = 101;
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing to JSON: " + e.getLocalizedMessage());
                errorCode = 102;
            }

            ResponseData<Error> response = new ResponseData<Error>();
            response.setErrorCode(errorCode);
            response.setDescription("An error occurred. Retry");
            response.setStatus(0);

            return response;
        }
    }

    //endregion

    private static int createDemoId(JSONArray array) {
        int id = new Random().nextInt(100);
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject user = array.getJSONObject(i);
                if (id == user.getInt("id")) {
                    return createDemoId(array);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "Error reading users: " + e.getLocalizedMessage());
            }
        }

        return id;
    }
}
