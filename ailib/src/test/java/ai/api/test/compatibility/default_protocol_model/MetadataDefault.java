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

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class MetadataDefault implements Serializable {

    /**
     * Name of the intent that produced this result
     */
    @SerializedName("intentName")
    private String intentName;

    /**
     * Id of the intent that produced this result
     */
    @SerializedName("intentId")
    private String intentId;

    /**
     * Currently active contexts
     */
    @SerializedName("contexts")
    private String[] contexts;

    /**
     * Name of the intent that produced this result
     */
    public String getIntentName() {
        return intentName;
    }

    public void setIntentName(final String intentName) {
        this.intentName = intentName;
    }

    /**
     * Id of the intent that produced this result
     */
    public String getIntentId() {
        return intentId;
    }

    public void setIntentId(final String intentId) {
        this.intentId = intentId;
    }

    public String[] getContexts() {
        return contexts;
    }
}
