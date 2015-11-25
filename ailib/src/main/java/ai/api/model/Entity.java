package ai.api.model;

/***********************************************************************************************************************
 *
 * API.AI Android SDK - client-side libraries for API.AI
 * =================================================
 *
 * Copyright (C) 2015 by Speaktoit, Inc. (https://www.speaktoit.com)
 * https://www.api.ai
 *
 * ********************************************************************************************************************
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
import java.util.ArrayList;
import java.util.List;

/**
 * User Entity what can be uploaded to the API.AI service and override (or extend) entities, described in agent.
 * User Entities works only in specified session, and do not affect other sessions.
 */
public class Entity implements Serializable {

    @SerializedName("name")
    private String name;

    @SerializedName("entries")
    private List<EntityEntry> entries;

    @SerializedName("extend")
    private Boolean extend;

    @SerializedName("isEnum")
    private Boolean isEnum;

    public Entity() {
    }

    public Entity(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public List<EntityEntry> getEntries() {
        return entries;
    }

    public void setEntries(final List<EntityEntry> entries) {
        this.entries = entries;
    }

    public void addEntry(final EntityEntry entry) {
        if (entries == null) {
            entries = new ArrayList<>();
        }
        entries.add(entry);
    }

    public Boolean getExtend() {
        return extend;
    }

    public void setExtend(final boolean extend) {
        this.extend = extend;
    }

    public Boolean isEnum() {
        return isEnum;
    }

    public void setIsEnum(final Boolean isEnum) {
        this.isEnum = isEnum;
    }
}
