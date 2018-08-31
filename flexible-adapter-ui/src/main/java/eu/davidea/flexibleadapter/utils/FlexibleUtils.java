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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import eu.davidea.flexibleadapter.SelectableAdapter;
import eu.davidea.flexibleadapter.helpers.R;

/**
 * Set of utility methods most used in all applications.
 *
 * @author Davide Steduto
 * @see DrawableUtils
 * @see LayoutUtils
 * @since 27/01/2016 Created in main package
 * <br>17/12/2017 Moved into UI package
 * <br>12/05/2018 Added even more utils
 */
@SuppressWarnings({"WeakerAccess", "unused", "ConstantConditions", "deprecation"})
public final class FlexibleUtils {

    public static final String DATE_TIME_FORMAT = "dd MMM yyyy HH:mm:ss z";
    public static final String SPLIT_EXPRESSION = "([, ]+)";
    public static final int INVALID_COLOR = -1;
    public static int colorAccent = INVALID_COLOR;

    /*----------------*/
    /* VERSIONS UTILS */
    /*----------------*/

    public static String getVersionName(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return context.getString(android.R.string.unknownName);
        }
    }

    public static int getVersionCode(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }

    /**
     * API 26
     *
     * @see VERSION_CODES#O
     */
    public static boolean hasOreo() {
        return Build.VERSION.SDK_INT >= VERSION_CODES.O;
    }

    /**
     * API 24
     *
     * @see VERSION_CODES#N
     */
    public static boolean hasNougat() {
        return Build.VERSION.SDK_INT >= VERSION_CODES.N;
    }

    /**
     * API 23
     *
     * @see VERSION_CODES#M
     */
    public static boolean hasMarshmallow() {
        return Build.VERSION.SDK_INT >= VERSION_CODES.M;
    }

    /**
     * API 21
     *
     * @see VERSION_CODES#LOLLIPOP
     */
    public static boolean hasLollipop() {
        return Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP;
    }

    /**
     * API 16
     *
     * @see VERSION_CODES#JELLY_BEAN
     */
    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN;
    }

    /*---------------------*/
    /* STRING MANIPULATION */
    /*---------------------*/

    /**
     * Sets a spannable text with the accent color into the provided TextView.
     * <p>Multiple matches will be highlighted, but if the 2nd match is consecutive,
     * the highlight is skipped.</p>
     * Internally calls {@link #fetchAccentColor(Context, int)}.
     *
     * @param textView     the TextView to transform and which the originalText is assigned to
     * @param originalText the original text which the transformation is applied to
     * @param constraint   the text to highlight
     * @see #highlightText(TextView, String, String, int)
     * @see #highlightWords(TextView, String, String)
     * @since 1.0.0-b1
     */
    public static void highlightText(@NonNull final TextView textView,
                                     @Nullable final String originalText,
                                     @Nullable String constraint) {
        int accentColor = fetchAccentColor(textView.getContext(), 1);
        highlightText(textView, originalText, constraint, accentColor);
    }

    /**
     * Sets a spannable text with any highlight color into the provided TextView.
     * <p>Multiple matches will be highlighted, but if the 2nd match is consecutive,
     * the highlight is skipped.</p>
     *
     * @param textView     the TextView to transform and which the originalText is assigned to
     * @param originalText the original text which the transformation is applied to
     * @param constraint   the text to highlight
     * @param color        the highlight color
     * @see #highlightText(TextView, String, String)
     * @see #highlightWords(TextView, String, String, int)
     * @since 1.0.0-b1
     */
    public static void highlightText(@NonNull final TextView textView,
                                     @Nullable final String originalText,
                                     @Nullable String constraint,
                                     @ColorInt int color) {
        constraint = toLowerCase(constraint);
        int start = toLowerCase(originalText).indexOf(constraint);
        if (start != -1) {
            Spannable spanText = Spannable.Factory.getInstance().newSpannable(originalText);
            spanText(originalText, constraint, color, start, spanText);
            textView.setText(spanText, TextView.BufferType.SPANNABLE);
        } else {
            textView.setText(originalText, TextView.BufferType.NORMAL);
        }
    }

    /**
     * Sets a spannable text with the accent color for <u>each word</u> provided by the
     * constraint text into the provided TextView.
     * <p><b>Note:</b>
     * <ul>
     * <li>Words are automatically split by {@code "([, ]+)"} regular expression.</li>
     * <li>To Actually see the text highlighted, the filter must check words too.</li>
     * <li>Internally calls {@link #fetchAccentColor(Context, int)}.</li></ul></p>
     *
     * @param textView     the TextView to transform and which the originalText is assigned to
     * @param originalText the original text which the transformation is applied to
     * @param constraints  the multiple words to highlight
     * @see #highlightWords(TextView, String, String, int)
     * @see #highlightText(TextView, String, String)
     * @since 1.0.0-b1
     */
    public static void highlightWords(@NonNull final TextView textView,
                                      @Nullable final String originalText,
                                      @Nullable String constraints) {
        int accentColor = fetchAccentColor(textView.getContext(), 1);
        highlightWords(textView, originalText, constraints, accentColor);
    }

    /**
     * Sets a spannable text with any highlight color for <u>each word</u> provided by the
     * constraint text into the provided TextView.
     * <p><b>Note:</b>
     * <ul><li>Words are automatically split by {@code "([, ]+)"} regular expression.</li>
     * <li>To Actually see the text highlighted, the filter must check words too.</li></ul></p>
     *
     * @param textView     the TextView to transform and which the originalText is assigned to
     * @param originalText the original text which the transformation is applied to
     * @param constraints  the multiple words to highlight
     * @param color        the highlight color
     * @see #highlightWords(TextView, String, String)
     * @see #highlightText(TextView, String, String, int)
     * @since 1.0.0-b1
     */
    public static void highlightWords(@NonNull final TextView textView,
                                      @Nullable final String originalText,
                                      @Nullable String constraints,
                                      @ColorInt int color) {
        constraints = toLowerCase(constraints);
        Spannable spanText = null;

        for (String constraint : constraints.split(SPLIT_EXPRESSION)) {
            int start = toLowerCase(originalText).indexOf(constraint);
            if (start != -1) {
                if (spanText == null) {
                    spanText = Spannable.Factory.getInstance().newSpannable(originalText);
                }
                spanText(originalText, constraint, color, start, spanText);
            }
        }

        if (spanText != null) {
            textView.setText(spanText, TextView.BufferType.SPANNABLE);
        } else {
            textView.setText(originalText, TextView.BufferType.NORMAL);
        }
    }

    private static void spanText(@NonNull final String originalText,
                                 @NonNull String constraint,
                                 @ColorInt int color, int start,
                                 @NonNull final Spannable spanText) {
        do {
            int end = start + constraint.length();
            spanText.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spanText.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            start = toLowerCase(originalText).indexOf(constraint, end + 1); // +1 skips the consecutive span
        } while (start != -1);
    }

    @NonNull
    public static String toLowerCase(@Nullable String text) {
        if (text == null) text = "";
        return text.toLowerCase(Locale.getDefault());
    }

    public static Spanned fromHtmlCompat(String text) {
        if (hasNougat()) {
            return Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(text);
        }
    }

    public static void textAppearanceCompat(TextView textView, @StyleRes int resId) {
        if (hasMarshmallow()) {
            textView.setTextAppearance(resId);
        } else {
            textView.setTextAppearance(textView.getContext(), resId);
        }
    }

    /**
     * Predefined format: <code>dd MMM yyyy HH:mm:ss z</code>
     *
     * @param date the date to format
     * @return The date formatted.
     */
    public static String formatDateTime(Date date) {
        return formatDateTime(date, DATE_TIME_FORMAT);
    }

    /**
     * Date formatter with the provided format.
     *
     * @param date   the date to format
     * @param format the format to apply
     * @return The date formatted.
     */
    public static String formatDateTime(Date date, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        return dateFormat.format(date);
    }

    /*-----------*/
    /* SHORTCUTS */
    /*-----------*/

    /**
     * @return the string representation of the provided {@link SelectableAdapter.Mode}
     */
    @NonNull
    public static String getModeName(@SelectableAdapter.Mode int mode) {
        return LayoutUtils.getModeName(mode);
    }

    /**
     * @return the <code>SimpleClassName</code> of the provided object
     */
    @NonNull
    public static String getClassName(@Nullable Object o) {
        return LayoutUtils.getClassName(o);
    }

    /*-------------*/
    /* COLOR UTILS */
    /*-------------*/

    /**
     * Adjusts the alpha of a color.
     *
     * @param color the color
     * @param alpha the alpha value we want to set 0-255
     * @return the adjusted color
     */
    public static int adjustAlpha(@ColorInt int color, @IntRange(from = 0, to = 255) int alpha) {
        return (alpha << 24) | (color & 0x00ffffff);
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
    public static int fetchAccentColor(Context context, @ColorInt int defColor) {
        if (colorAccent == INVALID_COLOR) {
            int attr = R.attr.colorAccent;
            if (hasLollipop()) attr = android.R.attr.colorAccent;
            TypedArray androidAttr = context.getTheme().obtainStyledAttributes(new int[]{attr});
            colorAccent = androidAttr.getColor(0, defColor);
            androidAttr.recycle();
        }
        return colorAccent;
    }

    /*--------------*/
    /* SCREEN UTILS */
    /*--------------*/

    @NonNull
    public static Point getScreenDimensions(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);

        Point point = new Point();
        point.set(dm.widthPixels, dm.heightPixels);
        return point;
    }

    public static DisplayMetrics getDisplayMetrics(Context context) {
        return context.getResources().getDisplayMetrics();
    }

    public static int dpToPx(Context context, float dp) {
        return Math.round(dp * getDisplayMetrics(context).density);
    }

    /*----------------*/
    /* KEYBOARD UTILS */
    /*----------------*/

    /**
     * Show Soft Keyboard with new Thread.
     */
    public static void showSoftInput(final Context context, final View view) {
        new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        }.run();
    }

    /**
     * Show Soft Keyboard with new Thread.
     */
    public static void hideSoftInput(final Activity activity) {
        if (activity.getCurrentFocus() != null) {
            new Runnable() {
                public void run() {
                    InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
                }
            }.run();
        }
    }

    /**
     * Hide Soft Keyboard from Dialogs with new Thread.
     */
    public static void hideSoftInputFrom(final Context context, final View view) {
        new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }.run();
    }

    /*---------------------*/
    /* REVEAL EFFECT UTILS */
    /*---------------------*/

    /**
     * Create the reveal effect animation.
     *
     * @param view    the View to reveal
     * @param centerX the x coordinate of the center of the animating circle, relative to <code>view</code>.
     * @param centerY the y coordinate of the center of the animating circle, relative to <code>view</code>.
     * @since 1.0.0-b4
     */
    @TargetApi(VERSION_CODES.LOLLIPOP)
    public static void reveal(final View view, int centerX, int centerY) {
        if (!hasLollipop()) {
            view.setVisibility(View.VISIBLE);
            return;
        }
        // Get the final radius for the clipping circle
        int finalRadius = Math.max(view.getWidth(), view.getHeight());
        // Create the animator for this view (the start radius is zero)
        Animator animator = ViewAnimationUtils.createCircularReveal(view, centerX, centerY, 0, finalRadius);
        // Make the view visible and start the animation
        view.setVisibility(View.VISIBLE);
        animator.start();
    }

    /**
     * Create the un-reveal effect animation.
     * <p>View will be set as visibility <code>GONE</code>.</p>
     *
     * @param view    the View to reveal
     * @param centerX the x coordinate of the center of the animating circle, relative to <code>view</code>.
     * @param centerY the y coordinate of the center of the animating circle, relative to <code>view</code>.
     * @since 1.0.0-b4
     */
    @TargetApi(VERSION_CODES.LOLLIPOP)
    public static void unReveal(final View view, int centerX, int centerY) {
        if (!hasLollipop()) {
            view.setVisibility(View.GONE);
            return;
        }
        // Get the initial radius for the clipping circle
        int initialRadius = view.getWidth();
        // Create the animation (the final radius is zero)
        Animator animator = ViewAnimationUtils.createCircularReveal(view, centerX, centerY, initialRadius, 0);
        // Make the view invisible when the animation is done
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setVisibility(View.GONE);
            }
        });
        // Start the animation
        animator.start();
    }

}