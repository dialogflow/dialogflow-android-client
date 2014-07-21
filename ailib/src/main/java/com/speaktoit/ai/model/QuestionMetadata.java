package com.speaktoit.ai.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class QuestionMetadata implements Serializable {

    @SerializedName("agent_id")
    private String agentId;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("timezone")
    private String timezone;

    @SerializedName("lang")
    private String language;

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(final String agentId) {
        this.agentId = agentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(final String userId) {
        this.userId = userId;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(final String timezone) {
        this.timezone = timezone;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(final String language) {
        this.language = language;
    }
}
