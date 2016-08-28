package eu.davidea.samples.flexibleadapter;

import android.content.Context;
import android.os.Build;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public class ViewBehavior extends CoordinatorLayout.Behavior<HeaderView> {

	private Context mContext;

	private int mStartMarginLeft;
	private int mEndMarginLeft;
	private int mMarginRight;
	private int mStartMarginBottom;
	private boolean isHide;

	public ViewBehavior(Context context, AttributeSet attrs) {
		mContext = context;
	}

	@Override
	public boolean layoutDependsOn(CoordinatorLayout parent, HeaderView child, View dependency) {
		return dependency instanceof AppBarLayout;
	}

	@Override
	public boolean onDependentViewChanged(CoordinatorLayout parent, HeaderView child, View dependency) {
		shouldInitProperties(child, dependency);

		int maxScroll = ((AppBarLayout) dependency).getTotalScrollRange();
		float percentage = Math.abs(dependency.getY()) / (float) maxScroll;

		float childPosition = dependency.getHeight()
				+ dependency.getY()
				- child.getHeight()
				- (getToolbarHeight() - child.getHeight()) * percentage / 2;


		childPosition = childPosition - mStartMarginBottom * (1f - percentage);

		CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
		lp.leftMargin = (int) (percentage * mEndMarginLeft) + mStartMarginLeft;
		lp.rightMargin = mMarginRight;
		child.setLayoutParams(lp);

		child.setY(childPosition);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			if (isHide && percentage < 1) {
				child.setVisibility(View.VISIBLE);
				isHide = false;
			} else if (!isHide && percentage == 1) {
				child.setVisibility(View.GONE);
				isHide = true;
			}
		}
		return true;
	}

	private void shouldInitProperties(HeaderView child, View dependency) {

		if (mStartMarginLeft == 0)
			mStartMarginLeft = mContext.getResources().getDimensionPixelOffset(R.dimen.header_view_start_margin_left);

		if (mEndMarginLeft == 0)
			mEndMarginLeft = mContext.getResources().getDimensionPixelOffset(R.dimen.header_view_end_margin_left);

		if (mStartMarginBottom == 0)
			mStartMarginBottom = mContext.getResources().getDimensionPixelOffset(R.dimen.header_view_start_margin_bottom);

		if (mMarginRight == 0)
			mMarginRight = mContext.getResources().getDimensionPixelOffset(R.dimen.header_view_end_margin_right);

	}


	public int getToolbarHeight() {
		int result = 0;
		TypedValue tv = new TypedValue();
		if (mContext.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
			result = TypedValue.complexToDimensionPixelSize(tv.data, mContext.getResources().getDisplayMetrics());
		}
		return result;
	}

}