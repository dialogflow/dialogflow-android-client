package com.speaktoit.ai.model;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.HashMap;

public class Result implements Serializable {

    @SerializedName("speech")
    private String speech;

    @SerializedName("action")
    private String action;

    /**
     * This field will be deserialized as hashMap container with all parameters and it's values
     */
    @SerializedName("parameters")
    private HashMap<String, JsonElement> parameters;

    @SerializedName("metadata")
    private Metadata metadata;

    public String getSpeech() {
        return speech;
    }

    public void setSpeech(final String speech) {
        this.speech = speech;
    }

    public String getAction() {
        return action;
    }

    public void setAction(final String action) {
        this.action = action;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(final Metadata metadata) {
        this.metadata = metadata;
    }

    public HashMap<String, JsonElement> getParameters() {
        return parameters;
    }
}
