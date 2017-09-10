/*
 * Copyright 2016-2017
 * - The Android Open Source Project, for the drawing technique.
 * - Bo Song, for the item spacing technique.
 * - Davide Steduto, for the optimizations and all the rest.
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
package eu.davidea.flexibleadapter.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntRange;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.SparseArray;
import android.view.View;

import java.util.Arrays;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.flexibleadapter.items.ISectionable;
import eu.davidea.flexibleadapter.utils.FlexibleUtils;

/**
 * This item decorator implements identical drawing technique of {@code DividerItemDecorator}
 * from Android API, and at the same time, it adds several useful functionalities:
 * <ul>
 * <li>Supports all RecyclerView LayoutManagers that implement (Linear, Grid or StaggeredGrid).</li>
 * <li>Can add <u>equal</u> space offset at all sides of an item.</li>
 * <li>Can customize the offset for each view type.</li>
 * <li>Can customize the offset at the 4 edges: first and last column / first and last row.</li>
 * <li>Recognizes the sections of FlexibleAdapter and can add gap offset between them.</li>
 * <li>Recognizes the orientation of the current Layout.</li>
 * <li>Supports the default Android divider {@code android.R.attr.listDivider}.</li>
 * <li>Supports a custom divider by {@code DrawableRes id}.</li>
 * <li>Supports drawing the divider over or underneath the items.</li>
 * <li>Supports drawing the divider for specific viewTypes.</li>
 * </ul>
 * <b>Tip:</b> Call the method {@link FlexibleAdapter#invalidateItemDecorations(long)} to rebuild
 * the invalidated offsets due to the changes coming from events like moveItem or any layout
 * change that modifies the order of the items.
 *
 * @author Davide Steduto
 * @since 20/07/2015 Created
 * <br>23/04/2017 Drawing of the divider over or underneath the items
 * <br>26/05/2017 Rewrote the full class to support equal spaces between items in all situations
 * <br>10/09/2017 Optional view types for divider
 */
@SuppressWarnings({"WeakerAccess"})
public class FlexibleItemDecoration extends RecyclerView.ItemDecoration {

    private Context context;
    private SparseArray<ItemDecoration> mDecorations; // viewType -> itemDeco
    private List<Integer> mViewTypes;
    private final ItemDecoration mDefaultDecoration = new ItemDecoration();
    private int mOffset, mSectionOffset;
    private boolean withLeftEdge, withTopEdge, withRightEdge, withBottomEdge;

    protected Drawable mDivider;
    protected final Rect mBounds = new Rect();
    protected boolean mDrawOver;
    protected static final int[] ATTRS = new int[]{
            android.R.attr.listDivider
    };

    /**
     * Constructor which saves the Context to calculate the dpi OR to retrieve the divider later on.
     *
     * @param context current Context, it will be used to calculate dpi OR to retrieve the divider
     * @since 5.0.0-rc2
     */
    public FlexibleItemDecoration(@NonNull Context context) {
        this.context = context;
    }

	/*==========*/
    /* DIVIDERS */
    /*==========*/

    /**
     * Default Android divider will be used.
     *
     * @param viewTypes the specific ViewTypes for which the divider will be drawn OR do not
     *                  specify any to draw divider for all ViewTypes (default behavior).
     * @return this FlexibleItemDecoration instance so the call can be chained
     * @see #withDivider(int, Integer...)
     * @see #withDrawOver(boolean)
     * @see #removeDivider()
     * @since 5.0.0-rc2
     */
    public FlexibleItemDecoration withDefaultDivider(Integer... viewTypes) {
        final TypedArray styledAttributes = context.obtainStyledAttributes(ATTRS);
        mDivider = styledAttributes.getDrawable(0);
        styledAttributes.recycle();
        mViewTypes = Arrays.asList(viewTypes);
        return this;
    }

    /**
     * Custom divider.
     *
     * @param resId     drawable resourceId that should be used as a divider
     * @param viewTypes the specific ViewTypes for which the divider will be drawn OR do not
     *                  specify any to draw divider for all ViewTypes (default behavior).
     * @return this FlexibleItemDecoration instance so the call can be chained
     * @see #withDefaultDivider(Integer...)
     * @see #withDrawOver(boolean)
     * @see #removeDivider()
     * @since 5.0.0-rc2
     */
    public FlexibleItemDecoration withDivider(@DrawableRes int resId, Integer... viewTypes) {
        mDivider = ContextCompat.getDrawable(context, resId);
        mViewTypes = Arrays.asList(viewTypes);
        return this;
    }

    /**
     * Removes any divider previously set.
     *
     * @return this FlexibleItemDecoration instance so the call can be chained
     * @since 5.0.0-rc2
     */
    public FlexibleItemDecoration removeDivider() {
        mDivider = null;
        return this;
    }

    /**
     * Changes the mode to draw the divider.
     * <p>- When {@code false}, any content will be drawn before the item views are drawn, and will
     * thus appear <i>underneath</i> the views.
     * <br>- When {@code true}, any content will be drawn after the item views are drawn, and will
     * thus appear <i>over</i> the views.</p>
     * Default value is false (drawn underneath).
     *
     * @param drawOver true to draw after the item has been added, false to draw underneath the item
     * @return this Divider, so the call can be chained
     * @since 5.0.0-b8
     */
    public FlexibleItemDecoration withDrawOver(boolean drawOver) {
        this.mDrawOver = drawOver;
        return this;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (mDivider != null && !mDrawOver) {
            draw(c, parent);
        }
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (mDivider != null && mDrawOver) {
            draw(c, parent);
        }
    }

    protected void draw(Canvas c, RecyclerView parent) {
        if (parent.getLayoutManager() == null) {
            return;
        }
        if (FlexibleUtils.getOrientation(parent) == RecyclerView.VERTICAL) {
            drawVertical(c, parent);
        } else {
            drawHorizontal(c, parent);
        }
    }

    @SuppressLint("NewApi")
    protected void drawVertical(Canvas canvas, RecyclerView parent) {
        canvas.save();
        final int left;
        final int right;
        if (parent.getClipToPadding()) {
            left = parent.getPaddingLeft();
            right = parent.getWidth() - parent.getPaddingRight();
            canvas.clipRect(left, parent.getPaddingTop(), right,
                    parent.getHeight() - parent.getPaddingBottom());
        } else {
            left = 0;
            right = parent.getWidth();
        }

        final int itemCount = parent.getChildCount();
        for (int i = 0; i < itemCount - 1; i++) {
            final View child = parent.getChildAt(i);
            RecyclerView.ViewHolder viewHolder = parent.getChildViewHolder(child);
            if (shouldDrawDivider(viewHolder)) {
                parent.getDecoratedBoundsWithMargins(child, mBounds);
                final int bottom = mBounds.bottom + Math.round(child.getTranslationY());
                final int top = bottom - mDivider.getIntrinsicHeight();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(canvas);
            }
        }
        canvas.restore();
    }

    @SuppressLint("NewApi")
    protected void drawHorizontal(Canvas canvas, RecyclerView parent) {
        canvas.save();
        final int top;
        final int bottom;
        if (parent.getClipToPadding()) {
            top = parent.getPaddingTop();
            bottom = parent.getHeight() - parent.getPaddingBottom();
            canvas.clipRect(parent.getPaddingLeft(), top,
                    parent.getWidth() - parent.getPaddingRight(), bottom);
        } else {
            top = 0;
            bottom = parent.getHeight();
        }

        final int itemCount = parent.getChildCount();
        for (int i = 0; i < itemCount - 1; i++) {
            final View child = parent.getChildAt(i);
            RecyclerView.ViewHolder viewHolder = parent.getChildViewHolder(child);
            if (shouldDrawDivider(viewHolder)) {
                parent.getLayoutManager().getDecoratedBoundsWithMargins(child, mBounds);
                final int right = mBounds.right + Math.round(child.getTranslationX());
                final int left = right - mDivider.getIntrinsicWidth();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(canvas);
            }
        }
        canvas.restore();
    }

    /**
     * Allows to define for which items the divider should be drawn.
     * <p>Override to define custom logic. By default all viewTypes will have the divider.</p>
     *
     * @param viewHolder the ViewHolder under analysis
     * @return {@code true} to draw the divider, {@code false} to skip the drawing
     */
    protected boolean shouldDrawDivider(RecyclerView.ViewHolder viewHolder) {
        return mViewTypes == null || mViewTypes.contains(viewHolder.getItemViewType());
    }

	/*==============================*/
    /* OFFSET & EDGES CONFIGURATION */
    /*==============================*/

    /**
     * Adds an extra offset at the end of each section.
     * <p>Works only with {@link FlexibleAdapter}.</p>
     *
     * @param sectionOffset the extra offset at the end of each section
     * @return this FlexibleItemDecoration instance so the call can be chained
     * @since 5.0.0-rc2
     */
    public FlexibleItemDecoration withSectionGapOffset(@IntRange(from = 0) int sectionOffset) {
        mSectionOffset = (int) (context.getResources().getDisplayMetrics().density * sectionOffset);
        return this;
    }

    /**
     * Adds an offset to the items at the 4 edges (left, top, right and bottom).
     * <p><b>Note: </b>An offset OR an itemType with offsets must be added.</p>
     * Default value is {@code false} (no edge).
     *
     * @param withEdge true to enable, false otherwise
     * @return this FlexibleItemDecoration instance so the call can be chained
     * @see #withOffset(int)
     * @see #addItemViewType(int, int)
     * @since 5.0.0-rc2
     */
    public FlexibleItemDecoration withEdge(boolean withEdge) {
        withLeftEdge = withTopEdge = withRightEdge = withBottomEdge = withEdge;
        return this;
    }

    /**
     * Adds an offset to the items at the Left edge.
     * <p><b>Note: </b>An offset OR an itemType with offsets must be added.</p>
     * Default value is {@code false} (no edge).
     *
     * @param withLeftEdge true to add offset to the items at the Left of the first column.
     * @return this FlexibleItemDecoration instance so the call can be chained
     * @see #withOffset(int)
     * @see #addItemViewType(int, int)
     * @since 5.0.0-rc2
     */
    public FlexibleItemDecoration withLeftEdge(boolean withLeftEdge) {
        this.withLeftEdge = withLeftEdge;
        return this;
    }

    /**
     * Adds an offset to the items at the Top edge.
     * <p><b>Note: </b>An offset OR an itemType with offsets must be added.</p>
     * Default value is {@code false} (no edge).
     *
     * @param withTopEdge true to add offset to the items at the Top of the first row.
     * @return this FlexibleItemDecoration instance so the call can be chained
     * @see #withOffset(int)
     * @see #addItemViewType(int, int)
     * @since 5.0.0-rc2
     */
    public FlexibleItemDecoration withTopEdge(boolean withTopEdge) {
        this.withTopEdge = withTopEdge;
        return this;
    }

    /**
     * Adds an offset to the items at the Bottom edge.
     * <p><b>Note: </b>An offset OR an itemType with offsets must be added.</p>
     * Default value is {@code false} (no edge).
     *
     * @param withBottomEdge true to add offset to the items at the Bottom of the last row.
     * @return this FlexibleItemDecoration instance so the call can be chained
     * @see #withOffset(int)
     * @see #addItemViewType(int, int)
     * @since 5.0.0-rc2
     */
    public FlexibleItemDecoration withBottomEdge(boolean withBottomEdge) {
        this.withBottomEdge = withBottomEdge;
        return this;
    }

    /**
     * Adds an offset to the items at the Right edge.
     * <p><b>Note: </b>An offset OR an itemType with offsets must be added.</p>
     * Default value is {@code false} (no edge).
     *
     * @param withRightEdge true to add offset to the items at the Right of the last column.
     * @return this FlexibleItemDecoration instance so the call can be chained
     * @see #withOffset(int)
     * @see #addItemViewType(int, int)
     * @since 5.0.0-rc2
     */
    public FlexibleItemDecoration withRightEdge(boolean withRightEdge) {
        this.withRightEdge = withRightEdge;
        return this;
    }

    /**
     * Returns the current general offset in dpi.
     *
     * @return the offset previously set in dpi
     */
    public int getOffset() {
        return (int) (mOffset / context.getResources().getDisplayMetrics().density);
    }

    /**
     * Applies the <u>same</u> physical offset to all sides of the item AND between items.
     *
     * @param offset the offset in dpi to apply
     * @return this FlexibleItemDecoration instance so the call can be chained
     * @since 5.0.0-rc2
     */
    public FlexibleItemDecoration withOffset(@IntRange(from = 0) int offset) {
        mOffset = (int) (context.getResources().getDisplayMetrics().density * offset);
        return this;
    }

    /**
     * Applies the general offset also to the specified viewType.
     * <p>Call {@link #withOffset(int)} to set a general offset.</p>
     *
     * @param viewType the viewType affected
     * @return this FlexibleItemDecoration instance so the call can be chained
     * @see #withOffset(int)
     * @see #addItemViewType(int, int)
     * @see #removeItemViewType(int)
     * @since 5.0.0-rc2
     */
    public FlexibleItemDecoration addItemViewType(@LayoutRes int viewType) {
        return addItemViewType(viewType, -1);
    }

    /**
     * As {@link #addItemViewType(int)} but with custom offset equals to all sides that will
     * affect only this viewType.
     *
     * @param viewType the viewType affected
     * @return this FlexibleItemDecoration instance so the call can be chained
     * @see #withOffset(int)
     * @see #removeItemViewType(int)
     * @since 5.0.0-rc2
     */
    public FlexibleItemDecoration addItemViewType(@LayoutRes int viewType, int offset) {
        return addItemViewType(viewType, offset, offset, offset, offset);
    }

    /**
     * As {@link #addItemViewType(int)} but with custom offset that will affect only this viewType.
     *
     * @param viewType the viewType affected
     * @param left     the offset to the left of the item
     * @param top      the offset to the top of the item
     * @param right    the offset to the right of the item
     * @param bottom   the offset to the bottom of the item
     * @return this FlexibleItemDecoration instance so the call can be chained
     * @see #addItemViewType(int, int)
     * @see #removeItemViewType(int)
     * @since 5.0.0-rc2
     */
    public FlexibleItemDecoration addItemViewType(@LayoutRes int viewType, int left, int top, int right, int bottom) {
        if (mDecorations == null) {
            mDecorations = new SparseArray<>();
        }
        left = (int) (context.getResources().getDisplayMetrics().density * left);
        top = (int) (context.getResources().getDisplayMetrics().density * top);
        right = (int) (context.getResources().getDisplayMetrics().density * right);
        bottom = (int) (context.getResources().getDisplayMetrics().density * bottom);
        mDecorations.put(viewType, new ItemDecoration(left, top, right, bottom));
        return this;
    }

    /**
     * In case a viewType should not have anymore the applied offset.
     *
     * @param viewType the viewType to remove from the decoration management
     * @return this FlexibleItemDecoration instance so the call can be chained
     * @since 5.0.0-rc2
     */
    public FlexibleItemDecoration removeItemViewType(@LayoutRes int viewType) {
        mDecorations.remove(viewType);
        return this;
    }

	/*====================*/
    /* OFFSET CALCULATION */
	/*====================*/

    /**
     * @since 5.0.0-rc2
     */
    @Override
    public void getItemOffsets(final Rect outRect, View view, RecyclerView recyclerView, RecyclerView.State state) {
        int position = recyclerView.getChildAdapterPosition(view);
        // Skip check so on item deleted, offset is kept (only if general offset was set!)
        // if (position == RecyclerView.NO_POSITION) return;

        // Get custom Item Decoration or default
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        int itemType = adapter.getItemViewType(position);
        ItemDecoration deco = getItemDecoration(itemType);

        // No offset set, applies the general offset to this item decoration
        if (!deco.hasOffset()) {
            deco = new ItemDecoration(mOffset);
        }

        // Default values (LinearLayout)
        int spanIndex = 0;
        int spanSize = 1;
        int spanCount = 1;
        int orientation = RecyclerView.VERTICAL;

        if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
            GridLayoutManager.LayoutParams lp = (GridLayoutManager.LayoutParams) view.getLayoutParams();
            spanIndex = lp.getSpanIndex();
            spanSize = lp.getSpanSize();
            GridLayoutManager lm = (GridLayoutManager) recyclerView.getLayoutManager();
            spanCount = lm.getSpanCount(); // Assume that there are spanCount items in this row/column.
            orientation = lm.getOrientation();

        } else if (recyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager.LayoutParams lp = (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();
            spanIndex = lp.getSpanIndex();
            StaggeredGridLayoutManager lm = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
            spanCount = lm.getSpanCount(); // Assume that there are spanCount items in this row/column.
            spanSize = lp.isFullSpan() ? spanCount : 1;
            orientation = lm.getOrientation();
        }

        boolean isFirstRowOrColumn = isFirstRowOrColumn(position, adapter, spanIndex, itemType);
        boolean isLastRowOrColumn = isLastRowOrColumn(position, adapter, spanIndex, spanCount, spanSize, itemType);

        // Reset offset values
        int left = 0, top = 0, right = 0, bottom = 0;

        if (orientation == GridLayoutManager.VERTICAL) {
            int index = spanIndex;
            if (withLeftEdge) index = spanCount - spanIndex;
            left = deco.left * index / spanCount;

            index = (spanCount - (spanIndex + spanSize - 1) - 1);
            if (withRightEdge) index = spanIndex + spanSize;
            right = deco.right * index / spanCount;

            if (isFirstRowOrColumn && (withTopEdge)) {
                top = deco.top;
            }
            if (isLastRowOrColumn) {
                if (withBottomEdge) {
                    bottom = deco.bottom;
                }
            } else {
                bottom = deco.bottom;
            }

        } else {
            int index = spanIndex;
            if (withTopEdge) index = spanCount - spanIndex;
            top = deco.top * index / spanCount;

            index = (spanCount - (spanIndex + spanSize - 1) - 1);
            if (withBottomEdge) index = spanIndex + spanSize;
            bottom = deco.bottom * index / spanCount;

            if (isFirstRowOrColumn && (withLeftEdge)) {
                left = deco.left;
            }
            if (isLastRowOrColumn) {
                if (withRightEdge) {
                    right = deco.right;
                }
            } else {
                right = deco.right;
            }
        }

        outRect.set(left, top, right, bottom);

        applySectionGap(outRect, adapter, position, orientation);
    }

    @NonNull
    private ItemDecoration getItemDecoration(int itemType) {
        ItemDecoration deco = null;
        if (mDecorations != null) {
            deco = mDecorations.get(itemType);
        }
        if (deco == null) {
            deco = mDefaultDecoration;
        }
        return deco;
    }

    private boolean isFirstRowOrColumn(int position, RecyclerView.Adapter adapter, int spanIndex, int itemType) {
        int prePos = position > 0 ? position - 1 : -1;
        // Last position on the last row
        int preRowPos = position > spanIndex ? position - (1 + spanIndex) : -1;
        // isFirstRowOrColumn if one of the following condition is true
        return position == 0 || prePos == -1 || itemType != adapter.getItemViewType(prePos) ||
                preRowPos == -1 || itemType != adapter.getItemViewType(preRowPos);
    }

    private boolean isLastRowOrColumn(int position, RecyclerView.Adapter adapter, int spanIndex, int spanCount, int spanSize, int itemType) {
        int itemCount = adapter.getItemCount();
        int nextPos = position < itemCount - 1 ? position + 1 : -1;
        // First position on the next row
        int nextRowPos = position < itemCount - (spanCount / spanSize - spanIndex) ? position + (spanCount / spanSize - spanIndex) : -1;
        // isLastRowOrColumn if one of the following condition is true
        return position == itemCount - 1 || nextPos == -1 || itemType != adapter.getItemViewType(nextPos) ||
                nextRowPos == -1 || itemType != adapter.getItemViewType(nextRowPos);
    }

    @SuppressWarnings("unchecked")
    private void applySectionGap(Rect outRect, RecyclerView.Adapter adapter, int position, int orientation) {
        // Section Gap Offset
        if (mSectionOffset > 0 && adapter instanceof FlexibleAdapter) {
            FlexibleAdapter flexibleAdapter = (FlexibleAdapter) adapter;
            IFlexible item = flexibleAdapter.getItem(position);

            // - Only ISectionable items can finish with a gap and only if next item is a IHeader item
            // - Important: the check must be done on the bottom of the section, otherwise the
            //   sticky header will jump!
            if (item instanceof ISectionable &&
                    (flexibleAdapter.isHeader(flexibleAdapter.getItem(position + 1)) ||
                            position >= adapter.getItemCount() - 1)) {

                if (orientation == RecyclerView.VERTICAL) {
                    outRect.bottom += mSectionOffset;
                } else {
                    outRect.right += mSectionOffset;
                }
            }
        }
    }

    private static class ItemDecoration {
        private int left, top, right, bottom;

        ItemDecoration() {
            this(-1);
        }

        ItemDecoration(int offset) {
            this(offset, offset, offset, offset);
        }

        ItemDecoration(int left, int top, int right, int bottom) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }

        final boolean hasOffset() {
            return this.top >= 0 || this.left >= 0 || this.right >= 0 || this.bottom >= 0;
        }
    }

}