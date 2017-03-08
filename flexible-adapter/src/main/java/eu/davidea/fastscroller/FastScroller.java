package eu.davidea.fastscroller;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.support.annotation.IdRes;
import android.support.annotation.IntDef;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
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

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Class taken from GitHub, customized and optimized for FlexibleAdapter project.
 *
 * @see <a href="https://github.com/AndroidDeveloperLB/LollipopContactsRecyclerViewFastScroller">
 * github.com/AndroidDeveloperLB/LollipopContactsRecyclerViewFastScroller</a>
 * @since Up to the date 23/01/2016
 * <br/>23/01/2016 Added onFastScrollerStateChange in the listener
 */
public class FastScroller extends FrameLayout {

	private static final int BUBBLE_ANIMATION_DURATION = 300;
	private static final int TRACK_SNAP_RANGE = 5;

	private static final int AUTOHIDE_ANIMATION_DURATION = 300;
	private static final boolean DEFAULT_AUTOHIDE_ENABLED = true;
	private static final int DEFAULT_AUTOHIDE_DELAY_IN_MILLIS = 1000;

	@Retention(SOURCE)
	@IntDef({FastScrollerBubblePosition.ADJACENT, FastScrollerBubblePosition.CENTER})
	protected @interface FastScrollerBubblePosition {
		int ADJACENT = 0;
		int CENTER = 1;
	}

	@FastScrollerBubblePosition
	private static final int DEFAULT_BUBBLE_POSITION = FastScrollerBubblePosition.ADJACENT;

	private TextView bubble;
	private ImageView handle;
	private View bar;
	private int height;
	private int width;
	private boolean isInitialized = false;
	private ObjectAnimator currentAnimator;
	private RecyclerView recyclerView;
	private RecyclerView.LayoutManager layoutManager;
	private BubbleTextCreator bubbleTextCreator;
	private List<OnScrollStateChangeListener> scrollStateChangeListeners = new ArrayList<OnScrollStateChangeListener>();

	private boolean autoHideEnabled;
	private long autoHideDelayInMillis;
	@FastScrollerBubblePosition
	private int bubblePosition;

	private AnimatorSet scrollbarAnimator;

	private final RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
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

	public FastScroller(Context context) {
		super(context);
		init();
	}

	public FastScroller(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public FastScroller(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.FastScroller, 0, 0);
		try {
			autoHideEnabled = a.getBoolean(R.styleable.FastScroller_fastScrollerAutoHideEnabled, DEFAULT_AUTOHIDE_ENABLED);
			if (autoHideEnabled) {
				autoHideDelayInMillis = a.getInteger(R.styleable.FastScroller_fastScrollerAutoHideDelayInMillis, DEFAULT_AUTOHIDE_DELAY_IN_MILLIS);
			}

			//noinspection WrongConstant
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
	}

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

	private void notifyScrollStateChange(boolean scrolling) {
		for (OnScrollStateChangeListener stateChangeListener : scrollStateChangeListeners) {
			stateChangeListener.onFastScrollerStateChange(scrolling);
		}
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
		if (bubble != null) return;//Already inflated
		LayoutInflater inflater = LayoutInflater.from(getContext());
		inflater.inflate(layoutResId, this, true);
		bubble = (TextView) findViewById(bubbleResId);
		if (bubble != null) bubble.setVisibility(INVISIBLE);
		handle = (ImageView) findViewById(handleResId);
		bar = findViewById(R.id.fast_scroller_bar);
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
		width = w;
	}

	@Override
	public boolean onTouchEvent(@NonNull MotionEvent event) {
		if (recyclerView.getAdapter().getItemCount() == 0) {
			return super.onTouchEvent(event);
		}

		int action = event.getAction();
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				if (event.getX() < handle.getX() - ViewCompat.getPaddingStart(handle)) return false;
				if (currentAnimator != null) currentAnimator.cancel();
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

	private void setRecyclerViewPosition(float y) {
		if (recyclerView != null) {
			int itemCount = recyclerView.getAdapter().getItemCount();
			//Calculate proportion
			float proportion;
			if (handle.getY() == 0) {
				proportion = 0f;
			} else if (handle.getY() + handle.getHeight() >= height - TRACK_SNAP_RANGE) {
				proportion = 1f;
			} else {
				proportion = y / (float) height;
			}
			int targetPos = getValueInRange(0, itemCount - 1, (int) (proportion * (float) itemCount));
			//Scroll To Position based on LayoutManager
			if (layoutManager instanceof StaggeredGridLayoutManager) {
				((StaggeredGridLayoutManager) layoutManager).scrollToPositionWithOffset(targetPos, 0);
			} else {
				((LinearLayoutManager) layoutManager).scrollToPositionWithOffset(targetPos, 0);
			}
			//Update bubbleText
			if (bubble != null) {
				String bubbleText = bubbleTextCreator.onCreateBubbleText(targetPos);
				if (bubbleText != null) {
					bubble.setVisibility(View.VISIBLE);
					bubble.setText(bubbleText);
				} else {
					bubble.setVisibility(View.GONE);
				}
			}
		}
	}

	private static int getValueInRange(int min, int max, int value) {
		int minimum = Math.max(min, value);
		return Math.min(minimum, max);
	}

	private void setBubbleAndHandlePosition(float y) {
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

	private void showScrollbar() {
		if (bar == null || handle == null || !autoHideEnabled) {
			return;
		}

		if (scrollbarAnimator != null) {
			scrollbarAnimator.cancel();
		}

		if (bar.getVisibility() == View.INVISIBLE || handle.getVisibility() == View.INVISIBLE) {
			bar.setVisibility(View.VISIBLE);
			handle.setVisibility(View.VISIBLE);

			ObjectAnimator barAnimator = ObjectAnimator.ofFloat(bar, "translationX", 0);
			ObjectAnimator handleAnimator = ObjectAnimator.ofFloat(handle, "translationX", 0);
			scrollbarAnimator = new AnimatorSet();
			scrollbarAnimator.playTogether(barAnimator, handleAnimator);
			scrollbarAnimator.setDuration(AUTOHIDE_ANIMATION_DURATION);
			scrollbarAnimator.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					super.onAnimationEnd(animation);
					scrollbarAnimator = null;
				}

				@Override
				public void onAnimationCancel(Animator animation) {
					super.onAnimationCancel(animation);
					scrollbarAnimator = null;
				}
			});
			scrollbarAnimator.start();
		}
	}

	private void hideScrollbar() {
		if (bar == null || handle == null || !autoHideEnabled) {
			return;
		}

		if (scrollbarAnimator != null) {
			scrollbarAnimator.cancel();
		}

        ObjectAnimator barAnimator = ObjectAnimator.ofFloat(bar, "translationX", bar.getWidth());
        ObjectAnimator handleAnimator = ObjectAnimator.ofFloat(handle, "translationX", handle.getWidth());

		scrollbarAnimator = new AnimatorSet();
		scrollbarAnimator.playTogether(barAnimator, handleAnimator);
		scrollbarAnimator.setDuration(AUTOHIDE_ANIMATION_DURATION);
		scrollbarAnimator.setStartDelay(autoHideDelayInMillis);

		scrollbarAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
				resetScrollbarPosition();
				scrollbarAnimator = null;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
				resetScrollbarPosition();
				scrollbarAnimator = null;
            }
        });
		scrollbarAnimator.start();
    }

    private void resetScrollbarPosition() {
		bar.setVisibility(View.INVISIBLE);
		handle.setVisibility(View.INVISIBLE);
		bar.setTranslationX(0);
		handle.setTranslationX(0);
	}

	public interface BubbleTextCreator {
		String onCreateBubbleText(int pos);
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