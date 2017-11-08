/*
 * Copyright 2016-2017 Davide Steduto
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
package eu.davidea.flexibleadapter.items;

import android.support.annotation.IntRange;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;

/**
 * Basic interface to manage operations like enabling, selecting, hiding, filtering on items.
 * <p>Implements this interface or use {@link AbstractFlexibleItem}.</p>
 *
 * @author Davide Steduto
 * @see IExpandable
 * @see IFilterable
 * @see IHeader
 * @see IHolder
 * @see ISectionable
 * @since 19/01/2016 Created
 * <br>28/03/2017 Individual item's span size AND {@code shouldNotifyChange}
 * <br>12/05/2017 Simplified {@code createViewHolder} params
 * <br>16/06/2017 Added {@code getBubbleText} method
 * <br>04/09/2017 Added {@code getItemViewType} method
 */
public interface IFlexible<VH extends RecyclerView.ViewHolder> {

	/*---------------*/
    /* BASIC METHODS */
	/*---------------*/

    /**
     * Returns if the Item is enabled.
     *
     * @return (default) true for enabled item, false for disabled one.
     */
    boolean isEnabled();

    /**
     * Setter to change enabled behaviour.
     *
     * @param enabled false to disable all operations on this item
     */
    void setEnabled(boolean enabled);

    /**
     * (Internal usage).
     * When and item has been deleted (with Undo) or has been filtered out by the
     * adapter, then, it has hidden status.
     *
     * @return true for hidden item, (default) false for the shown one.
     */
    boolean isHidden();

    /**
     * Setter to change hidden behaviour. Useful while filtering this item.
     * Default value is false.
     *
     * @param hidden true if this item should remain hidden, false otherwise
     */
    void setHidden(boolean hidden);

    /**
     * Individual item's span size to use only with {@code GridLayoutManager}.
     * <p><b>Note:</b>
     * <ul>
     * <li>Default implementation in {@link AbstractFlexibleItem} already returns 1.</li>
     * <li>Not used when {@code StaggeredGridLayoutManager} is set. With such layout use
     * {@link eu.davidea.viewholders.FlexibleViewHolder#setFullSpan(boolean)}.</li>
     * </ul></p>
     *
     * @param spanCount current column count
     * @param position  the adapter position of the item
     * @return the number of span occupied by the item at position.
     * @since 5.0.0-rc2
     */
    @IntRange(from = 1)
    int getSpanSize(int spanCount, int position);

    /**
     * Called by the FlexibleAdapter when it wants to check if this item should be bound
     * again with new content.
     * <p>You should return {@code true} whether you want this item will be updated because
     * its visual representations will change.</p>
     * <b>Note: </b>This method won't be called if
     * {@link FlexibleAdapter#setNotifyChangeOfUnfilteredItems(boolean)} is disabled.
     * <p>Default value is {@code true}.</p>
     *
     * @param newItem The new item object with the new content
     * @return True will trigger a new binding to display new content, false if the content shown
     * is already the latest data.
     * @since 5.0.0-rc2
     */
    boolean shouldNotifyChange(IFlexible newItem);

	/*--------------------*/
	/* SELECTABLE METHODS */
	/*--------------------*/

    /**
     * Checks if the item can be selected.
     *
     * @return (default) true for a Selectable item, false otherwise
     */
    boolean isSelectable();

    /**
     * Setter to change selectable behaviour.
     *
     * @param selectable false to disable selection on this item
     */
    void setSelectable(boolean selectable);

    /**
     * Custom bubble text for FastScroller.
     * <p>To be called from the implementation of {@code onCreateBubbleText()}. Example:
     * <pre>
     * public String onCreateBubbleText(int position) {
     *     return getItem(position).getBubbleText(position);
     * }</pre></p>
     *
     * @param position the current mapped position
     * @return Any desired value
     * @since 5.0.0-rc3
     */
    String getBubbleText(int position);

	/*-------------------*/
	/* TOUCHABLE METHODS */
	/*-------------------*/

    boolean isDraggable();

    void setDraggable(boolean draggable);

    boolean isSwipeable();

    void setSwipeable(boolean swipeable);

	/*---------------------*/
	/* VIEW HOLDER METHODS */
	/*---------------------*/

    /**
     * Identifies a specific view type for this item, used by FlexibleAdapter to auto-map
     * the ViewTypes.
     * <p><b>HELP:</b> To know how to implement AutoMap for ViewTypes please refer to the
     * FlexibleAdapter <a href="https://github.com/davideas/FlexibleAdapter/wiki">Wiki Page</a>
     * on GitHub.</p>
     *
     * @return user defined item view type identifier or layout reference if not overridden
     * @since 5.0.0-rc3
     */
    int getItemViewType();

    /**
     * Returns the layout resource ID to auto-inflate the View for this item. Optionally, you
     * can assign same layout for multiple item types, but {@link #getItemViewType()} must
     * return <b>unique</b> values!
     * <p><b>NOTE:</b> Should identify a resource Layout reference {@link android.R.layout}.</p>
     *
     * @return layout identifier
     */
    @LayoutRes
    int getLayoutRes();

    /**
     * Delegates the creation of the ViewHolder to the user (AutoMap).
     * <p><b>HELP:</b> To know how to implement AutoMap for ViewTypes please refer to the
     * FlexibleAdapter <a href="https://github.com/davideas/FlexibleAdapter/wiki">Wiki Page</a>
     * on GitHub.</p>
     *
     * @param view    the already inflated view
     * @param adapter the Adapter instance extending {@link FlexibleAdapter}
     * @return a new ViewHolder that holds a View of the given view type
     */
    VH createViewHolder(View view, FlexibleAdapter adapter);

    /**
     * Delegates the binding of this item's data to the given Layout.
     * <p><b>HELP:</b> To know how to implement AutoMap for ViewTypes please refer to the
     * FlexibleAdapter <a href="https://github.com/davideas/FlexibleAdapter/wiki">Wiki Page</a>
     * on GitHub.</p>
     * How to use Payload, please refer to
     * {@link android.support.v7.widget.RecyclerView.Adapter#onBindViewHolder(RecyclerView.ViewHolder, int, List)}.
     *
     * @param adapter  the FlexibleAdapter instance
     * @param holder   the ViewHolder instance
     * @param position the current position
     * @param payloads a non-null list of merged payloads. Can be empty list if requires full update
     */
    void bindViewHolder(FlexibleAdapter adapter, VH holder, int position, List<Object> payloads);

    /**
     * Called when a view created by this adapter has been recycled.
     * <p>A view is recycled when a {@code RecyclerView.LayoutManager} decides that it no longer
     * needs to be attached to its parent RecyclerView. This can be because it has fallen out
     * of visibility or a set of cached views represented by views still attached to the parent
     * RecyclerView.</p>
     * If an item view has large or expensive data bound to it such as large bitmaps, this may be
     * a good place to release those resources.
     *
     * @param adapter  the FlexibleAdapter instance
     * @param holder   the ViewHolder instance being recycled
     * @param position the current position
     */
    void unbindViewHolder(FlexibleAdapter adapter, VH holder, int position);

}