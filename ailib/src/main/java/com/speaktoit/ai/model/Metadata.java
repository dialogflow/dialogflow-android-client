package com.speaktoit.ai.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Metadata implements Serializable {

    @SerializedName("intentName")
    private String intentName;

    @SerializedName("intentId")
    private String intentId;

    public String getIntentName() {
        return intentName;
    }

    public void setIntentName(final String intentName) {
        this.intentName = intentName;
    }

    public String getIntentId() {
        return intentId;
    }

    public void setIntentId(final String intentId) {
        this.intentId = intentId;
    }
}
