/*
 * Copyright 2017 Davide Steduto
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
package eu.davidea.flexibleadapter.helpers;

import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewPropertyAnimator;

import eu.davidea.fastscroller.FastScroller;
import eu.davidea.flexibleadapter.FlexibleAdapter;

/**
 * Helper class to show an empty view when Recycler View has no items. This helper supports:
 * <ul>
 * <li>Empty View in normal state.</li>
 * <li>Empty View for no filter result.</li>
 * <li>FastScroller handling if previously set into the Adapter.</li>
 * </ul>
 *
 * @author Davide Steduto
 * @since 26/12/2017 Created in UI package
 * <br>22/04/2018 Added static creator methods
 */
@SuppressWarnings("WeakerAccess")
public class EmptyViewHelper implements FlexibleAdapter.OnUpdateListener, FlexibleAdapter.OnFilterListener {

    private FlexibleAdapter mAdapter;
    private OnEmptyViewListener mEmptyViewListener;
    private View mEmptyDataView;
    private View mEmptyFilterView;

    public static EmptyViewHelper create(FlexibleAdapter adapter, View emptyDataView) {
        return new EmptyViewHelper(adapter, emptyDataView);
    }

    public static EmptyViewHelper create(FlexibleAdapter adapter, View emptyDataView, View emptyFilterView) {
        return new EmptyViewHelper(adapter, emptyDataView, emptyFilterView);
    }

    public static EmptyViewHelper create(FlexibleAdapter adapter, View emptyDataView, View emptyFilterView,
                                       @Nullable OnEmptyViewListener emptyViewListener) {
        return new EmptyViewHelper(adapter, emptyDataView, emptyFilterView, emptyViewListener);
    }

    /**
     * Constructor to initialize both empty data view.
     * <p><b>Note:</b> To better handle FastScroller, initialize this helper class after setting fast scroller
     * to the Adapter.</p>
     *
     * @param adapter       the Adapter instance
     * @param emptyDataView the view for empty updates
     */
    private EmptyViewHelper(FlexibleAdapter adapter, View emptyDataView) {
        this(adapter, emptyDataView, null);
    }

    /**
     * Constructor to initialize both empty views: Empty data and empty filter result.
     * <p><b>Note:</b> To better handle FastScroller, initialize this helper class after setting fast scroller
     * to the Adapter.</p>
     *
     * @param adapter         the Adapter instance
     * @param emptyDataView   the view for empty updates
     * @param emptyFilterView the view for empty filter result
     */
    private EmptyViewHelper(FlexibleAdapter adapter, View emptyDataView, View emptyFilterView) {
        this(adapter, emptyDataView, emptyFilterView, null);
    }

    /**
     * Constructor to initialize both empty views: Empty data and empty filter result with an extra level of
     * callback for more customization.
     * <p><b>Note:</b> To better handle FastScroller, initialize this helper class after setting fast scroller
     * to the Adapter.</p>
     *
     * @param adapter           the Adapter instance
     * @param emptyDataView     the view for empty updates
     * @param emptyFilterView   the view for empty filter result
     * @param emptyViewListener another level of listener callback in case more customization is needed
     */
    private EmptyViewHelper(FlexibleAdapter adapter, View emptyDataView, View emptyFilterView,
                           @Nullable OnEmptyViewListener emptyViewListener) {
        this.mEmptyDataView = emptyDataView;
        this.mEmptyFilterView = emptyFilterView;
        this.mEmptyViewListener = emptyViewListener;
        this.mAdapter = adapter;
        this.mAdapter.addListener(this);
    }

    public View getEmptyDataView() {
        return mEmptyDataView;
    }

    public View getEmptyFilterView() {
        return mEmptyFilterView;
    }

    /**
     * Shows EmptyDataView by animating alpha property to {@code 1}.
     */
    public final void showEmptyDataView() {
        showView(mEmptyDataView);
    }

    /**
     * Hides EmptyDataView by setting Alpha property to {@code 0}.</p>
     */
    public final void hideEmptyDataView() {
        hideView(mEmptyDataView);
    }

    /**
     * Shows EmptyFilterView by animating alpha property to {@code 1}.
     */
    public final void showEmptyFilterView() {
        showView(mEmptyFilterView);
    }

    /**
     * Hides EmptyFilterView by setting Alpha property to {@code 0}.</p>
     */
    public final void hideEmptyFilterView() {
        hideView(mEmptyFilterView);
    }

    @Override
    public final void onUpdateEmptyView(int size) {
        FastScroller fastScroller = mAdapter.getFastScroller();
        hideEmptyFilterView();
        if (size > 0) {
            hideEmptyDataView();
            if (fastScroller != null && fastScroller.isEnabled()) fastScroller.showScrollbar();
        } else if (mEmptyDataView != null && mEmptyDataView.getAlpha() == 0) {
            showEmptyDataView();
            if (fastScroller != null && !fastScroller.isHidden()) fastScroller.hideScrollbar();
        }
        if (mEmptyViewListener != null) {
            mEmptyViewListener.onUpdateEmptyDataView(size);
        }
    }

    @Override
    public final void onUpdateFilterView(int size) {
        FastScroller fastScroller = mAdapter.getFastScroller();
        hideEmptyDataView();
        if (size > 0) {
            hideEmptyFilterView();
            if (fastScroller != null && fastScroller.isEnabled()) fastScroller.showScrollbar();
        } else if (mEmptyFilterView != null && mEmptyFilterView.getAlpha() == 0) {
            showEmptyFilterView();
            if (fastScroller != null && !fastScroller.isHidden()) fastScroller.hideScrollbar();
        }
        if (mEmptyViewListener != null) {
            mEmptyViewListener.onUpdateEmptyFilterView(size);
        }
    }

    private static void showView(View view) {
        if (view != null) {
            ViewPropertyAnimator animator = view.animate();
            animator.cancel();
            animator.alpha(1);
        }
    }

    public static void hideView(View view) {
        if (view != null) {
            ViewPropertyAnimator animator = view.animate();
            animator.cancel();
            animator.alpha(0);
        }
    }

    /**
     * @since 26/12/2017
     */
    public interface OnEmptyViewListener {
        /**
         * Called at startup and every time a main item is inserted or removed.
         * <p><b>Note:</b> Having any Scrollable Headers/Footers visible, the {@code size}
         * will represents only the <b>main</b> items.</p>
         *
         * @param size the current number of <b>main</b> items in the adapter, result of
         *             {@link FlexibleAdapter#getMainItemCount()}
         * @since 1.0.0-b2
         */
        void onUpdateEmptyDataView(int size);

        /**
         * Called at each filter request.
         *
         * @param size the current number of <b>filtered</b> items.
         * @since 1.0.0-b2
         */
        void onUpdateEmptyFilterView(int size);
    }

}