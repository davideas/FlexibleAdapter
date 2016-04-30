package eu.davidea.common;

import android.animation.Animator;
import android.text.TextWatcher;

/**
 * {@link TextWatcher} implementation that does nothing by default
 */
public abstract class SimpleAnimatorListener implements Animator.AnimatorListener {


	@Override
	public void onAnimationStart(Animator animation) {

	}

	@Override
	public void onAnimationEnd(Animator animation) {

	}

	@Override
	public void onAnimationCancel(Animator animation) {

	}

	@Override
	public void onAnimationRepeat(Animator animation) {

	}
}