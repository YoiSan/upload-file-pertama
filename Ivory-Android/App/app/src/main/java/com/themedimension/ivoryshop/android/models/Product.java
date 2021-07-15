/*
 * Copyright © 2018-present, MNK Group. All rights reserved.
 */

package com.themedimension.ivoryshop.android.models;

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Product {

    //region GLOBALS

    /*  <field productId </field> is the ID assigned by the database used by te app
    *   to an instance of a product.
    */
    @SerializedName("productId")
    private int productId;

    @SerializedName("productName")
    private String productName;     //  Base name of the product instance

    @SerializedName("productColor")
    private String productColor;    //  Base color of the product instance

    @SerializedName("productGender")
    private String productGender;   //  Scope of the product instance

    @SerializedName("productPrice")
    private Double productPrice;    //  Base price of the product instance

    @SerializedName("productImage")
    private String productImage;       //  Image resource used by the product instance

    @SerializedName("sizes")
    private ArrayList<ProductSize> sizes;

    @SerializedName("productAddedDate")
    private String productAddedDate;

    @SerializedName("productCategory")
    private String category;


    /**
     * Category represents the scope of an instance of a product.
     * For further details:
     */

    public ProductSize getProductSize() {
        return productSize;
    }

    public void setProductSize(ProductSize productSize) {
        this.productSize = productSize;
    }

    private ProductSize productSize;

    //endregion

    //  Constructor used to load <object> products </object> from the server.
    public Product(String productName, Integer productId, String productColor, String productImage, Double productPrice,
                   ArrayList<ProductSize> sizes, String productCategory, String productAddedDate, String productGender) {
        this.productName = productName;
        this.productId = productId;
        this.productColor = productColor;
        this.productImage = productImage;
        this.productPrice = productPrice;
        this.sizes = sizes;
        this.category = productCategory;
        this.productAddedDate = productAddedDate;
        this.productGender = productGender;
    }

    /*  As <object> products </object> will not be volatile in accordance with user interaction,
    *   there is no need for <code> setter </code> methods.
    *
    *   Only <code> getter </code> methods will be needed in order to
    *   provide data for the user to see.
    */
    public int getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductImage() {
        return productImage;
    }

    public Double getProductPrice() {
        return productPrice;
    }

    public String getProductColor() {
        return productColor;
    }

    public String getProductAddedDate() {
        return productAddedDate;
    }

    public String getProductGender() {
        return productGender;
    }

    public String getCategory() {
        return category;
    }

    public ArrayList<ProductSize> getSizes() {
        return sizes;
    }

    public void removeSize(ProductSize size) {
        this.sizes.remove(size);
    }

    @Nullable
    public static ProductSize getProductSize(Product p, int sizeId) {
        for (ProductSize size : p.getSizes()) {
            if (size.getSizeId() == sizeId) {
                return size;
            }
        }
        return null;
    }

    @Nullable
    public ProductSize getSizeByName(String name) {
        for (ProductSize size : this.sizes) {
            if (size.getSizeName().equalsIgnoreCase(name)) {
                return size;
            }
        }
        return null;
    }



    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", productName='" + productName + '\'' +
                ", productColor='" + productColor + '\'' +
                ", productGender='" + productGender + '\'' +
                ", productPrice=" + productPrice +
                ", productImage='" + productImage + '\'' +
                ", sizes=" + sizes +
                ", productAddedDate='" + productAddedDate + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}