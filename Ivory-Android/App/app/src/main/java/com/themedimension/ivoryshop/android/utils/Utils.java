/*
 * Copyright © 2018-present, MNK Group. All rights reserved.
 */

package com.themedimension.ivoryshop.android.utils;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.view.Window;

import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.themedimension.ivoryshop.android.R;
import com.themedimension.ivoryshop.android.models.Product;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Class that stores the API keys used by the Stripe library and the URL to the app's server.
 */

public class Utils {
    //  Change these with your Stripe API publishable/secret key.
    public static final String STRIPE_PUBLISHABLE_KEY = "pk_test_kZHYJp4nKB4i2KySGhnMSLw5";
    public static final String STRIPE_SECRET_KEY = "sk_test_ajhptdIdHUQQIC3HUXIkG9QJ";

    //  Change this with the server URL in order for the app to correctly communicate with backend.
//    public static final String BASE_URL = "http://192.168.1.168:8000/";
    public static final String BASE_URL = "http://192.168.1.168:8000/";
    public static final String BASE_IMAGE_RES_URL = BASE_URL + "images/products/";
    public static final String URL_BASE_JSON = BASE_URL + "productlist";
    public static final String URL_USER_LOGIN = BASE_URL + "loginUser?email=%s&password=%s";
//    public static final String URL_USER_SIGNUP = BASE_URL + "registerUser";
    public static final String URL_USER_SIGNUP = BASE_URL + "registerUser";
    public static final String RESET_PASS_URL = BASE_URL + "resetPass?id=%d";
    public static final String UPDATE_URL = BASE_URL + "update";
    public static final String GET_CART_PRODUCTS_URL = BASE_URL + "cartProducts?id=%d";
    public static final String GET_WISHLIST_URL = BASE_URL + "favoriteProducts?id=%d";
    public static final String UPDATE_CART_URL = BASE_URL + "insertCartProducts";
    public static final String UPDATE_WISHLIST_URL = BASE_URL + "insertFavoriteProducts";
    public static final String PAY_PAL_PAYMENT_URL = BASE_URL + "paypal";
    public static final String DELIVERY_PAYMENT_URL = BASE_URL + "delivery";
    public static final String STRIPE_PAYMENT_URL = BASE_URL + "stripe";
    public static final String CUSTOM_PAYMENT_URL = BASE_URL + "custom";

    public static String TRANSACTION_CURRENCY = "USD";


    /*  Filters  */
    public static String FILTER_ALL = "All";
    public static String FILTER_WOMEN = "Women";
    public static String FILTER_MEN = "Men";
    public static String FILTER_KIDS = "Kids";

    public static final String NEWEST = "Newest";
    public static final String OLDEST = "Oldest";
    public static final String PRICE_UP = "Price up";
    public static final String PRICE_DOWN = "Price down";

    //  Change this with the correct PayPal environment: SANDBOX, NO_INTERNET, PRODUCTION.
    public static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_SANDBOX;
    //  Change this with your PayPal API key.
    public static final String CONFIG_CLIENT_ID = "AYOkioNycSbrDovJYUf97dQLc5MMfqRSzYIshDEO7dH_B8RG45GmYn3LMT0VN4xvOvFJ7dWcWIyRY44C";

    //  Method codes for the various payment methods supported by the app.
    public static final int PAYMENT_STRIPE = 100;
    public static final int PAYMENT_PAYPAL = 200;
    public static final int PAYMENT_CUSTOM = 300;
    public static final int PAYMENT_NO_PAYMENT = 400;

    public static String md5(String text) {
        final String MD5 = "MD5";

        try {
            MessageDigest digest = MessageDigest.getInstance(MD5);

            digest.update(text.getBytes());
            byte messageDigest[] = digest.digest();

            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2) {
                    h = "0" + h;
                }

                hexString.append(h);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static  int dpToPx(int dp, Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static void showSingleButtonAlert(Context context, String title, String message){
        AlertDialog.Builder builder =new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(context.getResources().getString(R.string.button_ok), null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public static void showSingleButtonAlertWithEditableButton(Context context, String title, String message, String buttonText){
        AlertDialog.Builder builder =new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(buttonText, null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public static void showSingleButtonAlertWithoutTitle(Context context, String message){
        AlertDialog.Builder builder =new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.setPositiveButton(context.getResources().getString(R.string.button_ok), null);
        AlertDialog alertDialog = builder.create();
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    public static AlertDialog getSingleButtonAlertWithoutTitle(Context context, String message, String buttonText){
        AlertDialog.Builder builder =new AlertDialog.Builder(context);
//        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(buttonText, null);
        AlertDialog alertDialog = builder.create();
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.setCancelable(false);
        alertDialog.show();
        return alertDialog;
    }

    public static ArrayList<Product> getFilteredListByGender(String gender, ArrayList<Product> listToFilter){
        ArrayList<Product> listOfFilteredProducts = new ArrayList<>();
        for (Product product : listToFilter){
            if (product.getProductGender().equals(gender)){
                listOfFilteredProducts.add(product);
            }
        }
        return listOfFilteredProducts;
    }

    public static AlertDialog showDoubleButtonAlert(Context context, String title, String message, String positiveText, String negativeText){
        AlertDialog.Builder builder =new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(positiveText, null);
        builder.setNegativeButton(negativeText, null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        return alertDialog;
    }


    public static String formatPrice(double total){
        BigDecimal bd = new BigDecimal(( total - Math.floor( total )) * 100 );
        bd = bd.setScale(4, RoundingMode.HALF_DOWN);

        if (bd.intValue() / 10 != 0 && bd.intValue() % 10 != 0){
            return new DecimalFormat("0.00").format(total);
        } else {
            return new DecimalFormat("0.0").format(total);
        }
    }

    public static String formatError(String error){
        String string = error.replace("[", "");
        string = string.replace("]", "");
        string = string.replace("\"", "");
        return string;
    }

}
