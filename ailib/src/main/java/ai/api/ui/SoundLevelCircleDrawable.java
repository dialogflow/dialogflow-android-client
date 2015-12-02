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

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

public class SoundLevelCircleDrawable extends Drawable {
        public static final int HALO_COLOR_DEF = Color.argb(16, 0x0, 0x0, 0x0);
        public static final int CENTER_COLOR_DEF = 0xffF26C29;
        private static final float MIN_VALUE = 1f / 2;
        private static final float MAX_VALUE = 10;
        private static final float INITIAL_VALUE = 2.5f;

	private final float maxRadius;
	private final float minRadius;
	private final float circleCenterX;
	private final float circleCenterY;
	private float minMicLevel = MIN_VALUE;
	private float maxMicLevel = MAX_VALUE;

	private boolean drawSoundLevel = false;

	private final Paint paintIndicatorHalo;
	private final Paint paintIndicatorCenter;
	private float smoothedLevel = INITIAL_VALUE;
	private final Rect bounds = new Rect();
	private boolean drawCenter = false;

	private static Paint newColorPaint(final int color) {
		final Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		paint.setAntiAlias(true);
		paint.setColor(color);
		return paint;
	}

	public SoundLevelCircleDrawable() {
		this(null);
	}

	public SoundLevelCircleDrawable(@Nullable final Params params) {
		final int centerColor;
		final int haloColor;
		if (params != null) {
			this.maxRadius = params.maxRadius;
			this.minRadius = params.minRadius;
			this.circleCenterX = params.circleCenterX;
			this.circleCenterY = params.circleCenterY;
			centerColor = params.centerColor;
			haloColor = params.haloColor;
		} else {
			this.maxRadius = -1;
			this.minRadius = -1;
			this.circleCenterX = -1;
			this.circleCenterY = -1;
			centerColor = CENTER_COLOR_DEF;
			haloColor = HALO_COLOR_DEF;
		}
		paintIndicatorHalo = newColorPaint(haloColor);
		paintIndicatorCenter = newColorPaint(centerColor);
	}

	@SuppressWarnings("MagicNumber")
	@Override
	public void draw(final Canvas canvas) {
		if (drawSoundLevel || drawCenter) {
			canvas.save();
			try {
				if (this.maxRadius < 0 || this.circleCenterX < 0 || this.circleCenterY < 0)
					canvas.getClipBounds(bounds);

				canvas.drawColor(Color.TRANSPARENT);
				final float levelInFraction = (smoothedLevel - minMicLevel) / (maxMicLevel - minMicLevel);
				final float maxRadius = this.maxRadius < 0 ? bounds.width() / 2f : this.maxRadius;
				final float minRadius = this.minRadius < 0 ? maxRadius * (65 / 112.5f) : this.minRadius;
				final float soundMinRadius = minRadius * 0.8f; //to hide halo on silence
				final float rangeRadius = maxRadius - soundMinRadius;
				final float soundRadius = soundMinRadius + rangeRadius * levelInFraction;
				final float x = this.circleCenterX < 0 ? bounds.width() / 2f : this.circleCenterX;
				final float y = this.circleCenterY < 0 ? bounds.height() / 2f : this.circleCenterY;
				if (drawSoundLevel)
					canvas.drawCircle(x, y, soundRadius, paintIndicatorHalo);
				if (drawCenter || drawSoundLevel)
					canvas.drawCircle(x, y, minRadius, paintIndicatorCenter);
			} finally {
				canvas.restore();
			}
		}
	}

	@Override
	public void setAlpha(final int ignored) {
		//ignore
	}

	@Override
	public void setColorFilter(final ColorFilter ignored) {
		//ignore
	}

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}

	/**
	 * @param drawSoundLevel true to draw
	 * @return true if drawSoundLevel changed
	 */
	public boolean setDrawSoundLevel(final boolean drawSoundLevel) {
		if (this.drawSoundLevel != drawSoundLevel) {
			this.drawSoundLevel = drawSoundLevel;
			if (drawSoundLevel) {
			    minMicLevel = MIN_VALUE;
			    maxMicLevel = MAX_VALUE;
			    smoothedLevel = INITIAL_VALUE;
			}
			return true;
		} else
			return false;
	}

	public void setDrawCenter(final boolean drawCenter) {
		this.drawCenter = drawCenter;
	}

	public void setSoundLevel(final float soundLevel) {
		final float positiveSoundLevel = Math.abs(soundLevel);

		if (positiveSoundLevel < minMicLevel) {
			minMicLevel = (minMicLevel + positiveSoundLevel) / 2; // average
		}

		if (positiveSoundLevel > maxMicLevel) {
			maxMicLevel = (maxMicLevel + positiveSoundLevel) / 2; // average
		}

		final float alpha = 0.2f;
		smoothedLevel = (smoothedLevel * (1 - alpha) + (positiveSoundLevel * alpha));

		if (smoothedLevel > maxMicLevel) {
			smoothedLevel = maxMicLevel;
		} else if (smoothedLevel < minMicLevel) {
			smoothedLevel = minMicLevel;
		}
	}

	public float getMinRadius() {
		return minRadius;
	}

	public static class Params {
		public final float maxRadius;
		public final float minRadius;
		public final float circleCenterX;
		public final float circleCenterY;
		private final int centerColor;
		private final int haloColor;

		public Params(final float maxRadius, final float minRadius, final float circleCenterX, final float circleCenterY, final int centerColor, final int haloColor) {
			this.maxRadius = maxRadius;
			this.minRadius = minRadius;
			this.circleCenterX = circleCenterX;
			this.circleCenterY = circleCenterY;
			this.centerColor = centerColor;
			this.haloColor = haloColor;
		}
	}
}
