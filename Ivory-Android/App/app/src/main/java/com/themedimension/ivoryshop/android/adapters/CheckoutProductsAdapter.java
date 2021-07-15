/*
 * Copyright © 2018-present, MNK Group. All rights reserved.
 */
package com.themedimension.ivoryshop.android.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.themedimension.ivoryshop.android.R;
import com.themedimension.ivoryshop.android.manager.ProductListsManager;

import java.util.ArrayList;

public class CheckoutProductsAdapter extends RecyclerView.Adapter<CheckoutProductsAdapter.ProductsHolder> {

    //region GLOBALS
    private ArrayList<ProductListsManager.CartItem> productListCheckout = new ArrayList<ProductListsManager.CartItem>();
    private Context context;

    public CheckoutProductsAdapter(@NonNull Context context, ArrayList<ProductListsManager.CartItem> productListCheckout) {
        this.productListCheckout = productListCheckout;
        this.context = context;
    }

    //endregion

    /**
     * * A holder is used to speed up rendering of the GridView.
     * After taking an ArrayList, create a View class to get the layout and a
     * holder that holds the views of that layout. Get the object of the
     * ArrayList into the object of the ListView and set the values accordingly.
     */
    class ProductsHolder  extends RecyclerView.ViewHolder{
        TextView productName;
        ImageView productColor;
        TextView productSize;
        TextView productQuantity;
        TextView productPrice;

        ProductsHolder(View convertView) {
            super(convertView);
            productName = (TextView) convertView.findViewById(R.id.checkout_product_name);
            productColor = (ImageView) convertView.findViewById(R.id.checkout_product_color);
            productSize = (TextView) convertView.findViewById(R.id.checkout_product_size);
            productQuantity = (TextView) convertView.findViewById(R.id.checkout_product_quantity);
            productPrice = (TextView) convertView.findViewById(R.id.checkout_product_price);
        }
    }

    @Override
    public ProductsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.checkout_list_item, parent, false);
        return new ProductsHolder(view);
    }

    @Override
    public void onBindViewHolder(ProductsHolder holder, int position) {
        ProductListsManager.CartItem cartItem = productListCheckout.get(position);
        if (cartItem != null){
            holder.productName.setText(productListCheckout.get(position).getName());
            holder.productColor.setBackgroundColor(Color.parseColor(productListCheckout.get(position).getColor()));

            GradientDrawable gd1 = new GradientDrawable();
            gd1.setColor(Color.parseColor(productListCheckout.get(position).getColor()));
            gd1.setCornerRadius(100);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                holder.productColor.setBackground(gd1);
            }

            holder.productSize.setText(productListCheckout.get(position).getSize().getSizeName());
            holder.productQuantity.setText(String.valueOf(productListCheckout.get(position).getQuantity()));
            holder.productPrice.setText(context.getResources().getString(R.string.dollar_currency_string) + "" + productListCheckout.get(position).getPrice());
        }
    }

    @Override
    public int getItemCount() {
        return productListCheckout.size();
    }


}
