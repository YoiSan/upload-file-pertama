/*
 * Copyright © 2018-present, MNK Group. All rights reserved.
 */
package com.themedimension.ivoryshop.android.manager;

/**
 * Class to check for the type of response the app receives from the server.
 * <p>
 * It can also be extended to provide other types of response such as LOGIN
 * or REGISTER.
 *
 * @see NetworkManager
 */
public class ResponseExtractor {

    //  Enum of response types supported by the app.
    public enum ResponseType {
        DEMO, PRODUCT_LIST, NEW_USER, RESET_PASSWORD, UPDATE, WISHLIST, UPDATE_WISHLIST, UPDATE_CART, CARTLIST, PLACE_ORDER, EXISTING_USER
    }

}
