package eu.davidea.fastscroller;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.Method;

import eu.davidea.flexibleadapter.R;

public class FastScroller extends FrameLayout {

	private static final int BUBBLE_ANIMATION_DURATION = 300;
	private static final int TRACK_SNAP_RANGE = 5;

	private TextView bubble;
	private ImageView handle;
	private RecyclerView recyclerView;
	private int height;
	private boolean isInitialized = false;
	private ObjectAnimator currentAnimator = null;

	private final RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
		@Override
		public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
			if (bubble == null || handle.isSelected())
				return;
			int verticalScrollOffset = recyclerView.computeVerticalScrollOffset();
			int verticalScrollRange = recyclerView.computeVerticalScrollRange();
			float proportion = (float) verticalScrollOffset / ((float) verticalScrollRange - height);
			setBubbleAndHandlePosition(height * proportion);
		}
	};


	public FastScroller(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public FastScroller(Context context) {
		super(context);
		init();
	}

	public FastScroller(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	protected void init() {
		if (isInitialized) return;
		isInitialized = true;
		setClipChildren(false);
	}

	/**
	 * Layout customization.<br/>
	 * Color for Selected State is the color defined inside the Drawables.
	 *
	 * @param layoutResId Main layout of Fast Scroller
	 * @param bubbleResId Drawable resource for Bubble containing the Text
	 * @param handleResId Drawable resource for the Handle
	 */
	public void setViewsToUse(@LayoutRes int layoutResId, @IdRes int bubbleResId, @IdRes int handleResId) {
		LayoutInflater inflater = LayoutInflater.from(getContext());
		inflater.inflate(layoutResId, this, true);
		bubble = (TextView) findViewById(bubbleResId);
		if (bubble != null) bubble.setVisibility(INVISIBLE);
		handle = (ImageView) findViewById(handleResId);
	}

	/**
	 * Layout customization<br/>
	 * Color for Selected State is also customized by the user.
	 *
	 * @param layoutResId Main layout of Fast Scroller
	 * @param bubbleResId Drawable resource for Bubble containing the Text
	 * @param handleResId Drawable resource for the Handle
	 * @param accentColor Color for Selected state during touch and scrolling (usually accent color)
	 */
	public void setViewsToUse(@LayoutRes int layoutResId, @IdRes int bubbleResId, @IdRes int handleResId, int accentColor) {
		setViewsToUse(layoutResId, bubbleResId, handleResId);
		setBubbleAndHandleColor(accentColor);
	}

	private void setBubbleAndHandleColor(int accentColor) {
		//TODO: Programmatically generate the Drawables instead of using resources
		//BubbleDrawable accentColor
		GradientDrawable bubbleDrawable;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			bubbleDrawable = (GradientDrawable) getResources().getDrawable(R.drawable.fast_scroller_bubble, null);
		} else {
			//noinspection deprecation
			bubbleDrawable = (GradientDrawable) getResources().getDrawable(R.drawable.fast_scroller_bubble);
		}
		assert bubbleDrawable != null;
		bubbleDrawable.setColor(accentColor);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			bubble.setBackground(bubbleDrawable);
		} else {
			//noinspection deprecation
			bubble.setBackgroundDrawable(bubbleDrawable);
		}

		//HandleDrawable accentColor
		try {
			StateListDrawable stateListDrawable;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				stateListDrawable = (StateListDrawable) getResources().getDrawable(R.drawable.fast_scroller_handle, null);
			} else {
				//noinspection deprecation
				stateListDrawable = (StateListDrawable) getResources().getDrawable(R.drawable.fast_scroller_handle);
			}
			//Method is still hidden, invoke Java reflection
			Method getStateDrawable = StateListDrawable.class.getMethod("getStateDrawable", int.class);
			GradientDrawable handleDrawable = (GradientDrawable) getStateDrawable.invoke(stateListDrawable, 0);
			handleDrawable.setColor(accentColor);
			handle.setImageDrawable(stateListDrawable);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		height = h;
	}

	@Override
	public boolean onTouchEvent(@NonNull MotionEvent event) {
		int action = event.getAction();
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				if (event.getX() < handle.getX() - ViewCompat.getPaddingStart(handle)) return false;
				if (currentAnimator != null) currentAnimator.cancel();
				handle.setSelected(true);
				((ScrollerListener) recyclerView.getAdapter()).onFastScroll(true);
				showBubble();
			case MotionEvent.ACTION_MOVE:
				float y = event.getY();
				setBubbleAndHandlePosition(y);
				setRecyclerViewPosition(y);
				return true;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				handle.setSelected(false);
				((ScrollerListener) recyclerView.getAdapter()).onFastScroll(false);
				hideBubble();
				return true;
		}
		return super.onTouchEvent(event);
	}

	public void setRecyclerView(RecyclerView recyclerView) {
		this.recyclerView = recyclerView;
		this.recyclerView.addOnScrollListener(onScrollListener);
		this.recyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				FastScroller.this.recyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
				if (bubble == null || handle.isSelected()) return true;
				int verticalScrollOffset = FastScroller.this.recyclerView.computeVerticalScrollOffset();
				int verticalScrollRange = FastScroller.this.computeVerticalScrollRange();
				float proportion = (float) verticalScrollOffset / ((float) verticalScrollRange - height);
				setBubbleAndHandlePosition(height * proportion);
				return true;
			}
		});
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (recyclerView != null)
			recyclerView.removeOnScrollListener(onScrollListener);
	}

	private void setRecyclerViewPosition(float y) {
		if (recyclerView != null) {
			int itemCount = recyclerView.getAdapter().getItemCount();
			float proportion;
			if (handle.getY() == 0)
				proportion = 0f;
			else if (handle.getY() + handle.getHeight() >= height - TRACK_SNAP_RANGE)
				proportion = 1f;
			else
				proportion = y / (float) height;
			int targetPos = getValueInRange(0, itemCount - 1, (int) (proportion * (float) itemCount));
			String bubbleText = ((ScrollerListener) recyclerView.getAdapter()).getTextToShowInBubble(targetPos);
			((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(targetPos, 0);
			if (bubble != null)
				bubble.setText(bubbleText);
		}
	}

	private int getValueInRange(int min, int max, int value) {
		int minimum = Math.max(min, value);
		return Math.min(minimum, max);
	}

	private void setBubbleAndHandlePosition(float y) {
		int handleHeight = handle.getHeight();
		handle.setY(getValueInRange(0, height - handleHeight, (int) (y - handleHeight / 2)));
		if (bubble != null) {
			int bubbleHeight = bubble.getHeight();
			bubble.setY(getValueInRange(0, height - bubbleHeight - handleHeight / 2, (int) (y - bubbleHeight)));
		}
	}

	private void showBubble() {
		if (bubble != null && bubble.getVisibility() != VISIBLE) {
			bubble.setVisibility(VISIBLE);
			if (currentAnimator != null)
				currentAnimator.cancel();
			currentAnimator = ObjectAnimator.ofFloat(bubble, "alpha", 0f, 1f).setDuration(BUBBLE_ANIMATION_DURATION);
			currentAnimator.start();
		}
	}

	private void hideBubble() {
		if (bubble == null)
			return;
		if (currentAnimator != null)
			currentAnimator.cancel();
		currentAnimator = ObjectAnimator.ofFloat(bubble, "alpha", 1f, 0f).setDuration(BUBBLE_ANIMATION_DURATION);
		currentAnimator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				bubble.setVisibility(INVISIBLE);
				currentAnimator = null;
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				super.onAnimationCancel(animation);
				bubble.setVisibility(INVISIBLE);
				currentAnimator = null;
			}
		});
		currentAnimator.start();
	}

	public interface ScrollerListener {
		String getTextToShowInBubble(int pos);

		void onFastScroll(boolean scrolling);
	}

}