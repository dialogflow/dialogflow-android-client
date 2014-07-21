package com.speaktoit.ai.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Status implements Serializable {

    @SerializedName("code")
    private Integer code;

    @SerializedName("errorType")
    private String errorType;

    @SerializedName("errorDetails")
    private String errorDetails;

    @SerializedName("errorID")
    private String errorID;

    public Integer getCode() {
        return code;
    }

    public void setCode(final Integer code) {
        this.code = code;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(final String errorType) {
        this.errorType = errorType;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(final String errorDetails) {
        this.errorDetails = errorDetails;
    }

    public String getErrorID() {
        return errorID;
    }

    public void setErrorID(final String errorID) {
        this.errorID = errorID;
    }
}
