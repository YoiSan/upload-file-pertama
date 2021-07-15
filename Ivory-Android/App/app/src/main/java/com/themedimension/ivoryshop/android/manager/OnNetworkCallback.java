/*
 * Copyright © 2018-present, MNK Group. All rights reserved.
 */
package com.themedimension.ivoryshop.android.manager;

/**
 * Interface to declare the behaviour of the app-server communication
 * when the response has been received by the app.
 */
public interface OnNetworkCallback {
    void onSuccess(Object data, ResponseExtractor.ResponseType type);

    void onFailed(Object data, ResponseExtractor.ResponseType type);
}
