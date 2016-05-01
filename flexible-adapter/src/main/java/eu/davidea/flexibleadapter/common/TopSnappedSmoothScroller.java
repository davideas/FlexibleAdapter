package eu.davidea.flexibleadapter.common;

import android.content.Context;
import android.graphics.PointF;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.util.DisplayMetrics;

class TopSnappedSmoothScroller extends LinearSmoothScroller {

	private static final float MILLISECONDS_PER_INCH = 100f;

	private PointF vectorPosition = new PointF(0, 0);
	private LinearLayoutManager layoutManager;

	public TopSnappedSmoothScroller(Context context, LinearLayoutManager layoutManager) {
		super(context);
		this.layoutManager = layoutManager;
	}

	/**
	 * Controls the direction in which smoothScroll looks for your view
	 *
	 * @return the vector position
	 */
	@Override
	public PointF computeScrollVectorForPosition(int targetPosition) {
		final int firstChildPos = layoutManager.findFirstCompletelyVisibleItemPosition();
		final int direction = targetPosition < firstChildPos ? -1 : 1;

		if (layoutManager.getOrientation() == LinearLayoutManager.HORIZONTAL) {
			vectorPosition.set(direction, 0);
			return vectorPosition;
		} else {
			vectorPosition.set(0, direction);
			return vectorPosition;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
		return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
	}

	@Override
	protected int getVerticalSnapPreference() {
		return SNAP_TO_START;
	}

}