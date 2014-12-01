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
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

import ai.api.R;

/**
 * ImageView
 * src used as a mask, and MaskedColorView_mainColor as a color or state color list
 */
public class MaskedColorView extends ImageView {
	private ColorStateList colorStateList = null;

	public MaskedColorView(final Context context) {
		super(context, null);
	}

	public MaskedColorView(final Context context, final AttributeSet attrs) {
		super(context, attrs, android.R.attr.imageButtonStyle);
		configure(attrs);
	}

	public MaskedColorView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		configure(attrs);
	}

    @TargetApi(11)
	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		setColorFilter(getCurrentColor(getDrawableState()), PorterDuff.Mode.SRC_ATOP);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            jumpDrawablesToCurrentState();
        }
	}

	public void setColorStateList(final ColorStateList colorStateList) {
		this.colorStateList = colorStateList;
	}

	private int getCurrentColor(final int[] stateSet) {
		return colorStateList == null
			   ? Color.MAGENTA
			   : colorStateList.getColorForState(stateSet, colorStateList.getDefaultColor());
	}

	private void configure(final AttributeSet attrs) {
		if (attrs != null) {
			final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.MaskedColorView);
			try {
				final ColorStateList csl = a.getColorStateList(R.styleable.MaskedColorView_mainColor);
				if (csl != null) {
                    colorStateList = csl;
                }
			} finally {
				a.recycle();
			}
		}
	}

	protected String getDebugState() {
		return "====\ncsl is " + (colorStateList != null ? "NOT" : "") + " null";
	}

}
