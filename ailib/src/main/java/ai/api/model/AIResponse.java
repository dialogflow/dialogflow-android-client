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
