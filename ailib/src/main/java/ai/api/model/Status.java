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
