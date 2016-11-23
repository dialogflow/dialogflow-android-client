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

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

import java.util.List;

import ai.api.android.AIConfiguration;
import ai.api.AIListener;
import ai.api.android.AIService;
import ai.api.AIServiceException;
import ai.api.PartialResultsListener;
import ai.api.R;
import ai.api.RequestExtras;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.services.GoogleRecognitionServiceImpl;

public class AIButton extends SoundLevelButton implements AIListener {

    public interface AIButtonListener {
        void onResult(final AIResponse result);
        void onError(final AIError error);
        void onCancelled();
    }

    private static final String TAG = AIButton.class.getName();

    protected static final int[] STATE_WAITING = {R.attr.state_waiting};
    protected static final int[] STATE_SPEAKING = {R.attr.state_speaking};
    protected static final int[] STATE_INITIALIZING_TTS = {R.attr.state_initializing_tts};

    private float animationStage = 0;
    private boolean animationSecondPhase = false;
    private final WaitingAnimation animation = new WaitingAnimation();

    private AIService aiService;
    private AIButtonListener resultsListener;
    private PartialResultsListener partialResultsListener;

    @Override
    public void onResult(final AIResponse result) {
        post(new Runnable() {
            @Override
            public void run() {
                changeState(MicState.normal);
            }
        });

        if (resultsListener != null) {
            resultsListener.onResult(result);
        }
    }

    @Override
    public void onError(final AIError error) {
        post(new Runnable() {
            @Override
            public void run() {
                changeState(MicState.normal);
            }
        });

        if (resultsListener != null) {
            resultsListener.onError(error);
        }
    }

    @Override
    public void onAudioLevel(final float level) {
        setSoundLevel(level);
    }

    @Override
    public void onListeningStarted() {
        post(new Runnable() {
            @Override
            public void run() {
                changeState(MicState.listening);
            }
        });
    }

    @Override
    public void onListeningCanceled() {
        post(new Runnable() {
            @Override
            public void run() {
                changeState(MicState.normal);
            }
        });

        if (resultsListener != null) {
            resultsListener.onCancelled();
        }
    }

    @Override
    public void onListeningFinished() {
        post(new Runnable() {
            @Override
            public void run() {
                changeState(MicState.busy);
            }
        });
    }

    public enum MicState {
        normal,
        busy, // transitive state with disabled mic
        listening, // state with sound indicator
        speaking,
        initializingTts;

        public static MicState fromAttrs(final TypedArray viewAttrs) {
            if (viewAttrs.getBoolean(R.styleable.SoundLevelButton_state_listening, false))
                return listening;
            if (viewAttrs.getBoolean(R.styleable.SoundLevelButton_state_waiting, false))
                return busy;
            if (viewAttrs.getBoolean(R.styleable.SoundLevelButton_state_speaking, false))
                return speaking;
            if (viewAttrs.getBoolean(R.styleable.SoundLevelButton_state_initializing_tts, false))
                return initializingTts;

            return normal;
        }
    }

    private volatile MicState currentState = MicState.normal;

    public AIButton(final Context context) {
        super(context);
        init(context, null);
    }

    public AIButton(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AIButton(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(final Context context, final AttributeSet attrs) {
        if (attrs != null) {
            final TypedArray viewAttrs = context.obtainStyledAttributes(attrs, R.styleable.SoundLevelButton);
            try {
                currentState = MicState.fromAttrs(viewAttrs);
            } finally {
                viewAttrs.recycle();
            }
        }
    }

    public void initialize(final AIConfiguration config) {
        aiService = AIService.getService(getContext(), config);
        aiService.setListener(this);

        if (aiService instanceof GoogleRecognitionServiceImpl) {
            ((GoogleRecognitionServiceImpl) aiService).setPartialResultsListener(new PartialResultsListener() {
                @Override
                public void onPartialResults(final List<String> partialResults) {
                    if (partialResultsListener != null) {
                        partialResultsListener.onPartialResults(partialResults);
                    }
                }
            });
        }
    }

    public void setResultsListener(final AIButtonListener resultsListener) {
        this.resultsListener = resultsListener;
    }

    public void setPartialResultsListener(final PartialResultsListener partialResultsListener) {
        this.partialResultsListener = partialResultsListener;
    }

    public void startListening() {
        startListening(null);
    }

    public void startListening(final RequestExtras requestExtras) {
        if (aiService != null) {
            if (currentState == MicState.normal) {
                aiService.startListening(requestExtras);
            }
        } else {
            throw new IllegalStateException("Call initialize method before usage");
        }
    }

    public AIResponse textRequest(final AIRequest request) throws AIServiceException {
        if (aiService != null) {
            return aiService.textRequest(request);
        } else {
            throw new IllegalStateException("Call initialize method before usage");
        }
    }

    public AIResponse textRequest(final String request) throws AIServiceException {
        return textRequest(new AIRequest(request));
    }

    /**
     * Get AIService object for making different data requests
     * @return
     */
    public AIService getAIService() {
        return aiService;
    }

    @Override
    protected void onClick(final View v) {
        super.onClick(v);

        if (aiService != null) {
            switch (currentState) {
                case normal:
                    aiService.startListening();
                    break;
                case busy:
                    aiService.cancel();
                    break;
                default:
                    aiService.stopListening();
                    break;
            }
        }

    }

    @Override
    public int[] onCreateDrawableState(final int extraSpace) {
        final int[] state = super.onCreateDrawableState(extraSpace + 1);
        if (currentState != null) {
            switch (currentState) {
                case normal:
                    break;
                case busy:
                    mergeDrawableStates(state, STATE_WAITING);
                    break;
                case listening:
                    mergeDrawableStates(state, STATE_LISTENING);
                    break;
                case speaking:
                    mergeDrawableStates(state, STATE_SPEAKING);
                    break;
                case initializingTts:
                    mergeDrawableStates(state, STATE_INITIALIZING_TTS);
                    break;
            }
        }
        return state;
    }

    public void resume() {
        if (aiService != null) {
            aiService.resume();
        }
    }

    public void pause() {
        cancelListening();
        if (aiService != null) {
            aiService.pause();
        }
    }

    private void cancelListening() {
        if (aiService != null) {
            if (currentState != MicState.normal) {
                aiService.cancel();
                changeState(MicState.normal);
            }
        }
    }

    protected void changeState(final MicState toState) {
        switch (toState) {
            case normal:
                stopProcessingAnimation();
                setDrawSoundLevel(false);
                break;
            case busy:
                startProcessingAnimation();
                setDrawSoundLevel(false);
                break;
            case listening:
                stopProcessingAnimation();
                setDrawSoundLevel(true);
                break;
            case speaking:
                stopProcessingAnimation();
                setDrawSoundLevel(false);
                break;
            case initializingTts:
                stopProcessingAnimation();
                setDrawSoundLevel(false);
                break;
        }

        currentState = toState;
        refreshDrawableState();
    }

    protected MicState getCurrentState() {
        return currentState;
    }

    private void startProcessingAnimation() {
        setDrawCenter(true);
        animationSecondPhase = false;
        startAnimation(animation);
    }

    private void stopProcessingAnimation() {
        setDrawCenter(false);
        clearAnimation();
        animationStage = 0;
        animationSecondPhase = false;
        postInvalidate();
    }

    @Override
    protected String getDebugState() {
        return super.getDebugState() + "\nst:" + currentState;
    }

    private class WaitingAnimation extends Animation {
        protected WaitingAnimation() {
            super();
            setDuration(1500);
            this.setRepeatCount(INFINITE);
            this.setRepeatMode(RESTART);
            this.setInterpolator(new LinearInterpolator());
        }

        @Override
        protected void applyTransformation(final float interpolatedTime, final Transformation t) {
            animationStage = interpolatedTime;
            invalidate();
        }
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        if (animationStage > 0 || animationSecondPhase) {
            final float center = getWidth() / 2f;
            final float radius = getMinRadius() * 1.25f;
            final RectF size = new RectF(center - radius, center - radius, center + radius, center + radius);
            final Paint paint = new Paint();
            paint.setColor(getResources().getColor(R.color.icon_orange_color));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dpToPixels(this.getContext(), 4));
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setAntiAlias(true);
            final float startingAngle;
            final float sweepAngle;
            if (animationStage < 0.5 && !animationSecondPhase) {
                startingAngle = 0;
                sweepAngle = animationStage * 360;
            } else {
                startingAngle = (animationStage - 0.5f) * 360;
                sweepAngle = 180;
                animationSecondPhase = true;
            }
            canvas.drawArc(size, 270f + startingAngle, sweepAngle, false, paint);
        }
    }

    private static int dpToPixels(final Context context, final float dp) {
        final Resources r = context.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }
}
