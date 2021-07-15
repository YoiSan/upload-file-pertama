/*
 * Copyright © 2018-present, MNK Group. All rights reserved.
 */
package com.themedimension.ivoryshop.android.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.themedimension.ivoryshop.android.R;
import com.themedimension.ivoryshop.android.utils.Utils;
import com.themedimension.ivoryshop.android.activities.fragmentActivities.DrawerActivity;
import com.themedimension.ivoryshop.android.fragments.OnItemClickListener;
import com.themedimension.ivoryshop.android.manager.NetworkManager;
import com.themedimension.ivoryshop.android.manager.ProductListsManager;
import com.themedimension.ivoryshop.android.models.Product;
import com.themedimension.ivoryshop.android.models.User;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import static com.mikepenz.iconics.Iconics.TAG;
import static com.themedimension.ivoryshop.android.adapters.ProductListAdapter.FormattingTypes.FILTER_ALL;
import static com.themedimension.ivoryshop.android.adapters.ProductListAdapter.FormattingTypes.SORT_DATE_DOWN;

/**
 * The ProductListAdapter acts as a bridge between an AdapterView and the underlying data for that view.
 * The Adapter provides access to the data items. The Adapter is also responsible for making
 * a View for each item in the data set.
 */
public class ProductListAdapter extends ArrayAdapter<Product> {

    //region GLOBALS

    private ArrayList<Product> productList = new ArrayList<>();
    private OnItemClickListener itemListener;

    private Context context;

    private FormattingTypes mLastFilter = FormattingTypes.FILTER_ALL;
    private FormattingTypes mLastSort = SORT_DATE_DOWN;
    private FormattingTypes mFragmentType;

    private class Holder {
        LinearLayout parentLayout;
        TextView mainProductName;
        ImageView mainProductImage;
        TextView mainProducePrice;
        ImageView cart;
        ImageView favorite;
    }

    public enum FormattingTypes {
        SORT_PRICE_DOWN, SORT_DATE_UP, SORT_DATE_DOWN, FILTER_WOMEN, FILTER_MEN, FILTER_KIDS, FILTER_ALL, SORT_PRICE_UP,

        FRAG_ALL, FRAG_CLOTHES, FRAG_SHOES, FRAG_OTHER
    }

    //endregion

    public ProductListAdapter(Context context, int textViewResourceId, FormattingTypes fragmentType) {
        super(context, textViewResourceId);
        this.context = context;
        mFragmentType = fragmentType;
        setFragmentSource();
    }

    public void setItemListener(OnItemClickListener listener) {
        this.itemListener = listener;
    }

    @Override
    public int getCount() {
        return productList.size();
    }

    //region SORT LIST

    private void setFragmentSource() {
        switch (mFragmentType) {
            case FRAG_CLOTHES:
                productList = ProductListsManager.getInstance().getClothes();
                break;

            case FRAG_SHOES:
                productList = ProductListsManager.getInstance().getShoes();
                break;

            case FRAG_OTHER:
                productList = ProductListsManager.getInstance().getOther();
                break;

            default:
                productList = ProductListsManager.getInstance().getProducts();
        }

        if (mLastFilter == null) {
            mLastFilter = FILTER_ALL;
        }
        filter(mLastFilter);
    }

    private void filter(FormattingTypes lastFilter) {
        ArrayList<Product> newList;
        switch (lastFilter) {
            case FILTER_WOMEN:
                newList = new ArrayList<Product>();
                for (Product p : productList) {
                    if (p.getProductGender().equalsIgnoreCase("Women")) {
                        newList.add(p);
                    }
                }
                break;

            case FILTER_MEN:
                newList = new ArrayList<Product>();
                for (Product p : productList) {
                    if (p.getProductGender().equalsIgnoreCase("Men")) {
                        newList.add(p);
                    }
                }
                break;

            case FILTER_KIDS:
                newList = new ArrayList<Product>();
                for (Product p : productList) {
                    if (p.getProductGender().equalsIgnoreCase("Kids")) {
                        newList.add(p);
                    }
                }
                break;

            default:
                switch (mFragmentType) {
                    case FRAG_OTHER:
                        newList = ProductListsManager.getInstance().getOther();
                        break;

                    case FRAG_CLOTHES:
                        newList = ProductListsManager.getInstance().getClothes();
                        break;

                    case FRAG_SHOES:
                        newList = ProductListsManager.getInstance().getShoes();
                        break;

                    default:
                        newList = ProductListsManager.getInstance().getProducts();
                }
        }

        productList = newList;
        if (mLastSort == null) {
            mLastSort = SORT_DATE_DOWN;
        }
        sort(mLastSort);
    }

    private void sort(FormattingTypes sort) {
        switch (sort) {
            case SORT_DATE_DOWN:
                Collections.sort(productList, new Comparator<Product>() {
                    public int compare(Product p1, Product p2) {
                        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
                        Date product1Date = null;
                        Date product2Date = null;
                        try {
                            product1Date = sdf.parse(p1.getProductAddedDate());
                            product2Date = sdf.parse(p2.getProductAddedDate());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        return product1Date.compareTo(product2Date);
                    }
                });
                break;

            case SORT_PRICE_DOWN:
                Collections.sort(productList, new Comparator<Product>() {
                    public int compare(Product p1, Product p2) {
                        return p1.getProductPrice().compareTo(p2.getProductPrice());
                    }
                });
                break;

            case SORT_PRICE_UP:
                Collections.sort(productList, new Comparator<Product>() {
                    public int compare(Product p1, Product p2) {
                        return p2.getProductPrice().compareTo(p1.getProductPrice());
                    }
                });
                break;

            default:
                Collections.sort(productList, new Comparator<Product>() {
                    public int compare(Product p1, Product p2) {
                        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
                        Date product1Date = null;
                        Date product2Date = null;
                        try {
                            product1Date = sdf.parse(p1.getProductAddedDate());
                            product2Date = sdf.parse(p2.getProductAddedDate());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        return product2Date.compareTo(product1Date);
                    }
                });
        }

        notifyDataSetChanged();
    }

    public void setLastSort(FormattingTypes lastSort) {
        mLastSort = lastSort;
        sort(mLastSort);
    }

    public void setLastFilter(FormattingTypes filter) {
        mLastFilter = filter;
        setFragmentSource();
    }

    //endregion

    /**
     * A holder is used to speed up rendering of the GridView.
     * After taking an ArrayList, create a View class to get the layout and a
     * holder that holds the views of that layout. Get the object of the
     * arraylist into the object of the GridView and set the values accordingly.
     */
    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
            holder = new Holder();

            convertView = LayoutInflater.from(context).inflate(R.layout.widget_layout_products_gridview, parent, false);
            holder.parentLayout = (LinearLayout) convertView.findViewById(R.id.grid_view_items);
            DisplayMetrics metrics = new DisplayMetrics();
            ((DrawerActivity)context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
            holder.parentLayout.setLayoutParams(new AbsListView.LayoutParams((metrics.widthPixels / 2) - Utils.dpToPx(16, context), ViewGroup.LayoutParams.WRAP_CONTENT));
            holder.mainProductName = (TextView) convertView.findViewById(R.id.productName);
            holder.mainProductImage = (ImageView) convertView.findViewById(R.id.productImage);
            holder.mainProducePrice = (TextView) convertView.findViewById(R.id.productPrice);
            holder.cart = (ImageView) convertView.findViewById(R.id.cart);
            holder.favorite = (ImageView) convertView.findViewById(R.id.favorite);

            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        if (getCount() > position) {
            convertView.setVisibility(View.VISIBLE);
            if (ProductListsManager.getInstance().wishlistContains(productList.get(position).getProductId())) {
                holder.favorite.setImageResource(R.drawable.ic_favorite_red);
            } else {
                holder.favorite.setImageResource(R.drawable.ic_favorite_grey);
            }

            if (ProductListsManager.getInstance().cartContains(productList.get(position).getProductId())) {
                holder.cart.setImageResource(R.drawable.ic_shopping_cart_green);
            } else {
                holder.cart.setImageResource(R.drawable.ic_shopping_cart_grey);
            }

            holder.mainProductName.setText(productList.get(position).getProductName());
            holder.mainProducePrice.setText("$" + productList.get(position).getProductPrice().toString());

            /**
             *Resizing of productImage.
             */
            Log.e(TAG, Utils.BASE_IMAGE_RES_URL + productList.get(position).getProductImage());
            if (NetworkManager.DEMO_DATA) {
                try {
                    InputStream is = context.getAssets().open(productList.get(position).getProductImage());
                    Drawable d = Drawable.createFromStream(is, null);
                    holder.mainProductImage.setImageDrawable(d);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Picasso.with(context)
                        .load(Utils.BASE_IMAGE_RES_URL + productList.get(position).getProductImage())
                        .centerInside()
                        .resize(156, 132)
                        .into(holder.mainProductImage);
            }

            /**
             *Check if a product is in the favoriteProductList and changes
             * the image of the said marker when the user clicks on the favorite ImageView.
             *
             */
            final Holder finalHolder = holder;
            holder.favorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (User.getInstance().isAnon()){
                        Utils.showSingleButtonAlert(context, context.getResources().getString(R.string.user_not_logged_in), context.getResources().getString(R.string.connect_to_see_favorites));
//                        showUserNotLoggedAlert();
//                        Toast.makeText(context, "You have to be connected to add favorite products", Toast.LENGTH_LONG).show();
                    } else {
                        if (ProductListsManager.getInstance().wishlistContains(productList.get(position).getProductId())) {
                            finalHolder.favorite.setImageResource(R.drawable.ic_favorite_grey);
                            ProductListsManager.getInstance().removeWishlistItemById(productList.get(position).getProductId());
                        } else {
                            finalHolder.favorite.setImageResource(R.drawable.ic_favorite_red);
                            ProductListsManager.getInstance().addItemWishlist(productList.get(position).getProductId());
                        }
                    }

                }
            });

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemListener.onClick(productList.get(position).getProductId());
                }
            });
        } else {
            convertView.setVisibility(View.GONE);
        }
        return convertView;
    }

    private void showUserNotLoggedAlert(){
        AlertDialog.Builder builder =
                new AlertDialog.Builder(context);
        builder.setTitle("User not logged");
        builder.setMessage(context.getResources().getString(R.string.connect_to_see_favorites));
        builder.setPositiveButton("OK", null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /***
     *Retrieve data from Shared Preferences and notify the adapter of the changes that were made.
     * @param dataSource
     */
    public void setDataSource(ArrayList<Product> dataSource) {
        this.productList = dataSource;
        this.notifyDataSetChanged();
    }
}

