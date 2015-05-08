package ai.api.model;

/***********************************************************************************************************************
 *
 * API.AI Android SDK - client-side libraries for API.AI
 * =================================================
 *
 * Copyright (C) 2015 by Speaktoit, Inc. (https://www.speaktoit.com)
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
import java.util.Arrays;
import java.util.List;

public class EntityEntry implements Serializable {

    @SerializedName("value")
    private String value;

    @SerializedName("synonyms")
    private List<String> synonyms;

    public EntityEntry() {
    }

    public EntityEntry(final String value) {
        this.value = value;
    }

    public EntityEntry(final String value, final List<String> synonyms) {
        this.value = value;
        this.synonyms = synonyms;
    }

    public EntityEntry(final String value, final String[] synonyms) {
        this.value = value;
        this.synonyms = Arrays.asList(synonyms);
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public List<String> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(final List<String> synonyms) {
        this.synonyms = synonyms;
    }
}
