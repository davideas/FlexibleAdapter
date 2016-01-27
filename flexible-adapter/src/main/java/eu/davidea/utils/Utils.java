package eu.davidea.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Build.VERSION_CODES;

public final class Utils {

	private static int colorAccent = -1;

	/**
	 * API 21
	 * @see VERSION_CODES#LOLLIPOP
	 */
	public static boolean hasLollipop() {
		return Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP;
	}

	/**
	 * Fetch accent color on Lollipop devices and newer.
	 */
	@TargetApi(VERSION_CODES.LOLLIPOP)
	public static int fetchAccentColor(Context context) {
		if (hasLollipop() && colorAccent < 0) {
			TypedArray androidAttr = context.getTheme().obtainStyledAttributes(new int[]{android.R.attr.colorAccent});
			colorAccent = androidAttr.getColor(0, 0xFF009688); //Default: material_deep_teal_500
			androidAttr.recycle();
		}
		return colorAccent;
	}

}