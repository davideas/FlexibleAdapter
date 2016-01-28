package eu.davidea.flexibleadapter.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Build.VERSION_CODES;

public final class Utils {

	/**
	 * API 21
	 * @see VERSION_CODES#LOLLIPOP
	 */
	public static boolean hasLollipop() {
		return Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP;
	}

	/**
	 * Fetch accent color on devices with at least Lollipop.
	 *
	 * @param context context
	 * @param colorAccent value to return if the accentColor cannot be found
	 */
	@TargetApi(VERSION_CODES.LOLLIPOP)
	public static int fetchAccentColor(Context context, int colorAccent) {
		if (hasLollipop()) {
			TypedArray androidAttr = context.getTheme().obtainStyledAttributes(new int[]{android.R.attr.colorAccent});
			colorAccent = androidAttr.getColor(0, colorAccent);
			androidAttr.recycle();
		}
		return colorAccent;
	}

	public static int dpToPx(Context context, float dp) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return Math.round(dp * scale);
	}

}