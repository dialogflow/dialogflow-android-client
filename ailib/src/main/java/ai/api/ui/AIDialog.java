package ai.api.ui;

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

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Window;
import android.widget.TextView;

import java.util.List;

import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.AIServiceException;
import ai.api.PartialResultsListener;
import ai.api.R;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;

public class AIDialog {

    private static final String TAG = AIDialog.class.getName();

    private final Context context;
    private final AIConfiguration config;

    private AIDialogListener resultsListener;
    private final Dialog dialog;
    private final AIButton aiButton;
    private final TextView partialResultsTextView;

    private final Handler handler;

    public interface AIDialogListener {
        void onResult(final AIResponse result);
        void onError(final AIError error);
        void onCancelled();
    }

    public AIDialog(final Context context, final AIConfiguration config) {
        this(context, config, R.layout.aidialog);
    }

    public AIDialog(final Context context, final AIConfiguration config, final int customLayout) {
        this.context = context;
        this.config = config;
        dialog = new Dialog(context);
        handler = new Handler(Looper.getMainLooper());

        dialog.setCanceledOnTouchOutside(true);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(customLayout);

        partialResultsTextView = (TextView) dialog.findViewById(R.id.partialResultsTextView);

        aiButton = (AIButton) dialog.findViewById(R.id.micButton);
        aiButton.initialize(config);
        setAIButtonCallback(aiButton);
    }

    public void setResultsListener(final AIDialogListener resultsListener) {
        this.resultsListener = resultsListener;
    }

    public Dialog getDialog() {
        return dialog;
    }

    public void showAndListen() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                resetControls();
                dialog.show();
                startListening();
            }
        });
    }

    public AIResponse textRequest(final AIRequest request) throws AIServiceException {
        return aiButton.textRequest(request);
    }

    public AIResponse textRequest(final String request) throws AIServiceException {
        return textRequest(new AIRequest(request));
    }

    private void resetControls() {
        if (partialResultsTextView != null) {
            partialResultsTextView.setText("");
        }
    }

    private void setAIButtonCallback(final AIButton aiButton) {
        aiButton.setResultsListener(new AIButton.AIButtonListener() {
            @Override
            public void onResult(final AIResponse result) {

                AIDialog.this.close();

                if (resultsListener != null) {
                    resultsListener.onResult(result);
                }
            }

            @Override
            public void onError(final AIError error) {
                if (resultsListener != null) {
                    resultsListener.onError(error);
                }
            }

            @Override
            public void onCancelled() {

                AIDialog.this.close();

                if (resultsListener != null) {
                    resultsListener.onCancelled();
                }
            }
        });

        aiButton.setPartialResultsListener(new PartialResultsListener() {
            @Override
            public void onPartialResults(final List<String> partialResults) {
                final String result = partialResults.get(0);
                if (!TextUtils.isEmpty(result)) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (partialResultsTextView != null) {
                                partialResultsTextView.setText(result);
                            }
                        }
                    });
                }
            }
        });

    }

    private void startListening() {
        if (aiButton != null) {
            aiButton.startListening();
        }
    }

    public void close() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
            }
        });
    }

    /**
     * Get AIService object for making different data requests
     * @return
     */
    public AIService getAIService() {
        return aiButton.getAIService();
    }

    /**
     * Disconnect aiDialog from the recognition service.
     * Use pause/resume methods when you have permanent reference to the AIDialog object in your Activity.
     * pause() call should be added to the onPause() method of the Activity.
     * resume() call should be added to the onResume() method of the Activity.
     */
    public void pause() {
        if (aiButton != null) {
            aiButton.pause();
        }
    }

    /**
     * Reconnect aiDialog to the recognition service.
     * Use pause/resume methods when you have permanent reference to the AIDialog object in your Activity.
     * pause() call should be added to the onPause() method of the Activity.
     * resume() call should be added to the onResume() method of the Activity.
     */
    public void resume() {
        if (aiButton != null) {
            aiButton.resume();
        }
    }
}
