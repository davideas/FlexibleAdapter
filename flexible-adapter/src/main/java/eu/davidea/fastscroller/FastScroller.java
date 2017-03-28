package eu.davidea.fastscroller;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.IdRes;
import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import eu.davidea.flexibleadapter.R;
import eu.davidea.flexibleadapter.utils.Utils;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Class taken from GitHub, customized and optimized for FlexibleAdapter project.
 *
 * @see <a href="https://github.com/AndroidDeveloperLB/LollipopContactsRecyclerViewFastScroller">
 * github.com/AndroidDeveloperLB/LollipopContactsRecyclerViewFastScroller</a>
 * @since Up to the date 23/01/2016
 * <br/>23/01/2016 Added onFastScrollerStateChange in the listener
 * <br/>10/03/2017 Added autoHide, bubblePosition (thanks to @arpinca)
 */
public class FastScroller extends FrameLayout {

	protected static final int BUBBLE_ANIMATION_DURATION = 300;
	protected static final int TRACK_SNAP_RANGE = 5;

	protected static final int AUTOHIDE_ANIMATION_DURATION = 300;
	protected static final int DEFAULT_AUTOHIDE_DELAY_IN_MILLIS = 1000;
	protected static final boolean DEFAULT_AUTOHIDE_ENABLED = true;

	@Retention(SOURCE)
	@IntDef({FastScrollerBubblePosition.ADJACENT, FastScrollerBubblePosition.CENTER})
	public @interface FastScrollerBubblePosition {
		int ADJACENT = 0;
		int CENTER = 1;
	}

	@FastScrollerBubblePosition
	private static final int DEFAULT_BUBBLE_POSITION = FastScrollerBubblePosition.ADJACENT;

	protected TextView bubble;
	protected ImageView handle;
	protected View bar;
	protected int height;
	protected int width;

	@Deprecated
	protected int bubbleAndHandleColor;

	protected boolean isInitialized = false;

	protected RecyclerView recyclerView;
	protected RecyclerView.LayoutManager layoutManager;
	protected BubbleTextCreator bubbleTextCreator;
	protected List<OnScrollStateChangeListener> scrollStateChangeListeners = new ArrayList<>();

	protected boolean autoHideEnabled;
	protected long autoHideDelayInMillis;

	protected boolean bubbleEnabled;

	@FastScrollerBubblePosition
	protected int bubblePosition;

	protected BubbleAnimator bubbleAnimator;
	protected ScrollbarAnimator scrollbarAnimator;

	protected RecyclerView.OnScrollListener onScrollListener;

	/*--------------*/
	/* CONSTRUCTORS */
	/*--------------*/

	public FastScroller(Context context) {
		super(context);
		init();
	}

	public FastScroller(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	@SuppressWarnings("WrongConstant")
	public FastScroller(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.FastScroller, 0, 0);
		try {
			autoHideEnabled = a.getBoolean(R.styleable.FastScroller_fastScrollerAutoHideEnabled, DEFAULT_AUTOHIDE_ENABLED);
			autoHideDelayInMillis = a.getInteger(R.styleable.FastScroller_fastScrollerAutoHideDelayInMillis, DEFAULT_AUTOHIDE_DELAY_IN_MILLIS);
			bubbleEnabled = a.getBoolean(R.styleable.FastScroller_fastScrollerBubbleEnabled, true);
			bubblePosition = a.getInteger(R.styleable.FastScroller_fastScrollerBubblePosition, DEFAULT_BUBBLE_POSITION);
		} finally {
			a.recycle();
		}

		init();
	}

	protected void init() {
		if (isInitialized) return;
		isInitialized = true;
		setClipChildren(false);

		onScrollListener = new RecyclerView.OnScrollListener() {

			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				if (bubble == null || handle.isSelected())
					return;
				int verticalScrollOffset = recyclerView.computeVerticalScrollOffset();
				int verticalScrollRange = recyclerView.computeVerticalScrollRange();
				float proportion = (float) verticalScrollOffset / ((float) verticalScrollRange - height);
				setBubbleAndHandlePosition(height * proportion);
				showScrollbar();
				hideScrollbar();
			}
		};
	}

	/*---------------*/
	/* CONFIGURATION */
	/*---------------*/

	/**
	 * This is done by FlexibleAdapter already!
	 */
	public void setRecyclerView(final RecyclerView recyclerView) {
		this.recyclerView = recyclerView;
		this.recyclerView.addOnScrollListener(onScrollListener);
		this.recyclerView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
			@Override
			public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
				layoutManager = FastScroller.this.recyclerView.getLayoutManager();
			}
		});

		if (recyclerView.getAdapter() instanceof BubbleTextCreator)
			this.bubbleTextCreator = (BubbleTextCreator) recyclerView.getAdapter();
		if (recyclerView.getAdapter() instanceof OnScrollStateChangeListener)
			addOnScrollStateChangeListener((OnScrollStateChangeListener) recyclerView.getAdapter());

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

	public void addOnScrollStateChangeListener(OnScrollStateChangeListener stateChangeListener) {
		if (stateChangeListener != null && !scrollStateChangeListeners.contains(stateChangeListener))
			scrollStateChangeListeners.add(stateChangeListener);
	}

	public void removeOnScrollStateChangeListener(OnScrollStateChangeListener stateChangeListener) {
		scrollStateChangeListeners.remove(stateChangeListener);
	}

	protected void notifyScrollStateChange(boolean scrolling) {
		for (OnScrollStateChangeListener stateChangeListener : scrollStateChangeListeners) {
			stateChangeListener.onFastScrollerStateChange(scrolling);
		}
	}

	/**
	 * Layout customization.<br/>
	 * Color for Selected State is the bubbleAndHandleColor defined inside the Drawables.
	 *
	 * @param layoutResId Main layout of Fast Scroller
	 * @param bubbleResId Drawable resource for Bubble containing the Text
	 * @param handleResId Drawable resource for the Handle
	 */
	public void setViewsToUse(@LayoutRes int layoutResId, @IdRes int bubbleResId, @IdRes int handleResId) {
		if (bubble != null) return; //Already inflated
		LayoutInflater inflater = LayoutInflater.from(getContext());
		inflater.inflate(layoutResId, this, true);
		bubble = (TextView) findViewById(bubbleResId);
		if (bubble != null) bubble.setVisibility(INVISIBLE);
		handle = (ImageView) findViewById(handleResId);
		bar = findViewById(R.id.fast_scroller_bar);

		bubbleAnimator = new BubbleAnimator(bubble, BUBBLE_ANIMATION_DURATION);
		scrollbarAnimator = new ScrollbarAnimator(bar, handle, autoHideDelayInMillis, AUTOHIDE_ANIMATION_DURATION);
	}

	/**
	 * Layout customization<br/>
	 * Color for Selected State is also customized by the user.
	 *
	 * @param layoutResId Main layout of Fast Scroller
	 * @param bubbleResId Drawable resource for Bubble containing the Text
	 * @param handleResId Drawable resource for the Handle
	 * @param accentColor Color for Selected state during touch and scrolling (usually accent bubbleAndHandleColor)
	 * @deprecated Accent Color is automatically fetched at startup
	 */
	@Deprecated
	public void setViewsToUse(@LayoutRes int layoutResId, @IdRes int bubbleResId, @IdRes int handleResId, @ColorInt int accentColor) {
		setViewsToUse(layoutResId, bubbleResId, handleResId);
		setBubbleAndHandleColor(accentColor);
	}

	public int getBubbleAndHandleColor() {
		return bubbleAndHandleColor;
	}

	/**
	 * Changes the color of the Bubble and Handle views.
	 * <p><b>Note:</b> Views are already initialized with accent color at startup.</p>
	 *
	 * @param color any color
	 */
	@SuppressWarnings("deprecation")
	public void setBubbleAndHandleColor(@ColorInt int color) {
		this.bubbleAndHandleColor = color;

		// BubbleDrawable bubbleAndHandleColor
		if (bubble != null) {
			GradientDrawable bubbleDrawable;
			if (Utils.hasLollipop()) {
				bubbleDrawable = (GradientDrawable) getResources().getDrawable(R.drawable.fast_scroller_bubble, null);
			} else {
				bubbleDrawable = (GradientDrawable) getResources().getDrawable(R.drawable.fast_scroller_bubble);
			}
			bubbleDrawable.setColor(color);
			if (Utils.hasJellyBean()) {
				bubble.setBackground(bubbleDrawable);
			} else {
				bubble.setBackgroundDrawable(bubbleDrawable);
			}
		}
		// HandleDrawable bubbleAndHandleColor
		if (handle != null) {
			try {
				StateListDrawable stateListDrawable;
				if (Utils.hasLollipop()) {
					stateListDrawable = (StateListDrawable) getResources().getDrawable(R.drawable.fast_scroller_handle, null);
				} else {
					stateListDrawable = (StateListDrawable) getResources().getDrawable(R.drawable.fast_scroller_handle);
				}
				// Method is still hidden, invoke Java reflection
				Method getStateDrawable = StateListDrawable.class.getMethod("getStateDrawable", int.class);
				GradientDrawable handleDrawable = (GradientDrawable) getStateDrawable.invoke(stateListDrawable, 0);
				handleDrawable.setColor(color);
				handle.setImageDrawable(stateListDrawable);
			} catch (Exception e) {
				// This should never happen in theory (Java Reflection Exception)
				Log.e(FastScroller.class.getSimpleName(), "Exception while setting Bubble and Handle Color", e);
			}
		}
	}

	/*--------------*/
	/* MAIN METHODS */
	/*--------------*/

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		height = h;
		width = w;
	}

	@Override
	public boolean onTouchEvent(@NonNull MotionEvent event) {
		if (recyclerView.computeVerticalScrollRange() <= recyclerView.computeVerticalScrollExtent()) {
			return super.onTouchEvent(event);
		}

		int action = event.getAction();
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				if (event.getX() < handle.getX() - ViewCompat.getPaddingStart(handle)) return false;

				handle.setSelected(true);
				notifyScrollStateChange(true);
				showBubble();
				showScrollbar();
			case MotionEvent.ACTION_MOVE:
				float y = event.getY();
				setBubbleAndHandlePosition(y);
				setRecyclerViewPosition(y);
				return true;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				handle.setSelected(false);
				notifyScrollStateChange(false);
				hideBubble();
				hideScrollbar();
				return true;
		}
		return super.onTouchEvent(event);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (recyclerView != null)
			recyclerView.removeOnScrollListener(onScrollListener);
	}

	protected void setRecyclerViewPosition(float y) {
		if (recyclerView != null) {
			int targetPos = getTargetPos(y);

			if (layoutManager instanceof StaggeredGridLayoutManager) {
				((StaggeredGridLayoutManager) layoutManager).scrollToPositionWithOffset(targetPos, 0);
			} else {
				((LinearLayoutManager) layoutManager).scrollToPositionWithOffset(targetPos, 0);
			}

			updateBubbleText(targetPos);
		}
	}

	/**
	 * Computes the index where the RecyclerView should be scrolled to based on the currY (y-coordinate of the touch event in the scrollbar)
	 * @param currY y-coordinate of the touch event in the scrollbar
	 * @return index position in the RecyclerView
	 */
	protected int getTargetPos(float currY) {
		int itemCount = recyclerView.getAdapter().getItemCount();

		float proportion;
		if (handle.getY() == 0) {
			proportion = 0f;
		} else if (handle.getY() + handle.getHeight() >= height - TRACK_SNAP_RANGE) {
			proportion = 1f;
		} else {
			proportion = currY / (float) height;
		}
		return getValueInRange(0, itemCount - 1, (int) (proportion * (float) itemCount));
	}

	/**
	 * Update the text in the bubble based on the provided index.
	 * The text can be retrieved by calling
	 * <pre>
	 * {@code
	 * ...
	 * String bubbleTextString = bubbleTextCreator.onCreateBubbleText(index);
	 * bubble.setText(bubbleTextString);
	 * ...
	 * }
	 * </pre>
	 *
	 * Override this method if you want to do something different when displaying the text in the bubble e.g. display a different text when the result of onCreateBubbleText is empty; OR apply different format depending on the result of onCreateBubbleText.
	 * @param index index of the object that will be reflected in the bubble
	 */
	protected void updateBubbleText(int index) {
		if (bubble != null && bubbleEnabled) {
			String bubbleText = bubbleTextCreator.onCreateBubbleText(index);
			if (bubbleText != null) {
				bubble.setVisibility(View.VISIBLE);
				bubble.setText(bubbleText);
			} else {
				bubble.setVisibility(View.GONE);
			}
		}
	}

	protected static int getValueInRange(int min, int max, int value) {
		int minimum = Math.max(min, value);
		return Math.min(minimum, max);
	}

	/**
	 * Sets the y-position of the bubble and the handle based on the current y-position.
	 * Override this method if you want to adjust the min and max position of the handle and the bubble e.g. the max position of the bubble is the same as the max position of the handle.
	 * @param y current active y position in the scrollbar
	 */
	protected void setBubbleAndHandlePosition(float y) {
		int handleHeight = handle.getHeight();
		handle.setY(getValueInRange(0, height - handleHeight, (int) (y - handleHeight / 2)));
		if (bubble != null) {
			int bubbleHeight = bubble.getHeight();
			if (bubblePosition == FastScrollerBubblePosition.ADJACENT) {
				bubble.setY(getValueInRange(0, height - bubbleHeight - handleHeight / 2, (int) (y - bubbleHeight)));
			} else {
				bubble.setY(Math.max(0, (height - bubble.getHeight()) / 2));
				bubble.setX(Math.max(0, (width - bubble.getWidth()) / 2));
			}
		}
	}

	/*------------*/
	/* ANIMATIONS */
	/*------------*/

	protected void showBubble() {
		if (bubbleEnabled) {
			bubbleAnimator.showBubble();
		}
	}

	protected void hideBubble() {
		bubbleAnimator.hideBubble();
	}

	/*-----------*/
	/* AUTO-HIDE */
	/*-----------*/

	public boolean isHidden() {
		return bar == null || handle == null ||
				bar.getVisibility() == View.INVISIBLE ||
				handle.getVisibility() == View.INVISIBLE;
	}

	public boolean isAutoHideEnabled() {
		return autoHideEnabled;
	}

	/**
	 * Allows to auto hide the FastScroller after x-milliseconds.
	 * <p>Default value is {@code true}.</p>
	 *
	 * @param autoHideEnabled true to enable auto Hide, false to disable
	 * @see #setAutoHideDelayInMillis(long)
	 */
	public void setAutoHideEnabled(boolean autoHideEnabled) {
		this.autoHideEnabled = autoHideEnabled;
	}

	public long getAutoHideDelayInMillis() {
		return autoHideDelayInMillis;
	}

	/**
	 * Sets the delay in milli-seconds to auto hide the scroller when untouched.
	 * <p>Default value is {@link #DEFAULT_AUTOHIDE_DELAY_IN_MILLIS}</p>
	 *
	 * @param autoHideDelayInMillis value in milli-seconds
	 */
	public void setAutoHideDelayInMillis(@IntRange(from = 0) long autoHideDelayInMillis) {
		this.autoHideDelayInMillis = autoHideDelayInMillis;
		scrollbarAnimator.setDelayInMillis(autoHideDelayInMillis);
	}

	/**
	 * Shows the scrollbar with animation.
	 *
	 * @since 5.0.0-rc2
	 */
	public void showScrollbar() {
		if (autoHideEnabled) {
			scrollbarAnimator.showScrollbar();
		}
	}

	/**
	 * Hides the scrollbar with animation.
	 *
	 * @since 5.0.0-rc2
	 */
	public void hideScrollbar() {
		if (autoHideEnabled) {
			scrollbarAnimator.hideScrollbar();
		}
    }

	public interface BubbleTextCreator {
		String onCreateBubbleText(int position);
	}

	public interface OnScrollStateChangeListener {
		/**
		 * Called when scrolling state changes.
		 *
		 * @param scrolling true if the user is actively scrolling, false when idle
		 */
		void onFastScrollerStateChange(boolean scrolling);
	}

}