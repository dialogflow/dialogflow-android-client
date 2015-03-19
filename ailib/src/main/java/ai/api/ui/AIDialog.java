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
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.List;

import ai.api.AIConfiguration;
import ai.api.PartialResultsListener;
import ai.api.R;
import ai.api.model.AIError;
import ai.api.model.AIResponse;

public class AIDialog {

    private static final String TAG = AIDialog.class.getName();

    private final Context context;
    private final AIConfiguration config;

    private AIDialogListener resultsListener;
    private final Dialog dialog;
    private AIButton aiButton;
    private TextView partialResultsTextView;

    private final Handler handler;

    public interface AIDialogListener {
        public void onResult(final AIResponse result);
        public void onError(final AIError error);
    }

    public AIDialog(final Context context, final AIConfiguration config) {
        this.context = context;
        this.config = config;
        dialog = new Dialog(context);
        handler = new Handler(Looper.getMainLooper());
    }

    public void setResultsListener(final AIDialogListener resultsListener) {
        this.resultsListener = resultsListener;
    }

    public void show() {
        show(R.id.micButton);
    }

    public void show(final int customLayout) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                showInUIThread(customLayout);
            }
        });
    }

    private void showInUIThread(final int customLayout) {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.aidialog);

        partialResultsTextView = (TextView) dialog.findViewById(R.id.partialResultsTextView);

        aiButton = (AIButton) dialog.findViewById(customLayout);
        aiButton.initialize(config);
        setAIButtonCallback(aiButton);

        final Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        //window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        startListening();
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
        });

        aiButton.setPartialResultsListener(new PartialResultsListener() {
            @Override
            public void onPartialResults(final List<String> partialResults) {
                final String result = partialResults.get(0);
                Log.v(TAG, "onPartialResults: " + result);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (partialResultsTextView != null) {
                            partialResultsTextView.setText(result);
                        }
                    }
                });
            }
        });

    }

    public void startListening() {
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
}
