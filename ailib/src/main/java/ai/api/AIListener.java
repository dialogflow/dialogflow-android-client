package ai.api;

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

import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;

/**
 * Listener interface for working with API.AI service. Create implementation of it and use with {@link AIService AIService}
 */
public interface AIListener {

    /**
     * Event fires when entire process finished successfully, and returns result object
     *
     * @param result the result object, contains server answer
     */
    void onResult(AIResponse result);

    /**
     * Event fires if something going wrong while recognition or access to the AI server
     *
     * @param error the error description object
     */
    void onError(AIError error);

    /**
     * Event fires every time sound level changed. Use it to create visual feedback. There is no guarantee that this method will
     * be called.
     *
     * @param level the new RMS dB value
     */
    void onAudioLevel(float level);

    /**
     * Event fires when recognition engine start listening
     */
    void onListeningStarted();

    /**
     * Event fires when recognition engine cancel listening
     */
    void onListeningCanceled();

    /**
     * Event fires when recognition engine finish listening
     */
    void onListeningFinished();
}
