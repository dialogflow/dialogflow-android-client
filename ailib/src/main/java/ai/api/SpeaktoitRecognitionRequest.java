package ai.api;

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

import ai.api.model.QuestionMetadata;

public class SpeaktoitRecognitionRequest {

    private byte[] soundData;
    private QuestionMetadata metadata;

    public byte[] getSoundData() {
        return soundData;
    }

    public void setSoundData(final byte[] soundData) {
        this.soundData = soundData;
    }

    public QuestionMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(final QuestionMetadata metadata) {
        this.metadata = metadata;
    }
}
