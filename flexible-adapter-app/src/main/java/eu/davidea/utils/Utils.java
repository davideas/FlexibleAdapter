package eu.davidea.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntRange;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import eu.davidea.samples.flexibleadapter.R;

public final class Utils {

	public static final String DATE_TIME = "dd MMM yyyy HH:mm:ss z";
	private static int colorAccent = -1;

	private Utils() {
	}

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

	public static float dpToPx(Context context, float dp) {
		return Math.round(dp * getDisplayMetrics(context).density);
	}

	/**
	 * dd MMM yyyy HH:mm:ss z
	 * @param date
	 * @return The date formatted.
	 */
	public static String formatDate(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_TIME, Locale.getDefault());
		return dateFormat.format(date);
	}

	/**
	 * API 11
	 * @see VERSION_CODES#HONEYCOMB
	 */
	public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB;
    }

	/**
	 * API 14
	 * @see VERSION_CODES#ICE_CREAM_SANDWICH
	 */
	public static boolean hasIceCreamSandwich() {
		return Build.VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH;
	}

	/**
	 * API 16
	 * @see VERSION_CODES#JELLY_BEAN
	 */
	public static boolean hasJellyBean() {
		return Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN;
	}

	/**
	 * API 19
	 * @see VERSION_CODES#KITKAT
	 */
	public static boolean hasKitkat() {
		return Build.VERSION.SDK_INT >= VERSION_CODES.KITKAT;
	}

	/**
	 * API 21
	 * @see VERSION_CODES#LOLLIPOP
	 */
	public static boolean hasLollipop() {
		return Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP;
	}

	/**
	 * API 23
	 * @see VERSION_CODES#M
	 */
	public static boolean hasMarshmallow() {
		return Build.VERSION.SDK_INT >= VERSION_CODES.M;
	}

	public static String getVersionName(Context context) {
		try {
			PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return "v" + pInfo.versionName;
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
	 * Helper method to set the background depending on the android version.
	 *
	 * @param view the view to apply the background drawable
	 * @param drawable the Drawable, if null the background will be removed
	 */
	public static void setBackground(View view, Drawable drawable) {
		if (hasJellyBean()) {
			view.setBackground(drawable);
		} else {
			view.setBackgroundDrawable(drawable);
		}
	}

	/**
	 * Helper method to set the background depending on the android version.
	 *
	 * @param view the view to apply the background drawable
	 * @param drawableRes the Drawable resource id
	 */
	public static void setBackground(View view, @DrawableRes int drawableRes) {
		setBackground(view, getCompatDrawable(view.getContext(), drawableRes));
	}

	/**
	 * Helper method to get the drawable by its resource. specific to the correct android version.
	 *
	 * @param context the context
	 * @param drawableRes the Drawable resource id
	 * @return the compatible API Drawable
	 */
	@SuppressLint("NewApi")
	public static Drawable getCompatDrawable(Context context, @DrawableRes int drawableRes) {
		try {
			return hasLollipop() ? context.getResources().getDrawable(drawableRes, context.getTheme())
					: context.getResources().getDrawable(drawableRes);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Helper to get the system default selectable background.
	 *
	 * @param context the context
	 * @return the Drawable resource id
	 */
	public static int getSelectableBackground(Context context) {
		if (hasHoneycomb()) {
			//If we're running on Honeycomb or newer, then we can use the Theme's
			//selectableItemBackground to ensure that the View has a pressed state
			TypedValue outValue = new TypedValue();
			//It is important here to not use the android.R because this wouldn't add the latest drawable
			context.getTheme().resolveAttribute(R.attr.selectableItemBackground, outValue, true);
			return outValue.resourceId;
		} else {
			TypedValue outValue = new TypedValue();
			context.getTheme().resolveAttribute(android.R.attr.itemBackground, outValue, true);
			return outValue.resourceId;
		}
	}

	/**
	 * Helper to get the system default selectable background inclusive an active state.
	 *
	 * @param context       the context
	 * @param selectedColor the selected color
	 * @param animate       true if you want to fade over the states (only animates if API
	 *                      newer than Build.VERSION_CODES.HONEYCOMB)
	 * @return the StateListDrawable
	 */
	public static StateListDrawable getSelectableBackground(
			Context context, @ColorInt int selectedColor, boolean animate) {

		StateListDrawable states = new StateListDrawable();
		ColorDrawable clrActive = new ColorDrawable(selectedColor);
		states.addState(new int[]{android.R.attr.state_selected}, clrActive);
		states.addState(new int[]{android.R.attr.state_activated}, clrActive);
		states.addState(new int[]{}, ContextCompat.getDrawable(context, getSelectableBackground(context)));
		//if possible we enable animating across states
		if (animate && hasHoneycomb()) {
			int duration = context.getResources().getInteger(android.R.integer.config_shortAnimTime);
			states.setEnterFadeDuration(duration);
			states.setExitFadeDuration(duration);
		}
		return states;
	}

	/**
	 * Helper to get the system default selectable background inclusive an active and pressed state.
	 * <p>Useful for Image item selection.</p>
	 *
	 * @param context       the context
	 * @param selectedColor the selected color
	 * @param pressedAlpha  0-255
	 * @param animate       true if you want to fade over the states (only animates if API
	 *                      newer than Build.VERSION_CODES.HONEYCOMB)
	 * @return the StateListDrawable
	 */
	public static StateListDrawable getSelectablePressedBackground(
			Context context, @ColorInt int selectedColor,
			@IntRange(from = 0, to = 255) int pressedAlpha, boolean animate) {

		StateListDrawable states = getSelectableBackground(context, selectedColor, animate);
		ColorDrawable clrPressed = new ColorDrawable(adjustAlpha(selectedColor, pressedAlpha));
		states.addState(new int[]{android.R.attr.state_pressed}, clrPressed);
		return states;
	}

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

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public static int getColorAccent(Context context) {
		if (colorAccent < 0) {
			int accentAttr = eu.davidea.flexibleadapter.utils.Utils.hasLollipop() ? android.R.attr.colorAccent : R.attr.colorAccent;
			TypedArray androidAttr = context.getTheme().obtainStyledAttributes(new int[] { accentAttr });
			colorAccent = androidAttr.getColor(0, 0xFF009688); //Default: material_deep_teal_500
			androidAttr.recycle();
		}
		return colorAccent;
	}

	/**
	 * Show Soft Keyboard with new Thread
	 * @param activity
	 */
	public static void hideSoftInput(final Activity activity) {
		if (activity.getCurrentFocus() != null) {
			new Runnable() {
				public void run() {
					InputMethodManager imm =
							(InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
				}
			}.run();
		}
	}

	/**
	 * Hide Soft Keyboard from Dialogs with new Thread
	 * @param context
	 * @param view
	 */
	public static void hideSoftInputFrom(final Context context, final View view) {
		new Runnable() {
			@Override
			public void run() {
				InputMethodManager imm =
						(InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
			}
		}.run();
	}

	/**
	 * Show Soft Keyboard with new Thread
	 * @param context
	 * @param view
	 */
	public static void showSoftInput(final Context context, final View view) {
		new Runnable() {
			@Override
			public void run() {
				InputMethodManager imm =
						(InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
			}
		}.run();
	}

	/**
	 * Create the reveal effect animation
	 * @param view the View to reveal
	 * @param cx coordinate X
	 * @param cy coordinate Y
	 */
	@TargetApi(VERSION_CODES.LOLLIPOP)
	public static void reveal(final View view, int cx, int cy) {
		if (!hasLollipop()) {
			view.setVisibility(View.VISIBLE);
			return;
		}

		//Get the final radius for the clipping circle
		int finalRadius = Math.max(view.getWidth(), view.getHeight());

		//Create the animator for this view (the start radius is zero)
		Animator animator =
				ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);

		//Make the view visible and start the animation
		view.setVisibility(View.VISIBLE);
		animator.start();
	}

	/**
	 * Create the un-reveal effect animation
	 * @param view the View to hide
	 * @param cx coordinate X
	 * @param cy coordinate Y
	 */
	@TargetApi(VERSION_CODES.LOLLIPOP)
	public static void unReveal(final View view, int cx, int cy) {
		if (!hasLollipop()) {
			view.setVisibility(View.GONE);
			return;
		}

		//Get the initial radius for the clipping circle
		int initialRadius = view.getWidth();

		//Create the animation (the final radius is zero)
		Animator animator =
			ViewAnimationUtils.createCircularReveal(view, cx, cy, initialRadius, 0);

		//Make the view invisible when the animation is done
		animator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				view.setVisibility(View.GONE);
			}
		});

		//Start the animation
		animator.start();
	}

}