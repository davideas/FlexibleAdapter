/*
 * Copyright 2016 Davide Steduto
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
package eu.davidea.flexibleadapter.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.widget.TextView;

import java.util.Locale;

public final class Utils {

	public static final int INVALID_COLOR = -1;
	private static int colorAccent = INVALID_COLOR;

	/**
	 * API 21
	 *
	 * @see VERSION_CODES#LOLLIPOP
	 */
	public static boolean hasLollipop() {
		return Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP;
	}


	/**
	 * Sets a spannable text with the accent color (if available) into the passed TextView.
	 * <p>Internally calls {@link #fetchAccentColor(Context, int)}.</p>
	 *
	 * @param context      context
	 * @param textView     the TextView to transform
	 * @param originalText the original text which the transformation is applied to
	 * @param constraint   the text to highlight
	 * @param defColor     the default color in case accentColor is not available
	 * @see #fetchAccentColor(Context, int)
	 */
	public static void setHighlightText(Context context, TextView textView, String originalText, String constraint, int defColor) {
		Spannable spanText = Spannable.Factory.getInstance().newSpannable(originalText);
		int i = originalText.toLowerCase(Locale.getDefault()).indexOf(constraint);
		if (i != -1) {
			spanText.setSpan(new ForegroundColorSpan(fetchAccentColor(context, defColor)), i,
					i + constraint.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			spanText.setSpan(new StyleSpan(Typeface.BOLD), i,
					i + constraint.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			textView.setText(spanText, TextView.BufferType.SPANNABLE);
		} else {
			textView.setText(originalText, TextView.BufferType.NORMAL);
		}
	}

	/**
	 * Reset the internal accent color to {@link #INVALID_COLOR}, to give the possibility
	 * to re-fetch it at runtime, since once it is fetched it cannot be changed.
	 */
	public static void resetAccentColor() {
		colorAccent = INVALID_COLOR;
	}

	/**
	 * Optimized method to fetch the accent color on devices with at least Lollipop.
	 * <p>If accent color has been already fetched it is simply returned.</p>
	 *
	 * @param context  context
	 * @param defColor value to return if the accentColor cannot be found
	 */
	@TargetApi(VERSION_CODES.LOLLIPOP)
	public static int fetchAccentColor(Context context, int defColor) {
		if (colorAccent == INVALID_COLOR) {
			if (hasLollipop()) {
				TypedArray androidAttr = context.getTheme().obtainStyledAttributes(new int[]{android.R.attr.colorAccent});
				colorAccent = androidAttr.getColor(0, defColor);
				androidAttr.recycle();
			} else {
				colorAccent = defColor;
			}
		}
		return colorAccent;
	}

	public static int dpToPx(Context context, float dp) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return Math.round(dp * scale);
	}

}