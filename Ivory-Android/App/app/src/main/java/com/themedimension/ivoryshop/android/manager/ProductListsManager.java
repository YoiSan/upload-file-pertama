/*
 * Copyright © 2018-present, MNK Group. All rights reserved.
 */

package com.themedimension.ivoryshop.android.manager;

import android.support.annotation.Nullable;
import android.util.Log;

import com.themedimension.ivoryshop.android.models.Product;
import com.themedimension.ivoryshop.android.models.ProductSize;
import com.themedimension.ivoryshop.android.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.mikepenz.iconics.Iconics.TAG;

public class ProductListsManager {

    //region GLOBALS

    private static ProductListsManager mInstance = new ProductListsManager();

    private ArrayList<Product> mProducts = new ArrayList<Product>();
    private ArrayList<CartItem> mCartItems = new ArrayList<CartItem>();
    private double totalPrice;
    private ArrayList<Integer> mWishlistProductIds = new ArrayList<Integer>();

    public class CartItem {
        private Product product;
        private ProductSize size;
        private int quantity;
        private double price;

        public CartItem(Product product, @Nullable ProductSize size, int quantity) {
            this.product = product;
            this.size = size;
            this.quantity = quantity;
            this.price = product.getProductPrice();
        }

        public void add(int quantity) {
            this.quantity += quantity;
        }

        //region GETTERS

        public int getId() {
            return this.product.getProductId();
        }

        public String getName() {
            return this.product.getProductName();
        }

        public String getColor() {
            return this.product.getProductColor();
        }

        public String getGender() {
            return this.product.getProductGender();
        }

        public String getImage() {
            return this.product.getProductImage();
        }

        public ProductSize getSize() {
            return size;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getPrice() {
            return this.price;
        }

        //endregion
    }

    //endregion

    private ProductListsManager() {
    }

    public synchronized static ProductListsManager getInstance() {
        if (mInstance == null) {
            mInstance = new ProductListsManager();
        }

        return mInstance;
    }

    //region SETTERS

    public synchronized void setShopProducts(JSONArray products) {
        if (mProducts == null) {
            mProducts = new ArrayList<Product>();
        }

        try {
            for (int i = 0; i < products.length(); i++) {
                JSONObject jsonProduct = products.getJSONObject(i);

                int id = jsonProduct.getInt("productId");
                String color = jsonProduct.getString("productColor");
                String image = jsonProduct.getString("productImage");
                String name = jsonProduct.getString("productName");
                double price = jsonProduct.getDouble("productPrice");
                String gender = jsonProduct.getString("productGender");
                String category = jsonProduct.getString("productCategory");
                String addedDate = jsonProduct.getString("productAddedDate");

                ArrayList<ProductSize> sizes = new ArrayList<ProductSize>();
                JSONArray jsonSizes = jsonProduct.getJSONArray("sizes");
                for (int j = 0; j < jsonSizes.length(); j++) {
                    JSONObject jsonSize = jsonSizes.getJSONObject(j);
                    int sizeId = jsonSize.getInt("id");
                    String sizeName = jsonSize.getString("name");
                    int sizeQuantity = jsonSize.getInt("quantity");

//                    if (sizeQuantity != 0) {
                        sizes.add(new ProductSize(sizeId, sizeName, sizeQuantity));
//                    }
                }

                mProducts.add(new Product(name, id, color, image, price, sizes, category, addedDate, gender));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing data: " + e.getLocalizedMessage());
        }
    }

    public synchronized void setWishlist(JSONArray ids) {
        mWishlistProductIds = new ArrayList<>();

        if(ids.length() == 0) {
            return;
        }

        for (int i = 0; i < ids.length(); i++) {
            try {
                if(ids.getInt(i) < 0) {
                    continue;
                }
                mWishlistProductIds.add(ids.getInt(i));
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing JSON: " + e.getLocalizedMessage());
            }
        }
    }

    public synchronized void setCart(JSONArray array) {
        if (mCartItems == null) {
            mCartItems = new ArrayList<CartItem>();
        }

        if(array.length() == 0) {
            return;
        }

        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject object = array.getJSONObject(i);
                int id = object.getInt("id");
                if(id < 0) {
                    continue;
                }
                Product p = getProductById(id);
                int sizeId = object.getInt("sizeId");
                ProductSize s = Product.getProductSize(p, sizeId);
                int quantity = object.getInt("quantity");

                if (p != null && s != null) {
                    CartItem newItem = new CartItem(
                            p,
                            s,
                            quantity
                    );
                    mCartItems.add(newItem);
                } else {
                    Log.e(TAG, "Parse problem encountered");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "Parse problems encountered: " + e.getLocalizedMessage());
            }
        }
    }

    public synchronized void resetCart() {
        mCartItems = new ArrayList<CartItem>();
    }

    //endregion

    //region ADD TO LIST

    public synchronized void addItemCart(Product product, ProductSize size, int quantity) {
        CartItem newItem = new CartItem(product, size, quantity);
        mCartItems.add(newItem);
//        for (CartItem c : mCartItems) {
//            if (c.getId() == product.getProductId() && size.getSizeId() == c.getSize().getSizeId()) {
//                c.add(quantity);
//                return;
//            }
        }

//        mCartItems.add(newItem);
//    }

    public synchronized void addItemWishlist(int id) {
        mWishlistProductIds.add(id);

    }

    //endregion

    //region REMOVE FROM LIST

    public synchronized void removeCartItem(CartItem cartItem) {
        for (CartItem item : mCartItems) {
            if (cartItem == item) {
                mCartItems.remove(item);
                break;
            }
        }
    }

    public synchronized void removeWishlistItemById(int id) {
        for (Integer i : mWishlistProductIds) {
            if (i == id) {
                mWishlistProductIds.remove(i);
                break;
            }
        }
    }

    //endregion

    //region CONTENT TESTS

    public synchronized boolean wishlistContains(int id) {
        for (Integer i : mWishlistProductIds) {
            if (i == id) {
                return true;
            }
        }

        return false;
    }

    public synchronized boolean cartContains(int id) {
        for (CartItem item : mCartItems) {
            if (item.getId() == id) {
                return true;
            }
        }

        return false;
    }

    //endregion

    //region GETTERS

    public ArrayList<Product> getProducts() {
        return mProducts;
    }

    public ArrayList<CartItem> getCartItems() {
        return mCartItems;
    }

    public ArrayList<Integer> getWishlistProductIds() {
        return mWishlistProductIds;
    }

    public ArrayList<Product> getFavoriteProducts() {
        ArrayList<Product> newList = new ArrayList<Product>();
        for (int i : mWishlistProductIds) {
            for (Product p : mProducts) {
                if (p.getProductId() == i) {
                    newList.add(p);
                    break;
                }
            }
        }

        return newList;
    }

    @Nullable
    public synchronized Product getProductById(int id) {
        for (Product p : mProducts) {
            if (p.getProductId() == id) {
                return p;
            }
        }

        return null;
    }

    public synchronized ArrayList<Product> getShoes() {
        ArrayList<Product> list = new ArrayList<Product>();
        for (Product p : mProducts) {
            if (p.getCategory().equalsIgnoreCase("Shoes")) {
                list.add(p);
            }
        }

        return list;
    }

    public synchronized ArrayList<Product> getOther() {
        ArrayList<Product> list = new ArrayList<Product>();
        for (Product p : mProducts) {
            if (p.getCategory().equalsIgnoreCase("Other") || p.getCategory().equalsIgnoreCase("Accessories")) {
                list.add(p);
            }
        }

        return list;
    }

    public synchronized ArrayList<Product> getFavClothes() {
        ArrayList<Product> list = new ArrayList<Product>();
        for (Product p : getFavoriteProducts()) {
            if (p.getCategory().equalsIgnoreCase("Clothes")) {
                list.add(p);
            }
        }

        return list;
    }

    public synchronized ArrayList<Product> getFavShoes() {
        ArrayList<Product> list = new ArrayList<Product>();
        for (Product p : getFavoriteProducts()) {
            if (p.getCategory().equalsIgnoreCase("Shoes")) {
                list.add(p);
            }
        }

        return list;
    }

    public synchronized ArrayList<Product> getFavOther() {
        ArrayList<Product> list = new ArrayList<Product>();
        for (Product p : getFavoriteProducts()) {
            if (p.getCategory().equalsIgnoreCase("Other") || p.getCategory().equalsIgnoreCase("Accessories")) {
                list.add(p);
            }
        }

        return list;
    }

    public void clearManager(){
        mCartItems.clear();
        mWishlistProductIds.clear();
    }

    public synchronized ArrayList<Product> getClothes() {
        ArrayList<Product> list = new ArrayList<Product>();
        for (Product p : mProducts) {
            if (p.getCategory().equalsIgnoreCase("Clothes")) {
                list.add(p);
            }
        }

        return list;
    }

    //endregion

    //region PARSERS

    public synchronized JSONObject getJsonWishList() {
        JSONObject object = new JSONObject();
        JSONArray array = new JSONArray();
        for (int i : mWishlistProductIds) {
            array.put(i);
        }

        try {
            if(array.length() == 0) {
                array.put(-1);
                array.put(-1);
            }

            object.put("id", User.getInstance().getId());
            object.put("products", array);
        } catch (JSONException e) {
            Log.e(TAG, "JSON parse error: " + e.getLocalizedMessage());
        }

        return object;
    }

    public synchronized JSONObject getJsonCartList() {
        JSONObject object = new JSONObject();
        JSONArray array = new JSONArray();
        int id = User.getInstance().getId();
        try {
            for (CartItem i : mCartItems) {
                JSONObject newItem = new JSONObject();
                newItem.put("id", i.getId());
                newItem.put("sizeId", i.getSize().getSizeId());
                newItem.put("quantity", i.getQuantity());

                array.put(newItem);
            }

            if(array.length() == 0) {
                JSONObject extra = new JSONObject();
                extra.put("id", -1);
                extra.put("sizeId", -1);
                extra.put("quantity", 0);
                array.put(extra);
            }
            object.put("id", id);
            object.put("products", array);
        } catch (JSONException e) {
            Log.e(TAG, "JSON parse error: " + e.getLocalizedMessage());
        }

        return object;
    }

    //endregion

    public synchronized void resetInstance() {
        mCartItems = new ArrayList<CartItem>();
        mWishlistProductIds = new ArrayList<>();
    }

    public String toString(int mode) {
        switch (mode) {
            case 1:
                return "wishlist: " + mWishlistProductIds.size();

            case 2:
                return "cart: " + mCartItems.size();

            default:
                return super.toString();
        }
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
}
