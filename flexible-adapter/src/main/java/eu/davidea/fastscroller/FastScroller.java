/*
 * Copyright 2017 AndroidDeveloperLB, Davide Steduto & Arpinca
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import eu.davidea.flexibleadapter.utils.Log;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Class taken from GitHub, customized and optimized for FlexibleAdapter project.
 *
 * @see <a href="https://github.com/AndroidDeveloperLB/LollipopContactsRecyclerViewFastScroller">
 * github.com/AndroidDeveloperLB/LollipopContactsRecyclerViewFastScroller</a>
 * @since Up to the date 23/01/2016
 * <br>23/01/2016 Added onFastScrollerStateChange in the listener
 * <br>10/03/2017 Added autoHide, bubblePosition, bubbleEnabled, ignoreTouchesOutsideHandle (thanks to @arpinca)
 * <br>22/04/2017 Added minimum scroll threshold
 */
public class FastScroller extends FrameLayout {

    protected static final int TRACK_SNAP_RANGE = 5;
    protected static final int BUBBLE_ANIMATION_DURATION = 300;
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
    protected int height, width, minimumScrollThreshold;

    protected RecyclerView recyclerView;
    protected RecyclerView.LayoutManager layoutManager;
    protected BubbleTextCreator bubbleTextCreator;
    protected List<OnScrollStateChangeListener> scrollStateChangeListeners = new ArrayList<>();

    protected int bubbleAndHandleColor = Color.TRANSPARENT;
    protected long autoHideDelayInMillis;
    protected boolean isInitialized = false;
    protected boolean autoHideEnabled, bubbleEnabled, ignoreTouchesOutsideHandle;

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
            ignoreTouchesOutsideHandle = a.getBoolean(R.styleable.FastScroller_fastScrollerIgnoreTouchesOutsideHandle, false);
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
                if (!isEnabled() || bubble == null || handle.isSelected())
                    return;
                int verticalScrollOffset = recyclerView.computeVerticalScrollOffset();
                int verticalScrollRange = recyclerView.computeVerticalScrollRange();
                float proportion = (float) verticalScrollOffset / ((float) verticalScrollRange - height);
                setBubbleAndHandlePosition(height * proportion);
                // If scroll amount is small, don't show it
                if (minimumScrollThreshold == 0 || dy == 0 || Math.abs(dy) > minimumScrollThreshold || scrollbarAnimator.isAnimating()) {
                    showScrollbar();
                    autoHideScrollbar();
                }
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
        if (onScrollListener != null) this.recyclerView.removeOnScrollListener(onScrollListener);
        this.recyclerView.addOnScrollListener(onScrollListener);
        this.recyclerView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                layoutManager = FastScroller.this.recyclerView.getLayoutManager();
            }
        });

        if (recyclerView.getAdapter() instanceof BubbleTextCreator)
            setBubbleTextCreator((BubbleTextCreator) recyclerView.getAdapter());
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

    public void setBubbleTextCreator(BubbleTextCreator bubbleTextCreator) {
        this.bubbleTextCreator = bubbleTextCreator;
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
     * Layout customization.<br>
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
        bubble = findViewById(bubbleResId);
        if (bubble != null) bubble.setVisibility(INVISIBLE);
        handle = findViewById(handleResId);
        bar = findViewById(R.id.fast_scroller_bar);

        // Animators
        bubbleAnimator = new BubbleAnimator(bubble, BUBBLE_ANIMATION_DURATION);
        scrollbarAnimator = new ScrollbarAnimator(bar, handle, autoHideDelayInMillis, AUTOHIDE_ANIMATION_DURATION);

        // Runtime custom color OR the default (accentColor)
        if (bubbleAndHandleColor != Color.TRANSPARENT) {
            setBubbleAndHandleColor(bubbleAndHandleColor);
        }
    }

    /**
     * Changes the color of the Bubble and Handle views.
     * <p><b>Note:</b> Views are already initialized with accent color at startup.</p>
     *
     * @param color any color
     */
    @SuppressWarnings("deprecation")
    public void setBubbleAndHandleColor(@ColorInt int color) {
        bubbleAndHandleColor = color;

        // BubbleDrawable bubbleAndHandleColor
        if (bubble != null) {
            GradientDrawable bubbleDrawable;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                bubbleDrawable = (GradientDrawable) getResources().getDrawable(R.drawable.fast_scroller_bubble, null);
            } else {
                bubbleDrawable = (GradientDrawable) getResources().getDrawable(R.drawable.fast_scroller_bubble);
            }
            bubbleDrawable.setColor(color);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                bubble.setBackground(bubbleDrawable);
            } else {
                bubble.setBackgroundDrawable(bubbleDrawable);
            }
        }
        // HandleDrawable bubbleAndHandleColor
        if (handle != null) {
            try {
                StateListDrawable stateListDrawable;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
                Log.wtf(e, "Exception while setting Bubble and Handle Color");
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

                if (ignoreTouchesOutsideHandle &&
                        (event.getY() < handle.getY() || event.getY() > handle.getY() + handle.getHeight())) {
                    return false;
                }

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
                autoHideScrollbar();
                return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (recyclerView != null) {
            // #369 - OnScrollListener as counterpart of onDetachedFromWindow
            // Occurs only when a new ViewPager Adapter is changed at runtime
            recyclerView.addOnScrollListener(onScrollListener);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (recyclerView != null) {
            recyclerView.removeOnScrollListener(onScrollListener);
        }
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
     *
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
     * <p>Override this method if you want to do something different when displaying the text in
     * the bubble e.g. display a different text when the result of onCreateBubbleText is empty;
     * OR apply different format depending on the result of onCreateBubbleText.</p>
     * The text can be set by calling:
     * <pre>
     * ...
     * String bubbleTextString = bubbleTextCreator.onCreateBubbleText(index);
     * bubble.setText(bubbleTextString);
     * ...
     * </pre>
     *
     * @param position position of the object that will be reflected in the bubble
     */
    protected void updateBubbleText(int position) {
        if (bubble != null && bubbleEnabled) {
            String bubbleText = bubbleTextCreator.onCreateBubbleText(position);
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
     *
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
     * @see #setMinimumScrollThreshold(int)
     */
    public void setAutoHideEnabled(boolean autoHideEnabled) {
        this.autoHideEnabled = autoHideEnabled;
    }

    public long getAutoHideDelayInMillis() {
        return autoHideDelayInMillis;
    }

    /**
     * Sets the delay in milli-seconds to auto hide the scroller when untouched.
     * <p>Default value is {@value DEFAULT_AUTOHIDE_DELAY_IN_MILLIS}ms.</p>
     *
     * @param autoHideDelayInMillis value in milli-seconds
     */
    public void setAutoHideDelayInMillis(@IntRange(from = 0) long autoHideDelayInMillis) {
        this.autoHideDelayInMillis = autoHideDelayInMillis;
        if (scrollbarAnimator != null) {
            scrollbarAnimator.setDelayInMillis(autoHideDelayInMillis);
        }
    }

    /**
     * If enabled, it ignores touches outside handle.
     * <p>Default value is {@code false}.</p>
     *
     * @param ignoreFlag true to ignore touches outside handle
     * @since 5.0.0-rc2
     */
    public void setIgnoreTouchesOutsideHandle(boolean ignoreFlag) {
        ignoreTouchesOutsideHandle = ignoreFlag;
    }

    /**
     * If set, it ignores small scrolls, so scroller will be shown if enough amount of scroll
     * is reached (it mimics fling gesture).
     * <ul>
     * <li>It works only if AutoHide is enabled.</li>
     * <li>Good values are between 60 and 100 pixel on real devices.</li>
     * </ul>
     * Default value is {@code 0} (scroller is always shown at each scroll amount).
     *
     * @param dy minimum scroll amount to show the scroller.
     * @since 5.0.0-rc2
     */
    public void setMinimumScrollThreshold(@IntRange(from = 0) int dy) {
        minimumScrollThreshold = dy;
    }

    /**
     * Shows the scrollbar with animation.
     *
     * @see #hideScrollbar()
     * @since 5.0.0-rc2
     */
    public void showScrollbar() {
        if (scrollbarAnimator != null) {
            scrollbarAnimator.showScrollbar();
        }
    }

    /**
     * Hides the scrollbar with animation.
     *
     * @see #autoHideScrollbar()
     * @since 5.0.0-rc2
     */
    public void hideScrollbar() {
        if (scrollbarAnimator != null) {
            scrollbarAnimator.hideScrollbar();
        }
    }

    /**
     * Auto-hides the scrollbar with animation.
     *
     * @see #hideScrollbar()
     * @since 5.0.0-rc2
     */
    private void autoHideScrollbar() {
        if (autoHideEnabled) hideScrollbar();
    }

    /**
     * Displays or Hides the {@link FastScroller}.
     * <br>The action is animated.
     *
     * @see #setEnabled(boolean)
     * @since 5.0.0-b1
     */
    public void toggleFastScroller() {
        setEnabled(!isEnabled());
    }

    /**
     * Enable and display the FastScroller OR disable and hide the FastScroller.
     * <p>If {@code autoHide} is enabled, showing the FastScrollbar will trigger autoHide.</p>
     *
     * @param enabled true to enable and show, false to hide with animation.
     * @see #toggleFastScroller()
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
            showScrollbar();
            autoHideScrollbar();
        } else {
            hideScrollbar();
        }
    }

	/*------------*/
	/* INTERFACES */
	/*------------*/

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

    public interface AdapterInterface {
        void setFastScroller(@NonNull FastScroller fastScroller);
    }

	/*----------------*/
	/* DELEGATE CLASS */
	/*----------------*/

    /**
     * This class links the FastScroller to the RecyclerView.
     * To use FastScroller in your existing adapter (Not FlexibleAdapter), follow the steps below:
     * <ol>
     * <li>In your layout, include @layout/fast_scroller after the RecyclerView.</li>
     * <li>Implement {@link FastScroller.AdapterInterface} in your adapter.</li>
     * <li>In your adapter, create a {@link FastScroller.Delegate} and call the delegate methods:
     * {@code onAttachedToRecyclerView}, {@code onDetachedFromRecyclerView} and {@code setFastScroller}.</li>
     * <li>If {@code fastScrollerBubbleEnabled} is true, in your adapter, implement BubbleTextCreator
     * and add the logic to display the label in {@code onCreateBubbleText}.</li>
     * <li>In the fragment/activity call the adapter's {@code setFastScroller} after setting the
     * RecyclerView's adapter.</li>
     * </ol>
     */
    public static class Delegate {

        private static final String TAG = Delegate.class.getSimpleName();
        private static final boolean DEBUG = false;

        private RecyclerView mRecyclerView;
        private FastScroller mFastScroller;

        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            mRecyclerView = recyclerView;
        }

        public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
            mRecyclerView = null;
        }

        /**
         * Displays or Hides the {@link FastScroller} if previously configured.
         * <br>The action is animated.
         *
         * @see #setFastScroller(FastScroller)
         * @since 5.0.0-b1
         */
        public void toggleFastScroller() {
            if (mFastScroller != null) {
                mFastScroller.toggleFastScroller();
            }
        }

        /**
         * @return true if {@link FastScroller} is configured and shown, false otherwise
         * @since 5.0.0-b1
         */
        public boolean isFastScrollerEnabled() {
            return mFastScroller != null && mFastScroller.isEnabled();
        }

        /**
         * @return the current instance of the {@link FastScroller} object
         * @since 5.0.0-b1
         */
        public FastScroller getFastScroller() {
            return mFastScroller;
        }

        /**
         * Sets up the {@link FastScroller} with automatic fetch of accent color.
         * <p><b>IMPORTANT:</b> Call this method after the adapter is added to the RecyclerView.</p>
         * <b>NOTE:</b> If the device has at least Lollipop, the Accent color is fetched, otherwise
         * for previous version, the default value is used.
         *
         * @param fastScroller instance of {@link FastScroller}
         * @since 5.0.0-b6
         */
        @SuppressWarnings("ConstantConditions")
        public void setFastScroller(@NonNull FastScroller fastScroller) {
            if (DEBUG) {
                Log.v(TAG, "Setting FastScroller...");
            }
            if (mRecyclerView == null) {
                throw new IllegalStateException("RecyclerView cannot be null. Setup FastScroller after the Adapter has been added to the RecyclerView.");
            } else if (fastScroller == null) {
                throw new IllegalArgumentException("FastScroller cannot be null. Review the widget ID of the FastScroller.");
            }
            mFastScroller = fastScroller;
            mFastScroller.setRecyclerView(mRecyclerView);
            mFastScroller.setViewsToUse(
                    R.layout.library_fast_scroller_layout,
                    R.id.fast_scroller_bubble,
                    R.id.fast_scroller_handle);
            if (DEBUG) Log.i(TAG, "FastScroller initialized");
        }
    }

}