/*
 * Copyright © 2018-present, MNK Group. All rights reserved.
 */

package com.themedimension.ivoryshop.android.AsyncCardCharge.ServerCommunication;

import com.stripe.model.Charge;

/**
 * Interface to declare behaviour on a successful Charge object creation.
 */
public interface CreateSourceSuccessCallback {
    void successCallbackReceived(Charge c);
}
