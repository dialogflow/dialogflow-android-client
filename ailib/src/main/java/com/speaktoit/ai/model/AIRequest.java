package com.speaktoit.ai.model;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AIRequest extends QuestionMetadata implements Serializable {

    @SerializedName("query")
    private String[] query;

    @SerializedName("confidence")
    private float[] confidence;

    public void setQuery(final String query) {
        if (TextUtils.isEmpty(query)) {
            throw new IllegalStateException("Query must not be empty");
        }

        this.query = new String[]{query};
        confidence = null;
    }

    public void setQuery(final String[] query, final float[] confidence) {
        if (query == null) {
            throw new IllegalStateException("Query array must not be null");
        }

        if (confidence == null && query.length > 1) {
            throw new IllegalStateException("Then confidences array is null, query must be one or zero item length");
        }

        if (confidence != null && query.length != confidence.length) {
            throw new IllegalStateException("Query and confidence arrays must be equals size");
        }

        this.query = query;
        this.confidence = confidence;
    }

    public float[] getConfidence() {
        return confidence;
    }

    public void setConfidence(final float[] confidence) {
        this.confidence = confidence;
    }
}
