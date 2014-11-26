package ai.api.ui;

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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import ai.api.R;

public class SoundLevelButton extends MaskedColorView {

    private static final String TAG = SoundLevelButton.class.getName();

    protected static final int[] STATE_LISTENING = new int[] { R.attr.state_listening };

    private final SoundLevelCircleDrawable backgroundDrawable;
    protected boolean listening = false;

    @SuppressWarnings("UnusedDeclaration")
    public SoundLevelButton(@NonNull final Context context) {
        super(context);
        backgroundDrawable = new SoundLevelCircleDrawable(getParams(context, null));
        setCircleBackground(backgroundDrawable);
        init();
    }

    @SuppressWarnings("UnusedDeclaration")
    public SoundLevelButton(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
        backgroundDrawable = new SoundLevelCircleDrawable(getParams(context, attrs));
        setCircleBackground(backgroundDrawable);
        init();
    }

    @SuppressWarnings("UnusedDeclaration")
    public SoundLevelButton(@NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        backgroundDrawable = new SoundLevelCircleDrawable(getParams(context, attrs));
        setCircleBackground(backgroundDrawable);
        init();
    }

    private void init() {
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                SoundLevelButton.this.onClick(v);
            }
        });
    }

    protected void onClick(final View v) {

    }

    @Nullable
    private SoundLevelCircleDrawable.Params getParams(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        if (attrs != null) {
            final TypedArray viewAttrs = context.obtainStyledAttributes(attrs, R.styleable.SoundLevelButton);

            try {
                this.listening = viewAttrs.getBoolean(R.styleable.SoundLevelButton_state_listening, false);

                final float maxRadius = viewAttrs.getDimension(R.styleable.SoundLevelButton_maxRadius, -1);
                final float minRadius = viewAttrs.getDimension(R.styleable.SoundLevelButton_minRadius, -1);
                final float circleCenterX = viewAttrs.getDimension(R.styleable.SoundLevelButton_circleCenterX, -1);
                final float circleCenterY = viewAttrs.getDimension(R.styleable.SoundLevelButton_circleCenterY, -1);
                final int centerColor = viewAttrs.getColor(R.styleable.SoundLevelButton_centerColor, SoundLevelCircleDrawable.CENTER_COLOR_DEF);
                final int haloColor = viewAttrs.getColor(R.styleable.SoundLevelButton_haloColor, SoundLevelCircleDrawable.HALO_COLOR_DEF);
                return new SoundLevelCircleDrawable.Params(maxRadius, minRadius, circleCenterX, circleCenterY, centerColor, haloColor);
            } finally {
                viewAttrs.recycle();
            }
        }
        return null;
    }

    @Override
    public int[] onCreateDrawableState(final int extraSpace) {
        final int[] state = super.onCreateDrawableState(extraSpace + 1);
        if (listening) mergeDrawableStates(state, STATE_LISTENING);
        return state;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setCircleBackground(final SoundLevelCircleDrawable soundLevelCircleDrawable) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
            //noinspection deprecation
            setBackgroundDrawable(soundLevelCircleDrawable);
        else
            setBackground(soundLevelCircleDrawable);
    }

    public void setDrawSoundLevel(final boolean drawSoundLevel) {
        listening = drawSoundLevel;
        backgroundDrawable.setDrawSoundLevel(drawSoundLevel);
        refreshDrawableState();
        postInvalidate();
    }

    protected void setDrawCenter(final boolean drawCenter) {
        this.backgroundDrawable.setDrawCenter(drawCenter);
    }

    public void setSoundLevel(final float soundLevel) {
        backgroundDrawable.setSoundLevel(soundLevel);
        postInvalidate();
    }

    @Override
    protected String getDebugState() {
        return super.getDebugState() + "\ndrawSL: " + listening;
    }

    protected float getMinRadius() {
        return backgroundDrawable.getMinRadius();
    }
}
