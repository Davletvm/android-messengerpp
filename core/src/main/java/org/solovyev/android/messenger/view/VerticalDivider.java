/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.solovyev.android.messenger.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static android.os.Build.VERSION_CODES.HONEYCOMB;
import static org.solovyev.android.Views.toPixels;

public final class VerticalDivider extends View {

	@Nullable
	private Paint linePaint;


	public VerticalDivider(Context context) {
		super(context);
		init();
	}

	public VerticalDivider(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public VerticalDivider(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		if (Build.VERSION.SDK_INT >= HONEYCOMB) {
			setSoftwareLayerType();
		}
	}

	@TargetApi(HONEYCOMB)
	private void setSoftwareLayerType() {
		setLayerType(View.LAYER_TYPE_SOFTWARE, null);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawLine(0, 0, 0, getHeight(), getLinePaint(getWidth()));
	}

	@Nonnull
	private Paint getLinePaint(int width) {
		if (linePaint == null) {
			final DisplayMetrics dm = getResources().getDisplayMetrics();

			linePaint = new Paint();
			linePaint.setARGB(150, 0, 0, 0);
			linePaint.setStyle(Paint.Style.STROKE);
			linePaint.setStrokeWidth(width);
			linePaint.setPathEffect(new DashPathEffect(getIntervals(width), 0));
		}
		return linePaint;
	}

	@Nonnull
	private float[] getIntervals(int width) {
		final DisplayMetrics dm = getResources().getDisplayMetrics();
		return new float[]{width, toPixels(dm, 5f)};
	}
}
