/*
 * Copyright © 2018-present, MNK Group. All rights reserved.
 */

package com.themedimension.ivoryshop.android.AsyncCardCharge.ServerCommunication;

/**
 * Interface to declare behaviour on Server response.
 */

public interface ServiceHandler {
    int POST = 1;
    int GET = 0;

    void makeServiceCall(String url, int method);
}
