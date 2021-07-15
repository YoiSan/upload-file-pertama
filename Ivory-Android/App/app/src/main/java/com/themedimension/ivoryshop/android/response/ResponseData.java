/*
 * Copyright © 2018-present, MNK Group. All rights reserved.
 */
package com.themedimension.ivoryshop.android.response;

/**
 * Class to establish connection between the server and the app.
 * This class represents the response blueprint that the server
 * could return.
 */
public class ResponseData<T> {

    //region GLOBALS

    // TODO change values
    public static final int STATUS_CODE_FAIL = 0;
    public static final int STATUS_CODE_SUCCESS = 1;
    public static final int STATUS_CODE_IN_PROGRESS = 2;

    public static final int ERROR_CODE_FAIL = 0;
    public static final int ERROR_CODE_SUCCESS = 200;
    public static final int ERROR_CODE_INVALID_EMAIL = 2;
    public static final int ERROR_CODE_INVALID_PASS = 3;
    public static final int LOGIN = 101;
    public static final int GET_USER = 102;
    public static final int GET_CART = 103;

    private int status;
    private int errorCode;
    private String description;
    private T response;

    //endregion

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public T getResponse() {
        return response;
    }

    public void setResponse(T response) {
        this.response = response;
    }
}
