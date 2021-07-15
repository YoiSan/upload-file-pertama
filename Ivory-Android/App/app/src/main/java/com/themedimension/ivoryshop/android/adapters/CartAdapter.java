/*
 * Copyright © 2018-present, MNK Group. All rights reserved.
 */
package com.themedimension.ivoryshop.android.adapters;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.themedimension.ivoryshop.android.R;
import com.themedimension.ivoryshop.android.activities.checkout.CartActivity;
import com.themedimension.ivoryshop.android.manager.NetworkManager;
import com.themedimension.ivoryshop.android.manager.OnNetworkCallback;
import com.themedimension.ivoryshop.android.manager.ProductListsManager;
import com.themedimension.ivoryshop.android.manager.ResponseExtractor;
import com.themedimension.ivoryshop.android.utils.Utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class CartAdapter extends ArrayAdapter<ProductListsManager.CartItem> {

    //region GLOBALS

    private ArrayList<ProductListsManager.CartItem> productList = new ArrayList<>();
    private Activity parentActivity;
    private NetworkManager networkManager;

    public CartAdapter(Activity context, int textViewResourceId, ArrayList<ProductListsManager.CartItem> objects) {
        super(context, textViewResourceId, objects);
        this.productList = objects;
        this.parentActivity = context;
        networkManager = new NetworkManager(parentActivity);
    }

    //endregion

    /***
     *Method that removes a certain product from the checkoutProductList .
     */
    @Override
    public void remove(@Nullable ProductListsManager.CartItem object) {
        super.remove(object);
        notifyDataSetChanged();
        ProductListsManager.getInstance().removeCartItem(object);
        ((CartActivity) parentActivity).updateTotalPrice();
    }

    @Override
    public int getCount() {
        return this.productList.size();
    }

    /**
     * * A holder is used to speed up rendering of the GridView.
     * After taking an ArrayList, create a View class to get the layout and a
     * holder that holds the views of that layout. Get the object of the
     * arraylist into the object of the ListView and set the values accordingly.
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
            holder = new Holder();
            convertView = LayoutInflater.from(parentActivity).inflate(R.layout.cart_item, parent, false);

            holder.mainProductName = (TextView) convertView.findViewById(R.id.cartNameProduct);
            holder.mainProductImage = (ImageView) convertView.findViewById(R.id.cartImageProduct);
            holder.mainProductPrice = (TextView) convertView.findViewById(R.id.cartPriceProduct);
            holder.mainProductQuantity = (TextView) convertView.findViewById(R.id.cartQuantityProduct);
            holder.mainProductColor = (ImageView) convertView.findViewById(R.id.cartColorProduct);
            holder.mainProductSize = (TextView) convertView.findViewById(R.id.cartSizeProduct);
            holder.mainProductBin = (ImageView) convertView.findViewById(R.id.cartBinImageProduct);

            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        final ProductListsManager.CartItem cartItem = productList.get(position);

        if (cartItem != null) {
            holder.mainProductName.setText(cartItem.getName());
            if (NetworkManager.DEMO_DATA) {
                try {
                    InputStream is = parentActivity.getAssets().open(cartItem.getImage());
                    Drawable d = Drawable.createFromStream(is, null);
                    holder.mainProductImage.setImageDrawable(d);
                    holder.mainProductImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Picasso.with(parentActivity)
                        .load(cartItem.getImage())
                        .into(holder.mainProductImage);
            }

            holder.mainProductPrice.setText(parentActivity.getResources().getString(R.string.dollar_currency_string) + "" + cartItem.getPrice());
            holder.mainProductQuantity.setText(cartItem.getQuantity() + " " + (cartItem.getQuantity() > 1 ? "pcs" : "pc"));
            holder.mainProductColor.setBackgroundColor(Color.parseColor(cartItem.getColor()));

            // Item Color shape
            GradientDrawable gd1 = new GradientDrawable();
            gd1.setColor(Color.parseColor(cartItem.getColor()));
            gd1.setCornerRadius(100);
            gd1.setStroke(2, Color.WHITE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                holder.mainProductColor.setBackground(gd1);
            }

            // Item Size shape
            GradientDrawable gd2 = new GradientDrawable();
            gd2.setColor(Color.BLACK);
            gd2.setCornerRadius(100);
            gd2.setStroke(2, Color.BLACK);
            holder.mainProductSize.setText(cartItem.getSize().getSizeName());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                holder.mainProductSize.setBackground(gd2);
            }

            holder.mainProductBin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    networkManager.setListener(new OnNetworkCallback() {
                        @Override
                        public void onSuccess(Object data, ResponseExtractor.ResponseType type) {
                            remove(cartItem);
                        }

                        @Override
                        public void onFailed(Object data, ResponseExtractor.ResponseType type) {
                            Utils.showSingleButtonAlertWithoutTitle(parentActivity, parentActivity.getResources().getString(R.string.could_not_remove_item));
                        }
                    });
                    networkManager.updateCart(parentActivity);
                }
            });
        }
        return convertView;
    }

    private Drawable decodeFile(InputStream in) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
            InputStream is1 = new ByteArrayInputStream(baos.toByteArray());
            InputStream is2 = new ByteArrayInputStream(baos.toByteArray());

            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is1, null, o);

            final int IMAGE_MAX_SIZE = 100;

            System.out.println("h:" + o.outHeight + " w:" + o.outWidth);
            int scale = 1;
            if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
                scale = (int) Math.pow(2, (int) Math.round(Math.log(IMAGE_MAX_SIZE /
                        (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return new BitmapDrawable(parentActivity.getResources(), BitmapFactory.decodeStream(is2, null, o2));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get the corect item.
     * // * @param userEmail
     */
    public void updateSource() {
        this.productList = ProductListsManager.getInstance().getCartItems();
        notifyDataSetChanged();
    }

    private class Holder {
        TextView mainProductName;
        ImageView mainProductImage;
        TextView mainProductPrice;
        TextView mainProductQuantity;
        ImageView mainProductColor;
        TextView mainProductSize;
        ImageView mainProductBin;
    }
}
