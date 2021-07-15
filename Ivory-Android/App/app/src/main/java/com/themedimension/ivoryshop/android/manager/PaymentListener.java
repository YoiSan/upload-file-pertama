/*
 * Copyright © 2018-present, MNK Group. All rights reserved.
 */

package com.themedimension.ivoryshop.android.manager;

public interface PaymentListener {
    void onSuccess(Object data);

    void onFailed(Object data);
}
