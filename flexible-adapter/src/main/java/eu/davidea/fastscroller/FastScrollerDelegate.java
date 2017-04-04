package eu.davidea.fastscroller;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import eu.davidea.flexibleadapter.R;

/**
 * This class links the RecyclerView and FastScroller.
 * To use FastScroller in your existing adapter, follow the steps below:
 * <ol>
 *     <li>In your layout, include @layout/fast_scroller after the RecyclerView.</li>
 *     <li>Implement FastScrollerAdapterInterface in your adapter.</li>
 *     <li>In your adapter, create a FastScrollerDelegate and call the delegate functions onAttachedToRecyclerView, onDetachedFromRecyclerView, and setFastScroller.</li>
 *     <li>If fastScrollerBubbleEnabled is true, in your adapter, implement BubbleTextCreator and add the logic to display the label in onCreateBubbleText.</li>
 *     <li>In the fragment/activity using the RecyclerView, call the adapter's setFastScroller after setting the RecyclerView's adapter.</li>
 * </ol>
 */
public class FastScrollerDelegate {

    private static final String TAG = FastScrollerDelegate.class.getSimpleName();
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
            if (mFastScroller.isHidden()) {
                mFastScroller.showScrollbar();
            }
            else {
                mFastScroller.hideScrollbar();
            }
        }
    }

    /**
     * @return true if {@link FastScroller} is configured and shown, false otherwise
     * @since 5.0.0-b1
     */
    public boolean isFastScrollerEnabled() {
        return mFastScroller != null && mFastScroller.getVisibility() == View.VISIBLE;
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
     * @param fastScroller        instance of {@link FastScroller}
     * @since 5.0.0-b6
     */
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
        if (DEBUG) {
            Log.i(TAG, "FastScroller initialized with color " + mFastScroller.getBubbleAndHandleColor());
        }
    }
}
