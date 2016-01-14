package eu.davidea.flexibleadapter;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import java.util.EnumSet;
import java.util.List;

/**
 * This Class is responsible to animate items when RecyclerView is firstly loaded. Bounded items
 * are animated initially and also when user starts to scroll the list.<br/>
 * Animations can be customized for each items applying different logic based on item position and
 * beyond.
 * <p/>
 * Created by Davide on 10/01/2016.
 */
public abstract class FlexibleAnimatorAdapter<VH extends RecyclerView.ViewHolder, T> extends FlexibleAdapter<VH, T> {

	protected static final String TAG = FlexibleAnimatorAdapter.class.getSimpleName();

	private RecyclerView mRecyclerView;
	private Interpolator mInterpolator = new LinearInterpolator();
	private AnimatorAdapterDataObserver mNotifierObserver;

	private enum AnimatorEnum {
		ALPHA, SLIDE_IN_LEFT, SLIDE_IN_RIGHT, SLIDE_IN_BOTTOM, SCALE
	}

	/**
	 * The active Animators. Keys are hashcodes of the Views that are animated.
	 */
	@NonNull
	private final SparseArray<Animator> mAnimators = new SparseArray<>();

	/**
	 * The position of the last item that was animated.
	 */
	private int mLastAnimatedPosition = -1;

	private int mLastVisibleItems = -1;

	/**
	 * Contains type of animators already added
	 */
	private EnumSet<AnimatorEnum> animatorsUsed = EnumSet.noneOf(AnimatorEnum.class);

	private boolean isReverseEnabled = false,
			shouldAnimate = true,
			isFastScroll = false;

	private long mInitialDelay = 500L,
			mStepDelay = 100L,
			mDuration = 300L;


	/**
	 * Simple Constructor for Animator Adapter.<br/>
	 *
	 * @param items        Items to display
	 * @param recyclerView NonNull RV necessary to calculate the size of the ItemView on particular Animators
	 */
	public FlexibleAnimatorAdapter(@NonNull List<T> items, @NonNull RecyclerView recyclerView) {
		this(items, null, recyclerView);
	}

	/**
	 * Main Constructor for Animator Adapter.<br/>
	 *
	 * @param items        Items to display
	 * @param listener     Must be an instance of {@link OnUpdateListener}
	 * @param recyclerView NonNull RV necessary to calculate the size of the ItemView on particular Animators
	 */
	public FlexibleAnimatorAdapter(@NonNull List<T> items, Object listener, @NonNull RecyclerView recyclerView) {
		super(items, listener);
		this.mRecyclerView = recyclerView;
		if (recyclerView == null)
			throw new IllegalArgumentException("RecyclerView must be initialized and not null");
	}

	/**
	 * Build your custom list of {@link Animator} to apply on the ItemView.<br/>
	 * Write the logic based on the position and/or viewType and/or the item selection.<br/><br/>
	 * <b>Suggestions: </b>
	 * <br/>- A simple boolean for <i>isSelected</i> is preferable instead of {@link #isSelected(int)}
	 * <br/>- You can also use {@link #getItemViewType(int)} to apply
	 * different Animation for each view type.
	 * <br/>- If you want to apply same animation for all items, create new list at class level
	 * and initialize it in the constructor, not inside this method!
	 *
	 * @param itemView   The bounded ItemView
	 * @param position   Position can be used to differentiate the list of Animators
	 * @param isSelected boolean to be used to differentiate the list of Animators
	 * @return The list of animators to animate all together.
	 * @see #animateView(View, int, boolean)
	 * @see #getItemViewType(int)
	 */
	public abstract List<Animator> getAnimators(View itemView, int position, boolean isSelected);

	@Override
	public void onFastScroll(boolean scrolling) {
		super.onFastScroll(scrolling);
		isFastScroll = scrolling;
	}

	/**
	 * Cancels any existing animations for given View. Useful when fling.
	 */
	private void cancelExistingAnimation(@NonNull final View itemView) {
		int hashCode = itemView.hashCode();
		Animator animator = mAnimators.get(hashCode);
		if (animator != null) animator.end();
	}

	/**
	 * Animate the view based on the custom animator list built with {@link #getAnimators(View, int, boolean)}.
	 */
	protected final void animateView(final View itemView, int position, boolean isSelected) {
		//TODO: Skip animation on fast scrolling: DONE!
		//TODO: Finish to include elements of fast scrolling in the library!
		//TODO: Evaluate to include also EmptyView in the library...

		Log.d(TAG, "shouldAnimate=" + shouldAnimate
				+ " isFastScroll=" + isFastScroll
				+ " isReverseEnabled=" + isReverseEnabled
				+ (isReverseEnabled ? " isNotified=" + mNotifierObserver.isPositionNotified() : "")
				+ (!isReverseEnabled ? " Pos>AniPos=" + (position > mLastAnimatedPosition) : "")
		);

		if (shouldAnimate && !isFastScroll && (isReverseEnabled && !mNotifierObserver.isPositionNotified())
				|| (!isReverseEnabled && position > mLastAnimatedPosition)) {

			//Cancel animation is necessary when fling
			cancelExistingAnimation(itemView);

			ViewCompat.setAlpha(itemView, 0);

			//Retrieve user animators
			List<Animator> animators = getAnimators(itemView, position, isSelected);

			//Add Alpha animator if not yet
			if (!animatorsUsed.contains(AnimatorEnum.ALPHA))
				addAlphaAnimator(animators, itemView, 0f);

			if (DEBUG)
				Log.d(TAG, "Start Animation on position " + position + " Animators=" + animatorsUsed);
			animatorsUsed.clear();

			//Execute the animations all together
			AnimatorSet set = new AnimatorSet();
			set.playTogether(animators);
			set.setStartDelay(calculateAnimationDelay(position));
			//getVisibleItems(position);
			//set.setStartDelay(mInitialDelay += mStepDelay);
			set.setInterpolator(mInterpolator);
			set.setDuration(mDuration);
			set.addListener(new HelperAnimatorListener(itemView.hashCode()));
			set.start();
			mAnimators.put(itemView.hashCode(), set);
		}

		if (isReverseEnabled && mNotifierObserver.isPositionNotified())
			mNotifierObserver.removeNotified();

		mLastAnimatedPosition = position;
	}

	private void getVisibleItems(int position) {

		int lastVisiblePosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
		int firstVisiblePosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
		if (mLastAnimatedPosition > lastVisiblePosition)
			lastVisiblePosition = mLastAnimatedPosition;
		int visibleItems = lastVisiblePosition - firstVisiblePosition;

		Log.d(TAG, "Position=" + position +
				" FirstVisible=" + firstVisiblePosition +
				" LastVisible=" + lastVisiblePosition +
				" LastAnimated=" + mLastAnimatedPosition +
				" VisibleItems=" + visibleItems +
				" childCount=" + mRecyclerView.getChildCount());

		//Stop stepDelay when screen is filled
		if (mLastVisibleItems >= visibleItems ||
				(firstVisiblePosition > 1 && firstVisiblePosition <= mRecyclerView.getChildCount())) {
			if (DEBUG) Log.d(TAG, "Reset AnimationDelay on position " + position);
			mInitialDelay = 0L;
		}

		mLastVisibleItems = visibleItems;
	}

	/**
	 * Returns the delay in milliseconds after which, the animation for next ItemView should start.
	 */
	private long calculateAnimationDelay(final int position) {
		long delay;

		int lastVisiblePosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
		int firstVisiblePosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();

		//FIXME: Minor BUG on fast scrolling (occurs sometimes!): Position is animated with big delay!
		//Delay[663]=66300 FirstVisible=-1 LastVisible=717 LastAnimated=717 VisibleItems=718 childCount=0
		//Delay[182]=18200 FirstVisible=-1 LastVisible=196 LastAnimated=196 VisibleItems=197 childCount=0
		//if (firstVisiblePosition < 0) firstVisiblePosition = 0;

		if (mLastAnimatedPosition > lastVisiblePosition)
			lastVisiblePosition = mLastAnimatedPosition;

		int numberOfItemsOnScreen = lastVisiblePosition - firstVisiblePosition;
		int numberOfAnimatedItems = position - 1;

		if (numberOfItemsOnScreen < numberOfAnimatedItems || //forward scrolling after max itemOnScreen is reached
				(firstVisiblePosition > 1 && firstVisiblePosition <= mRecyclerView.getChildCount())) { //reverse scrolling
			delay = mStepDelay;
			mInitialDelay = 0L;

			if (mRecyclerView.getLayoutManager() instanceof GridLayoutManager) {
				int numColumns = ((GridLayoutManager) mRecyclerView.getLayoutManager()).getSpanCount();
				delay += mStepDelay * (position % numColumns);
			}
			if (DEBUG) Log.d(TAG, "Delay[" + position + "]=*" + delay +
					" FirstVisible=" + firstVisiblePosition +
					" LastVisible=" + lastVisiblePosition +
					" LastAnimated=" + mLastAnimatedPosition +
					" VisibleItems=" + numberOfItemsOnScreen +
					" childCount=" + mRecyclerView.getChildCount());

		} else {//forward scrolling before max itemOnScreen is reached
			delay = mInitialDelay + (position * mStepDelay);
			if (DEBUG) Log.d(TAG, "Delay[" + position + "]=" + delay +
					" FirstVisible=" + firstVisiblePosition +
					" LastVisible=" + lastVisiblePosition +
					" LastAnimated=" + mLastAnimatedPosition +
					" VisibleItems=" + numberOfItemsOnScreen +
					" childCount=" + mRecyclerView.getChildCount());
		}

		return delay;
	}

	/* CONFIGURATION SETTERS */

	/**
	 * Customize the initial delay for the first item animation.<br/>
	 * Default is 0ms.
	 *
	 * @param initialDelay Any non negative delay
	 */
	public void setAnimationInitialDelay(long initialDelay) {
		mInitialDelay = initialDelay;
	}

	/**
	 * Customize the step delay between an animation and the next to be added to the initial delay.<br/>
	 * The delay is added on top of the previous delay.<br/>
	 * Default is 100ms.
	 *
	 * @param delay Any positive delay
	 */
	public void setAnimationDelay(@IntRange(from = 0) long delay) {
		mStepDelay = delay;
	}

	/**
	 * Customize the duration of the animation for ALL items.<br/>
	 * Default is 300ms.
	 *
	 * @param duration any positive time
	 */
	public void setAnimationDuration(@IntRange(from = 1) long duration) {
		mDuration = duration;
	}

	/**
	 * Define a custom interpolator for ALL items.<br/>
	 * Default is {@link LinearInterpolator}.
	 *
	 * @param interpolator any valid non null interpolator
	 */
	public void setAnimationInterpolator(@NonNull Interpolator interpolator) {
		mInterpolator = interpolator;
	}

	/**
	 * Define an initial start animation adapter position.<br/>
	 * Default is 0 (1st position).
	 *
	 * @param start Non negative minimum position to start animation.
	 */
	public void setAnimationStartPosition(@IntRange(from = 0) int start) {
		mLastAnimatedPosition = start;
	}

	/**
	 * Enable reverse animation depending on user scrolling.<br/>
	 * Default disabled (only forward).
	 *
	 * @param enabled false to animate items only forward, true to reverse animate
	 */
	public void setAnimateOnReverseScrolling(boolean enabled) {
		isReverseEnabled = enabled;
		if (enabled) {
			//Get notified when item is changed (should skip animation)
			mNotifierObserver = new AnimatorAdapterDataObserver();
			registerAdapterDataObserver(mNotifierObserver);
		} else if (mNotifierObserver != null) {
			//Remove observer notification
			unregisterAdapterDataObserver(mNotifierObserver);
			mNotifierObserver = null;
		}
	}

	/**
	 * Enable/Disable item animation on loading.<br/>
	 * Default enabled.
	 *
	 * @param enabled true to enable item animation, false to disable.
	 */
	public void setAnimationEnabled(boolean enabled) {
		shouldAnimate = enabled;
	}

	/* ANIMATORS */

	/**
	 * This is the default animator.<br/>
	 * Alpha animator will be always added automatically if not done yet.
	 * <br/><br/>
	 * <b>Note:</b> Only 1 animator of the same compatible type can be added.<br/>
	 * Incompatible with ALPHA animator.
	 *
	 * @param animators user defined list
	 * @param view      ItemView to animate
	 * @param alphaFrom starting alpha value
	 */
	public void addAlphaAnimator(
			@NonNull List<Animator> animators, @NonNull View view, @FloatRange(from = 0.0, to = 1.0) float alphaFrom) {
		if (animatorsUsed.contains(AnimatorEnum.ALPHA)) return;
		animators.add(ObjectAnimator.ofFloat(view, "alpha", alphaFrom, 1f));
		animatorsUsed.add(AnimatorEnum.ALPHA);
	}

	/**
	 * Item will slide from Left to Right.<br/>
	 * Ignored if LEFT, RIGHT or BOTTOM animators were already added.
	 * <br/><br/>
	 * <b>Note:</b> Only 1 animator of the same compatible type can be added per time.<br/>
	 * Incompatible with LEFT, BOTTOM animators.
	 *
	 * @param animators user defined list
	 * @param view      ItemView to animate
	 * @param percent   Any % multiplier (between 0 and 1) of the LayoutManager Width
	 */
	public void addSlideInFromLeftAnimator(
			@NonNull List<Animator> animators, @NonNull View view, @FloatRange(from = 0.5, to = 1.0) float percent) {
		if (animatorsUsed.contains(AnimatorEnum.SLIDE_IN_LEFT) ||
				animatorsUsed.contains(AnimatorEnum.SLIDE_IN_RIGHT) ||
				animatorsUsed.contains(AnimatorEnum.SLIDE_IN_BOTTOM)) return;
		animators.add(ObjectAnimator.ofFloat(view, "translationX", -mRecyclerView.getLayoutManager().getWidth() * percent, 0));
		animatorsUsed.add(AnimatorEnum.SLIDE_IN_LEFT);
	}

	/**
	 * Item will slide from Right to Left.<br/>
	 * Ignored if LEFT, RIGHT or BOTTOM animators were already added.
	 * <br/><br/>
	 * <b>Note:</b> Only 1 animator of the same compatible type can be added per time.<br/>
	 * Incompatible with RIGHT, BOTTOM animators.
	 *
	 * @param animators user defined list
	 * @param view      ItemView to animate
	 * @param percent   Any % multiplier (between 0 and 1) of the LayoutManager Width
	 */
	public void addSlideInFromRightAnimator(
			@NonNull List<Animator> animators, @NonNull View view, @FloatRange(from = 0.5, to = 1.0) float percent) {
		if (animatorsUsed.contains(AnimatorEnum.SLIDE_IN_LEFT) ||
				animatorsUsed.contains(AnimatorEnum.SLIDE_IN_RIGHT) ||
				animatorsUsed.contains(AnimatorEnum.SLIDE_IN_BOTTOM)) return;
		animators.add(ObjectAnimator.ofFloat(view, "translationX", mRecyclerView.getLayoutManager().getWidth() * percent, 0));
		animatorsUsed.add(AnimatorEnum.SLIDE_IN_RIGHT);
	}

	/**
	 * Item will slide from Bottom of the screen to its natural position.<br/>
	 * Ignored if LEFT, RIGHT or BOTTOM animators were already added.
	 * <br/><br/>
	 * <b>Note:</b> Only 1 animator of the same compatible type can be added per time.<br/>
	 * Incompatible with LEFT, RIGHT, BOTTOM animators.
	 *
	 * @param animators user defined list
	 * @param view      ItemView to animate
	 */
	public void addSlideInFromBottomAnimator(
			@NonNull List<Animator> animators, @NonNull View view) {
		if (animatorsUsed.contains(AnimatorEnum.SLIDE_IN_LEFT) ||
				animatorsUsed.contains(AnimatorEnum.SLIDE_IN_RIGHT) ||
				animatorsUsed.contains(AnimatorEnum.SLIDE_IN_BOTTOM)) return;
		animators.add(ObjectAnimator.ofFloat(view, "translationY", mRecyclerView.getMeasuredHeight() >> 1, 0));
		animatorsUsed.add(AnimatorEnum.SLIDE_IN_BOTTOM);
	}

	/**
	 * Item will scale.<br/>
	 * Ignored if SCALE animator was already added.
	 * <br/><br/>
	 * <b>Note:</b> Only 1 animator of the same compatible type can be added per time.<br/>
	 * Incompatible with LEFT, RIGHT, BOTTOM animators.
	 *
	 * @param animators user defined list
	 * @param view      ItemView to animate
	 * @param scaleFrom Initial scale value
	 */
	public void addScaleInAnimator(
			@NonNull List<Animator> animators, @NonNull View view, @FloatRange(from = 0.0, to = 1.0) float scaleFrom) {
		if (animatorsUsed.contains(AnimatorEnum.SCALE)) return;
		animators.add(ObjectAnimator.ofFloat(view, "scaleX", scaleFrom, 1f));
		animators.add(ObjectAnimator.ofFloat(view, "scaleY", scaleFrom, 1f));
		animatorsUsed.add(AnimatorEnum.SCALE);
	}

	private class AnimatorAdapterDataObserver extends RecyclerView.AdapterDataObserver {
		private Handler mAnimatorHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
			public boolean handleMessage(Message message) {
				if (DEBUG) Log.d(TAG, "Clear notified for Animation");
				isNotified = false;
				return true;
			}
		});

		private boolean isNotified;

		public boolean isPositionNotified() {
			return isNotified;
		}

		public void removeNotified() {
			mAnimatorHandler.removeCallbacksAndMessages(null);
			mAnimatorHandler.sendMessageDelayed(Message.obtain(mHandler), 200L);
		}

		private void markNotified(int positionStart, int itemCount) {
			isNotified = true;
		}

		@Override
		public void onItemRangeChanged(int positionStart, int itemCount) {
			markNotified(positionStart, itemCount);
		}

		@Override
		public void onItemRangeInserted(int positionStart, int itemCount) {
			markNotified(positionStart, itemCount);
		}

		@Override
		public void onItemRangeRemoved(int positionStart, int itemCount) {
			markNotified(positionStart, itemCount);
		}

		@Override
		public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
			markNotified(fromPosition, itemCount);
		}
	}

	/**
	 * Helper Class to clear Animators List used to avoid multiple Item animation on same
	 * position when fling.
	 */
	private class HelperAnimatorListener implements Animator.AnimatorListener {
		int key;

		HelperAnimatorListener(int key) {
			this.key = key;
		}

		@Override
		public void onAnimationStart(Animator animation) {

		}

		@Override
		public void onAnimationEnd(Animator animation) {
			mAnimators.remove(key);
		}

		@Override
		public void onAnimationCancel(Animator animation) {

		}

		@Override
		public void onAnimationRepeat(Animator animation) {

		}
	}

}