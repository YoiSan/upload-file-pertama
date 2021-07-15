/*
 * Copyright © 2018-present, MNK Group. All rights reserved.
 */

package com.themedimension.ivoryshop.android.AsyncCardCharge.ServerCommunication;

import okhttp3.RequestBody;

public interface ServerRequest {
    void sendServerRequest(RequestBody body);
}
