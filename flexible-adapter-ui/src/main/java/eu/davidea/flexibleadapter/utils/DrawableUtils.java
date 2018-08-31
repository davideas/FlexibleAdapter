/*
 * Copyright 2016-2018 Davide Steduto, Davidea Solutions Sprl
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

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.v4.view.ViewCompat;
import android.util.TypedValue;
import android.view.View;

import java.util.Arrays;

import eu.davidea.flexibleadapter.helpers.R;

/**
 * @author Davide Steduto
 * @see FlexibleUtils
 * @see LayoutUtils
 * @since 14/06/2016 Created in main package
 * <br>17/12/2017 Moved into UI package
 */
@SuppressWarnings("deprecation")
public final class DrawableUtils {

    /**
     * Helper method to set the background to a view, depending on the android version.
     *
     * @param view     the view to apply the drawable
     * @param drawable drawable object
     * @since 1.0.0-b1
     */
    public static void setBackgroundCompat(View view, Drawable drawable) {
        ViewCompat.setBackground(view, drawable);
    }

    /**
     * Helper method to set the background to a view, depending on the android version
     *
     * @param view        the view to apply the drawable
     * @param drawableRes drawable resource id
     * @since 1.0.0-b1
     */
    public static void setBackgroundCompat(View view, @DrawableRes int drawableRes) {
        setBackgroundCompat(view, getDrawableCompat(view.getContext(), drawableRes));
    }

    /**
     * Ultra compat method to set the background to a view, providing color resource identifiers.
     * <p><b>Note:</b> If you already have the color in <i>Integer</i> format, please obtain the
     * {@code Drawable} object from {@link #getSelectableBackgroundCompat(int, int, int)} and set
     * it into {@link #setBackgroundCompat(View, Drawable)}.</p>
     *
     * @param view            the view interested at the background
     * @param normalColorRes  the color resource id
     * @param pressedColorRes the pressed color resource id
     * @param rippleColorRes  the ripple color resource id
     * @since 1.0.0-b5
     */
    public static void setBackgroundCompat(View view,
                                           @ColorRes int normalColorRes,
                                           @ColorRes int pressedColorRes,
                                           @ColorRes int rippleColorRes) {
        Context context = view.getContext();
        Drawable drawable;
        if (FlexibleUtils.hasMarshmallow()) {
            drawable = getSelectableBackgroundCompat(
                    context.getColor(normalColorRes),
                    context.getColor(pressedColorRes),
                    context.getColor(rippleColorRes));
        } else {
            drawable = getSelectableBackgroundCompat(
                    context.getResources().getColor(normalColorRes),
                    context.getResources().getColor(pressedColorRes),
                    context.getResources().getColor(rippleColorRes));
        }
        setBackgroundCompat(view, drawable);
    }

    /**
     * Helper method to get the drawable by its resource. Specific to the correct android version.
     *
     * @param context     the context
     * @param drawableRes drawable resource id
     * @return the drawable object
     * @since 1.0.0-b1
     */
    public static Drawable getDrawableCompat(Context context, @DrawableRes int drawableRes) {
        try {
            if (FlexibleUtils.hasLollipop()) {
                return context.getResources().getDrawable(drawableRes, context.getTheme());
            } else {
                return context.getResources().getDrawable(drawableRes);
            }
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Helper method to get the <i>system (or overridden)</i> default {@code selectableItemBackground} Drawable.
     * Returns the {@code R.attr.selectableItemBackground} of the style attribute.
     *
     * @param context the context
     * @return Default selectable item background drawable
     * @since 1.0.0-b1
     */
    public static Drawable getSelectableItemBackground(Context context) {
        TypedValue outValue = new TypedValue();
        // It's important to not use the android.R because this wouldn't add the overridden drawable
        context.getTheme().resolveAttribute(R.attr.selectableItemBackground, outValue, true);
        return getDrawableCompat(context, outValue.resourceId);
    }

    /**
     * Helper method to get the <i>system (or overridden)</i> default {@code colorControlHighlight}.
     * Returns the color of the {@code R.attr.colorControlHighlight} of the style attribute.
     *
     * @param context the context
     * @return Default Color Control Highlight
     * @since 1.0.0-b1
     */
    @ColorInt
    public static int getColorControlHighlight(Context context) {
        TypedValue outValue = new TypedValue();
        // It's important to not use the android.R because this wouldn't add the overridden drawable
        context.getTheme().resolveAttribute(R.attr.colorControlHighlight, outValue, true);
        if (FlexibleUtils.hasMarshmallow()) {
            return context.getColor(outValue.resourceId);
        } else {
            return context.getResources().getColor(outValue.resourceId);
        }
    }

    /**
     * Helper method to get a custom selectable background with Ripple color, if device has at least Lollipop.
     *
     * @param normalColor  the color in normal state
     * @param pressedColor the pressed color
     * @param rippleColor  the color of the ripple
     * @return the RippleDrawable with StateListDrawable if at least Lollipop, the normal
     * StateListDrawable otherwise
     * @since 1.0.0-b1
     */
    public static Drawable getSelectableBackgroundCompat(@ColorInt int normalColor,
                                                         @ColorInt int pressedColor,
                                                         @ColorInt int rippleColor) {
        if (FlexibleUtils.hasLollipop()) {
            return new RippleDrawable(ColorStateList.valueOf(rippleColor),
                    getStateListDrawable(normalColor, pressedColor),
                    getRippleMask(normalColor));
        } else {
            return getStateListDrawable(normalColor, pressedColor);
        }
    }

    /**
     * Adds a ripple effect to any Drawable background.
     *
     * @param drawable    any background drawable
     * @param rippleColor the color of the ripple
     * @return the RippleDrawable with the chosen background drawable if at least Lollipop,
     * the provided drawable otherwise
     * @since 1.0.0-b1
     */
    public static Drawable getRippleDrawable(Drawable drawable, @ColorInt int rippleColor) {
        if (FlexibleUtils.hasLollipop()) {
            return new RippleDrawable(ColorStateList.valueOf(rippleColor),
                    drawable, getRippleMask(Color.BLACK));
        } else {
            return drawable;
        }
    }

    private static Drawable getRippleMask(@ColorInt int color) {
        float[] outerRadii = new float[8];
        // 3 is the radius of final ripple, instead of 3 we can give required final radius
        Arrays.fill(outerRadii, 3);
        RoundRectShape r = new RoundRectShape(outerRadii, null, null);
        ShapeDrawable shapeDrawable = new ShapeDrawable(r);
        shapeDrawable.getPaint().setColor(color);
        return shapeDrawable;
    }

    private static StateListDrawable getStateListDrawable(@ColorInt int normalColor,
                                                          @ColorInt int pressedColor) {
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_activated}, getColorDrawable(pressedColor));
        if (!FlexibleUtils.hasLollipop()) {
            states.addState(new int[]{android.R.attr.state_pressed}, getColorDrawable(pressedColor));
        }
        states.addState(new int[]{}, getColorDrawable(normalColor));
        // Animating across states.
        // It seems item background is lost on scrolling out of the screen on 21 <= API <= 23
        if (!FlexibleUtils.hasLollipop() || FlexibleUtils.hasNougat()) {
            int duration = 200; //android.R.integer.config_shortAnimTime
            states.setEnterFadeDuration(duration);
            states.setExitFadeDuration(duration);
        }
        return states;
    }

    /**
     * Generates the {@code ColorDrawable} object from the provided Color.
     *
     * @param color the color
     * @return the {@code ColorDrawable} object
     * @since 1.0.0-b1
     */
    public static ColorDrawable getColorDrawable(@ColorInt int color) {
        return new ColorDrawable(color);
    }

}