/*
 * Copyright © 2018-present, MNK Group. All rights reserved.
 */

package com.themedimension.ivoryshop.android.models;

import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

import static android.content.ContentValues.TAG;

public final class ProductSize {

    //region GLOBALS

    @SerializedName("id")
    private int sizeId;
    @SerializedName("name")
    private String sizeName;
    @SerializedName("quantity")
    private int sizeQuantity;

    //endregion

    public ProductSize(int sizeId, String sizeName, int sizeQuantity) {
        this.sizeId = sizeId;
        this.sizeName = sizeName;
        this.sizeQuantity = sizeQuantity;
    }

    public int getSizeId() {
        return sizeId;
    }

    public String getSizeName() {
        return sizeName;
    }

    public int getSizeQuantity() {
        return sizeQuantity;
    }

    public void setSizeQuantity(int sizeQuantity) {
        this.sizeQuantity = sizeQuantity;
    }

    @Nullable
    public static ProductSize createFromJsonObject(JSONObject object) {
        try {
            int id = object.getInt("id");
            String name = object.getString("name");
            int quantity = object.getInt("quantity");

            return new ProductSize(id, name, quantity);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "Error creating size from JSON: " + e.getLocalizedMessage());
        }

        return null;
    }
}
