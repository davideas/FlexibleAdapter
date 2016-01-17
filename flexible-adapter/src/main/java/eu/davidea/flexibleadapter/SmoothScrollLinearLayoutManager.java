package eu.davidea.flexibleadapter;

import android.content.Context;
import android.graphics.PointF;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;

public class SmoothScrollLinearLayoutManager extends LinearLayoutManager {
	private static final float MILLISECONDS_PER_INCH = 100f;
	private Context mContext;

	public SmoothScrollLinearLayoutManager(Context context) {
		this(context, VERTICAL, false);
	}

	public SmoothScrollLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
		super(context, orientation, reverseLayout);
		mContext = context;
	}

	@Override
	public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state,
									   int position) {
		RecyclerView.SmoothScroller smoothScroller = new TopSnappedSmoothScroller(recyclerView.getContext()) {
			//This controls the direction in which smoothScroll looks for your view
			@Override
			public PointF computeScrollVectorForPosition(int targetPosition) {
				return new PointF(0, 1);
			}

			//This returns the milliseconds it takes to scroll one pixel.
			@Override
			protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
				return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
			}
		};
		smoothScroller.setTargetPosition(position);
		startSmoothScroll(smoothScroller);
	}


	private class TopSnappedSmoothScroller extends LinearSmoothScroller {
		public TopSnappedSmoothScroller(Context context) {
			super(context);

		}

		@Override
		public PointF computeScrollVectorForPosition(int targetPosition) {
			return SmoothScrollLinearLayoutManager.this
					.computeScrollVectorForPosition(targetPosition);
		}

		@Override
		protected int getVerticalSnapPreference() {
			return SNAP_TO_START;
		}
	}

}