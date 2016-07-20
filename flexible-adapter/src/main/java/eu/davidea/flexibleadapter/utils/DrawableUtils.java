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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.util.TypedValue;
import android.view.View;

import java.util.Arrays;

import eu.davidea.flexibleadapter.R;

/**
 * @author Davide Steduto
 * @since 14/06/2016 Created
 */
public final class DrawableUtils {

	/**
	 * Helper method to set the background depending on the android version.
	 *
	 * @param view     the view to apply the drawable
	 * @param drawable drawable object
	 * @since 5.0.0-b7
	 */
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public static void setBackground(View view, Drawable drawable) {
		if (Utils.hasJellyBean()) {
			view.setBackground(drawable);
		} else {
			view.setBackgroundDrawable(drawable);
		}
	}

	/**
	 * Helper method to set the background depending on the android version
	 *
	 * @param view        the view to apply the drawable
	 * @param drawableRes drawable resource id
	 * @since 5.0.0-b7
	 */
	public static void setBackground(View view, @DrawableRes int drawableRes) {
		setBackground(view, getDrawableCompat(view.getContext(), drawableRes));
	}

	/**
	 * Helper method to get the drawable by its resource. Specific to the correct android version..
	 *
	 * @param context     the context
	 * @param drawableRes drawable resource id
	 * @return the drawable object
	 * @since 5.0.0-b7
	 */
	@SuppressWarnings("deprecation")
	public static Drawable getDrawableCompat(Context context, @DrawableRes int drawableRes) {
		try {
			if (Utils.hasLollipop()) {
				return context.getResources().getDrawable(drawableRes, context.getTheme());
			} else {
				return context.getResources().getDrawable(drawableRes);
			}
		} catch (Exception ex) {
			return null;
		}
	}

	/**
	 * Helper to get the system default Selectable Background.
	 *
	 * @param context the context
	 * @return Default selectable background resId
	 * @since 5.0.0-b7
	 */
	public static int getSelectableBackground(Context context) {
		TypedValue outValue = new TypedValue();
		//it is important here to not use the android.R because this wouldn't add the latest drawable
		context.getTheme().resolveAttribute(R.attr.selectableItemBackground, outValue, true);
		return outValue.resourceId;
	}

	/**
	 * Helper to get the system default Color Control Highlight.
	 *
	 * @param context the context
	 * @return Default Color Control Highlight resId
	 * @since 5.0.0-b7
	 */
	public static int getColorControlHighlight(Context context) {
		TypedValue outValue = new TypedValue();
		//it is important here to not use the android.R because this wouldn't add the latest drawable
		context.getTheme().resolveAttribute(R.attr.colorControlHighlight, outValue, true);
		return outValue.resourceId;
	}

	/**
	 * Helper to get a custom selectable background with Ripple if device has at least Lollipop.
	 *
	 * @param rippleColor  the color of the ripple
	 * @param normalColor  the color in normal state
	 * @param pressedColor the pressed color
	 * @return the RippleDrawable with StateListDrawable if at least Lollipop, the normal
	 * StateListDrawable otherwise
	 * @since 5.0.0-b7
	 */
	public static Drawable getSelectableBackgroundCompat(@ColorInt int rippleColor,
														 @ColorInt int normalColor,
														 @ColorInt int pressedColor) {
		if (Utils.hasLollipop()) {
			return new RippleDrawable(ColorStateList.valueOf(rippleColor),
					getStateListDrawable(normalColor, pressedColor, true),
					getRippleMask(normalColor));
		} else {
			return getStateListDrawable(normalColor, pressedColor, false);
		}
	}

	private static Drawable getRippleMask(@ColorInt int color) {
		float[] outerRadii = new float[8];
		//3 is the radius of final ripple, instead of 3 we can give required final radius
		Arrays.fill(outerRadii, 3);
		RoundRectShape r = new RoundRectShape(outerRadii, null, null);
		ShapeDrawable shapeDrawable = new ShapeDrawable(r);
		shapeDrawable.getPaint().setColor(color);
		return shapeDrawable;
	}

	private static StateListDrawable getStateListDrawable(@ColorInt int normalColor,
														  @ColorInt int pressedColor,
														  boolean withRipple) {
		StateListDrawable states = new StateListDrawable();
		if (!withRipple)
			states.addState(new int[]{android.R.attr.state_pressed}, getColorDrawable(pressedColor));
		states.addState(new int[]{android.R.attr.state_activated}, getColorDrawable(pressedColor));
		states.addState(new int[]{}, getColorDrawable(normalColor));
		//if possible we enable animating across states
		if (!withRipple) {
			int duration = 200; //android.R.integer.config_shortAnimTime
			states.setEnterFadeDuration(duration);
			states.setExitFadeDuration(duration);
		}
		return states;
	}

	private static ColorDrawable getColorDrawable(@ColorInt int color) {
		return new ColorDrawable(color);
	}

}