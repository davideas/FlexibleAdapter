package eu.davidea.samples.flexibleadapter.animators;

import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.animation.Interpolator;

import eu.davidea.flexibleadapter.common.FlexibleItemAnimator;

public class GarageDoorItemAnimator extends FlexibleItemAnimator {

	public GarageDoorItemAnimator() {
	}

	public GarageDoorItemAnimator(Interpolator interpolator) {
		mInterpolator = interpolator;
	}

	@Override
	protected void animateRemoveImpl(ViewHolder holder, int index) {
		ViewCompat.animate(holder.itemView)
				.rotationX(90)
				.translationY(-(holder.itemView.getMeasuredHeight() / 2))
				.setDuration(300)
				.setInterpolator(mInterpolator)
				.setListener(new DefaultRemoveVpaListener(holder))
				.start();
	}

	@Override
	protected boolean preAnimateAddImpl(ViewHolder holder) {
		ViewCompat.setRotationX(holder.itemView, 90);
		ViewCompat.setTranslationY(holder.itemView, -(holder.itemView.getMeasuredHeight() / 2));
		return true;
	}

	@Override
	protected void animateAddImpl(ViewHolder holder, int index) {
		ViewCompat.animate(holder.itemView)
				.rotationX(0)
				.translationY(0)
				.setDuration(300)
				.setInterpolator(mInterpolator)
				.setListener(new DefaultAddVpaListener(holder))
				.start();
	}

}