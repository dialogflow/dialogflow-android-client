package ai.api.model;

/***********************************************************************************************************************
 *
 * API.AI Android SDK - client-side libraries for API.AI
 * =================================================
 *
 * Copyright (C) 2014 by Speaktoit, Inc. (https://www.speaktoit.com)
 * https://www.api.ai
 *
 ***********************************************************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 ***********************************************************************************************************************/

import android.text.TextUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

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

    /**
     * Currently active contexts
     */
    @SerializedName("contexts")
    private AIOutputContext[] contexts;


    @SerializedName("metadata")
    private Metadata metadata;

    /**
     * The query that was used to produce this result
     */
    @SerializedName("resolvedQuery")
    private String resolvedQuery;

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

    public AIOutputContext[] getContexts() {
        return contexts;
    }

    /**
     * The query that was used to produce this result
     */
    public String getResolvedQuery() {
        return resolvedQuery;
    }

    public void setResolvedQuery(final String resolvedQuery) {
        this.resolvedQuery = resolvedQuery;
    }

    void trimParameters() {
        if (parameters != null) {
            final List<String> parametersToTrim = new LinkedList<String>();
            for (final String key : parameters.keySet()) {
                final JsonElement jsonElement = parameters.get(key);
                if (jsonElement != null && jsonElement.isJsonPrimitive()) {
                    if (((JsonPrimitive) jsonElement).isString() && TextUtils.isEmpty(jsonElement.getAsString())) {
                        parametersToTrim.add(key);
                    }
                }
            }
            for (final String key : parametersToTrim) {
                parameters.remove(key);
            }
        }
    }

    @Override
    public String toString() {
        return String.format("Result {speech='%s', action='%s', resolvedQuery='%s'}",
                speech,
                action,
                resolvedQuery);
    }
}
