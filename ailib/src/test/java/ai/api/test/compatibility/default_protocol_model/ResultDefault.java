package ai.api.test.compatibility.default_protocol_model;

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

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.HashMap;

public class ResultDefault implements Serializable {

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
    private MetadataDefault metadata;

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

    public MetadataDefault getMetadata() {
        return metadata;
    }

    public void setMetadata(final MetadataDefault metadata) {
        this.metadata = metadata;
    }

    public HashMap<String, JsonElement> getParameters() {
        return parameters;
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

    @Override
    public String toString() {
        return String.format("Result {speech='%s', action='%s', resolvedQuery='%s'}",
                speech,
                action,
                resolvedQuery);
    }
}
