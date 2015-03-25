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
import java.util.HashMap;
import java.util.Map;

public class Status implements Serializable {

    private static final Map<Integer, String> errorDescriptions = new HashMap<>();
    private static final Map<Integer, String> errorTypes = new HashMap<>();
    {
        errorDescriptions.put(400,"Some required parameter is missing or has wrong value. Details will be in the errorDetails field.");
        errorTypes.put(400,"bad_request");

        errorDescriptions.put(401,"Authorization failed. Please check your access keys.");
        errorTypes.put(401,"unauthorized");

        errorDescriptions.put(404,"Uri is not found or some resource with provided id is not found.");
        errorTypes.put(404,"not_found");

        errorDescriptions.put(405,"Attempting to use POST with a GET-only endpoint, or vice-versa.");
        errorTypes.put(405,"not_allowed");

        errorDescriptions.put(406,"Uploaded files have some problems with it.");
        errorTypes.put(406,"not_acceptable");

        errorDescriptions.put(409,"The request could not be completed due to a conflict with the current state of the resource. This code is only allowed in situations where it is expected that the user might be able to resolve the conflict and resubmit the request.");
        errorTypes.put(409,"conflict");
    }

    public static Status fromResponseCode(final int responseCode) {
        final Status status = new Status();
        status.setCode(responseCode);
        if (errorTypes.containsKey(responseCode)) {
            status.setErrorType(errorTypes.get(responseCode));
        }

        if (errorDescriptions.containsKey(responseCode)) {
            status.setErrorDetails(errorDescriptions.get(responseCode));
        }

        return status;
    }

    /**
     * Response Status Code.
     * Possible values
     * <ul>
     * <li><b>400</b> bad_request - Some required parameter is missing or has wrong value. Details will be in the errorDetails field.</li>
     * <li><b>401</b> unauthorized - Internal authorization failed. It might mean missing or wrong credentials.</li>
     * <li><b>404</b> not_found - Returned if uri is not found or some resource with provided id is not found.</li>
     * <li><b>405</b> not_allowed - Attempting to use POST with a GET-only endpoint, or vice-versa.</li>
     * <li><b>406</b> not_acceptable - Can be returned if uploaded files have some problems with it.</li>
     * <li><b>200</b> deprecated - Can be used to indicate that some resource is deprecated and will be removed in the future.</li>
     * <li><b>409</b> conflict - The request could not be completed due to a conflict with the current state of the resource. This code is only allowed in situations where it is expected that the user might be able to resolve the conflict and resubmit the request.</li>
     * </ul>
     */
    @SerializedName("code")
    private Integer code;

    /**
     * Error type.
     * Possible values:
     * <ul>
     * <li><b>bad_request</b> - Some required parameter is missing or has wrong value. Details will be in the errorDetails field.</li>
     * <li><b>unauthorized</b> - Internal authorization failed. It might mean missing or wrong credentials.</li>
     * <li><b>not_found</b> - Returned if uri is not found or some resource with provided id is not found.</li>
     * <li><b>not_allowed</b> - Attempting to use POST with a GET-only endpoint, or vice-versa.</li>
     * <li><b>not_acceptable</b> - Can be returned if uploaded files have some problems with it.</li>
     * <li><b>deprecated</b> - Can be used to indicate that some resource is deprecated and will be removed in the future.</li>
     * <li><b>conflict</b> - The request could not be completed due to a conflict with the current state of the resource. This code is only allowed in situations where it is expected that the user might be able to resolve the conflict and resubmit the request.</li>
     * </ul>
     */
    @SerializedName("errorType")
    private String errorType;

    /**
     * Human readable error description.
     */
    @SerializedName("errorDetails")
    private String errorDetails;

    /**
     * Error unique ID. Use it in the requests to API.AI support.
     */
    @SerializedName("errorID")
    private String errorID;

    /**
     * Response Status Code.
     * Possible values:
     * <ul>
     * <li><b>400</b> bad_request - Some required parameter is missing or has wrong value. Details will be in the errorDetails field.</li>
     * <li><b>401</b> unauthorized - Internal authorization failed. It might mean missing or wrong credentials.</li>
     * <li><b>404</b> not_found - Returned if uri is not found or some resource with provided id is not found.</li>
     * <li><b>405</b> not_allowed - Attempting to use POST with a GET-only endpoint, or vice-versa.</li>
     * <li><b>406</b> not_acceptable - Can be returned if uploaded files have some problems with it.</li>
     * <li><b>200</b> deprecated - Can be used to indicate that some resource is deprecated and will be removed in the future.</li>
     * <li><b>409</b> conflict - The request could not be completed due to a conflict with the current state of the resource. This code is only allowed in situations where it is expected that the user might be able to resolve the conflict and resubmit the request.</li>
     * </ul>
     */
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
        if (code != null && errorDescriptions.containsKey(code)) {
            return errorDescriptions.get(code);
        }

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

    @Override
    public String toString() {
        return String.format("Status{code=%d, errorType='%s', errorDetails='%s'}",
                code,
                errorType,
                errorDetails);
    }
}
