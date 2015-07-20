package eu.davidea.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.view.View;
import android.view.ViewAnimationUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class Utils {

	public static final String DATE_TIME = "dd MMM yyyy HH:mm:ss z";

	private Utils() {
	}

	public static int dpToPx(Context context, float dp) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return Math.round(dp * scale);
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