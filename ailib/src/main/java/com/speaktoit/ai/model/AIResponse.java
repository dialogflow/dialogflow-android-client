package com.speaktoit.ai.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

public class AIResponse implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("resolvedQuery")
    private String resolvedQuery;

    @SerializedName("timestamp")
    private Date timestamp;

    @SerializedName("result")
    private Result result;

    @SerializedName("status")
    private Status status;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getResolvedQuery() {
        return resolvedQuery;
    }

    public void setResolvedQuery(final String resolvedQuery) {
        this.resolvedQuery = resolvedQuery;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final Date timestamp) {
        this.timestamp = timestamp;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(final Result result) {
        this.result = result;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }

    public boolean isError() {
        if (status != null && status.getCode() != null && status.getCode() >= 400) {
            return true;
        }

        return false;
    }
}
