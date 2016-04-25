/*
 * Copyright 2015-2016 Davide Steduto & Martin Guillon
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
package eu.davidea.flexibleadapter.section;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.CallSuper;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ListAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import eu.davidea.flexibleadapter.AnimatorAdapter;
import eu.davidea.flexibleadapter.helpers.ItemTouchHelperCallback;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * FlexibleAdapter class customized by Martin Guillon.
 * Not avaliable to users. Taking inspiration for some functionalities.
 */
@SuppressWarnings({"unchecked"})
abstract class FlexibleAdapterSections extends AnimatorAdapter
		implements ItemTouchHelperCallback.AdapterCallback {

	private static final String TAG = FlexibleAdapterSections.class.getSimpleName();
	private static final String EXTRA_HEADERS = TAG + "_headersShown";
	private LayoutManager mLayoutManager;

	/**
	 * Section Feature
	 */
	private final Context mContext;
	private SectionAdapter mSectionAdapter;
	private SectionPositionTranslator mPositionTranslator;
	private boolean headersShown = false, headersSticky = false;
	private GridLayoutManager.SpanSizeLookup gridSpanSizeLookup;
	private GridLayoutManager.SpanSizeLookup externalSpanSizeLookup;
	private StickySectionHeaderManager stickyHeaderManager;

	/**
	 * Header/Footer
	 */
	public static final int ITEM_VIEW_TYPE_HEADER_OR_FOOTER = -2;

	private ArrayList<FixedViewInfo> mHeaderViewInfos = null;
	private ArrayList<FixedViewInfo> mFooterViewInfos = null;

	/**
	 * Used to save deleted items and to recover them (Undo).
	 */
	private boolean restoreSelection = false;

	/* Drag&Drop and Swipe helpers */
	private boolean longPressDragEnabled = false, handleDragEnabled = true, swipeEnabled = false;
	private ItemTouchHelperCallback mItemTouchHelperCallback;
	private ItemTouchHelper mItemTouchHelper;

	/* Filter */
	private String mSearchText = "", mOldSearchText = "";
	private boolean mNotifyChangeOfUnfilteredItems = false, filtering = false;
	private Set<Integer> currentlyHiddenItems = null;

	/* Listeners */
	public OnItemClickListener mItemClickListener;
	public OnItemLongClickListener mItemLongClickListener;
	protected OnItemMoveListener mItemMoveListener;
	protected OnItemSwipeListener mItemSwipeListener;
	protected OnStickyHeaderChangeListener mStickyHeaderChangeListener;

	/**
	 * Handler for delayed {@link #filterItems(List)} and
	 * {@link OnDeleteCompleteListener#onDeleteConfirmed}
	 * <p>
	 * You can override this Handler, but you must keep the "What" already used:
	 * <br/>
	 * 0 = filterItems delay <br/>
	 * 1 = deleteConfirmed when Undo timeout is over
	 * </p>
	 */
	protected Handler mHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
		public boolean handleMessage(Message message) {
			switch (message.what) {
//				case 0: // filterItems
//					filterItems(0, getItemCount());
//					return true;
//				case 1: //confirm delete
//					OnDeleteCompleteListener listener = (OnDeleteCompleteListener) message.obj;
//					if (listener != null) listener.onDeleteConfirmed();
//					emptyBin();
//					return true;
			}
			return false;
		}
	});

    /*--------------*/
	/* CONSTRUCTORS */
	/*--------------*/

	/**
	 * Main Constructor with all managed listeners for ViewHolder and the
	 * Adapter itself.
	 * <p>
	 * The listener must be a single instance of a class, usually
	 * <i>Activity</i> or <i>Fragment</i>, where you can implement how to handle
	 * the different events.
	 * </p>
	 * Any write operation performed on the items list is <u>synchronized</u>.
	 * <p>
	 * <b>PASS ALWAYS A <u>COPY</u> OF THE ORIGINAL LIST</b>: <i>new
	 * ArrayList&lt;T&gt;(originalList);</i>
	 * </p>
	 *
	 * @param items     items to display
	 * @param listeners can be an instance of: <br/>
	 *                  - {@link OnUpdateListener} <br/>
	 *                  - {@link OnItemClickListener} <br/>
	 *                  - {@link OnItemLongClickListener} <br/>
	 *                  - {@link OnItemMoveListener} <br/>
	 *                  - {@link OnItemSwipeListener}
	 */
	public FlexibleAdapterSections(final Context context, @Nullable Object listeners) {
		mContext = context;// needed to create empty HeaderViewHolder
		if (this instanceof SectionAdapter) {
			mSectionAdapter = (SectionAdapter) this;
			mPositionTranslator = new SectionPositionTranslator();
			mPositionTranslator.build(mSectionAdapter, true);
		}

		if (listeners instanceof OnItemClickListener)
			mItemClickListener = (OnItemClickListener) listeners;
		if (listeners instanceof OnItemLongClickListener)
			mItemLongClickListener = (OnItemLongClickListener) listeners;
		if (listeners instanceof OnItemMoveListener)
			mItemMoveListener = (OnItemMoveListener) listeners;
		if (listeners instanceof OnItemSwipeListener)
			mItemSwipeListener = (OnItemSwipeListener) listeners;
		if (listeners instanceof OnStickyHeaderChangeListener)
			mStickyHeaderChangeListener = (OnStickyHeaderChangeListener) listeners;

	}

    /*------------------------------*/
	/* SELECTION METHODS OVERRIDDEN */
	/*------------------------------*/

	// to be overriden
	public boolean isEnabled(int position) {
		return true;
	}

	@Override
	public void toggleSelection(@IntRange(from = 0) int position) {
		if (isSelectable(position)) {
			super.toggleSelection(position);
		}
	}

	/**
	 * Helper to automatically select all the items of the viewType equal to the
	 * viewType of the first selected item.
	 * <p>
	 * Examples: <br/>
	 * - if user initially selects an expandable of type A, then only expandable
	 * items of type A will be selected. <br/>
	 * - if user initially selects a non expandable of type B, then only items
	 * of Type B will be selected. <br/>
	 * - The developer can override this behaviour by passing a list of
	 * viewTypes for which he wants to force the selection.
	 * </p>
	 *
	 * @param viewTypes All the desired viewTypes to be selected, pass nothing to
	 *                  automatically select all the viewTypes of the first item user
	 *                  selected
	 */
	@Override
	public void selectAll(Integer... viewTypes) {
		if (getSelectedItemCount() > 0 && viewTypes.length == 0) {
			super.selectAll(getItemViewType(getSelectedPositions().get(0)));// Priority on the first item
		} else {
			super.selectAll(viewTypes);// Force the selection for the viewTypes passed
		}
	}

    /*---------------*/
	/* HEADER FOOTER */
	/*---------------*/

	public void addHeaderView(View v) {
		if (mHeaderViewInfos == null) {
			mHeaderViewInfos = new ArrayList<>();
		}
		addHeaderOrFooterView(v, mHeaderViewInfos);
	}

	public void addFooterView(View v) {
		if (mFooterViewInfos == null) {
			mFooterViewInfos = new ArrayList<>();
		}
		addHeaderOrFooterView(v, mFooterViewInfos);
	}

	public boolean removeHeader(View v) {
		return removeHeaderOrFooterView(v, mHeaderViewInfos);
	}

	public boolean removeFooter(View v) {
		return removeHeaderOrFooterView(v, mFooterViewInfos);
	}

	private void addHeaderOrFooterView(View v, ArrayList list) {
		final FixedViewInfo info = new FixedViewInfo();
		info.view = v;
		info.data = null;
		info.isSelectable = false;

		list.add(info);
		notifyDataSetChanged();//Davideas: Here we can improve by accepting also the position of the info.data and notifyItemInserted(position)
	}

	private boolean removeHeaderOrFooterView(View v, ArrayList<FixedViewInfo> list) {
		if (list != null) {
			for (int i = 0; i < list.size(); i++) {
				FixedViewInfo info = list.get(i);
				if (info.view == v) {
					list.remove(i);
					notifyDataSetChanged();//Davideas: Here we can improve with getItem(info.data) and then the position of that item, notifyItemRemoved(position)
					return true;
				}
			}
		}
		return false;
	}

	public int getHeadersCount() {
		if (mHeaderViewInfos != null) {
			return mHeaderViewInfos.size();
		}
		return 0;
	}

	public int getFootersCount() {
		if (mFooterViewInfos != null) {
			return mFooterViewInfos.size();
		}
		return 0;
	}

    /*--------------*/
	/* MAIN METHODS */
	/*--------------*/

	protected int getRealItemCount() {
		if (mPositionTranslator != null) {
			return mPositionTranslator.getItemCount();
		}
		return 0;
	}

	@Override
	public int getItemCount() {
		return getRealItemCount() + getFootersCount() + getHeadersCount();
	}

	@Override
	public long getItemId(int position) {
		int numHeaders = getHeadersCount();
		if (position >= numHeaders) {
			int adjPosition = position - numHeaders;
			int adapterCount = getRealItemCount();
			if (adjPosition < adapterCount) {
				position = adjPosition;
				if (mPositionTranslator != null) {
					final long expandablePosition = mPositionTranslator.getExpandablePosition(position);
					final int sectionIndex = SectionAdapterHelper.getPackedPositionSection(expandablePosition);
					final int sectionItemIndex = SectionAdapterHelper.getPackedPositionChild(expandablePosition);

					if (sectionItemIndex == RecyclerView.NO_POSITION) {
						final long groupId = mSectionAdapter.getSectionId(sectionIndex);
						return SectionAdapterHelper.getCombinedSectionId(groupId);
					} else {
						final long groupId = mSectionAdapter.getSectionId(sectionIndex);
						final long childId = mSectionAdapter.getChildId(sectionIndex, sectionItemIndex);
						if (childId == RecyclerView.NO_ID) {
							return RecyclerView.NO_ID;
						}
						return SectionAdapterHelper.getCombinedChildId(groupId, childId);
					}
				}
			}
		}
		return RecyclerView.NO_ID;
	}

	public long getSectionId(int sectionIndex) {
		if (mSectionAdapter != null) {
			return mSectionAdapter.getSectionId(sectionIndex);
		}
		return RecyclerView.NO_ID;
	}

	/**
	 * You can override this method to define your own concept of "Empty". This
	 * method is never called internally.
	 * <p>
	 * Default value is the result of {@link #getItemCount()}.
	 * </p>
	 *
	 * @return true if the list is empty, false otherwise
	 * @see #getItemCount()
	 * @see #getItemCountOfTypes(Integer...)
	 */
	public boolean isEmpty() {
		return getItemCount() == 0;
	}

    /*--------------------------*/
	/* HEADERS/SECTIONS METHODS */
	/*--------------------------*/

	public boolean isHeader(int position) {
		if (headersShown) {
			final long expandablePosition = mPositionTranslator.getExpandablePosition(position);
			final int sectionItemIndex = SectionAdapterHelper.getPackedPositionChild(expandablePosition);
			return sectionItemIndex == RecyclerView.NO_POSITION;
		}
		return false;
	}

	/**
	 * @return true if all headers are currently displayed, false otherwise
	 */
	public boolean areHeadersShown() {
		return headersShown;
	}

	/**
	 * Sets if all headers should be shown at startup. To call before setting
	 * the headers!
	 * <p>
	 * Default value is false.
	 * </p>
	 *
	 * @param displayHeaders true to display them, false to keep them hidden
	 * @return this adapter so the call can be chained
	 */
	public FlexibleAdapterSections setDisplayHeaders(boolean displayHeaders) {
		headersShown = displayHeaders;
		return this;
	}

	/**
	 * Returns if Adapter will display sticky headers on the top.
	 *
	 * @return true if headers can be sticky, false if headers are scrolled
	 * together with all items
	 */
	public boolean areHeadersSticky() {
		return headersSticky;
	}

	/**
	 * Enables the sticky header functionality. Adds
	 * {@link StickySectionHeaderDecoration} to the RecyclerView.
	 * <p>
	 * Headers can be sticky only if they are shown. Command is otherwise
	 * ignored!
	 * </p>
	 * <b>NOTE:</b> Sticky headers cannot be dragged, swiped, moved nor deleted.
	 * They, instead, are automatically re-linked if the linked Sectionable item
	 * is different.
	 *
	 * @param maxCachedHeaders the max view instances to keep in the cache. This number
	 *                         depends by how many headers are normally displayed in the
	 *                         RecyclerView. It depends by the specific use case.
	 */
	public void enableStickySectionHeaders() {
		setStickySectionHeaders(true);
	}

	/**
	 * Disables the sticky header functionality. Clears the cache and removes
	 * the {@link StickySectionHeaderDecoration} from the RecyclerView.
	 */
	public void disableStickySectionHeaders() {
		setStickySectionHeaders(false);
	}

	public void setStickySectionHeaders(boolean headersSticky) {
		if (mSectionAdapter == null) {
			// makes no sense when not using sections
			return;
		}
		// Add or Remove the sticky headers decoration
		if (headersSticky) {
			this.headersSticky = true;
			if (stickyHeaderManager == null) {
				stickyHeaderManager = new StickySectionHeaderManager(this);
			}
			if (headersShown && mRecyclerView != null) {
				stickyHeaderManager.attachToRecyclerView(mRecyclerView);
				stickyHeaderManager.setStickyHeadersHolder(getStickySectionHeadersHolder());
			}
		} else if (stickyHeaderManager != null) {
			this.headersSticky = false;
			if (mRecyclerView != null) {
				stickyHeaderManager.detachFromRecyclerView(mRecyclerView);
			}
			stickyHeaderManager.setStickyHeadersHolder(null);
			stickyHeaderManager = null;
		}
	}

	/**
	 * Shows all headers in the RecyclerView at their linked position.
	 * <p>
	 * Headers can be shown or hidden all together.
	 * </p>
	 *
	 * @see #hideAllHeaders()
	 */
	public void showAllHeaders() {
		headersShown = true;
		notifyDataSetChanged();//Davideas: we can improve this by retrieving the correct positions maybe with an abstract method
	}

	/**
	 * Hides all headers from the RecyclerView.
	 * <p>
	 * Headers can be shown or hidden all together.
	 * </p>
	 *
	 * @see #showAllHeaders()
	 */
	public void hideAllHeaders() {
		headersShown = false;
		notifyDataSetChanged();//Davideas: we can improve this by retrieving the correct positions maybe with an abstract method
	}

	@Override
	public void onAttachedToRecyclerView(RecyclerView recyclerView) {
		super.onAttachedToRecyclerView(recyclerView);
		//Setup the spanSizeLookup if any
		setLayoutManager(recyclerView.getLayoutManager());

		if (mItemTouchHelper != null) {
			mItemTouchHelper.attachToRecyclerView(mRecyclerView);
		}
		if (stickyHeaderManager != null) {
			stickyHeaderManager.setStickyHeadersHolder(getStickySectionHeadersHolder());
			if (headersShown) {
				stickyHeaderManager.attachToRecyclerView(mRecyclerView);
			}
		}
	}

	@Override
	public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
		if (stickyHeaderManager != null) {
			stickyHeaderManager.setStickyHeadersHolder(null);
			stickyHeaderManager.detachFromRecyclerView(mRecyclerView);
			stickyHeaderManager = null;
		}
		super.onDetachedFromRecyclerView(recyclerView);
	}

	public void setSpanSizeLookup(GridLayoutManager.SpanSizeLookup spanSizeLookup) {
		externalSpanSizeLookup = spanSizeLookup;
	}

	public void setLayoutManager(final LayoutManager layoutManager) {
		if (layoutManager instanceof GridLayoutManager) {
			gridSpanSizeLookup = new GridLayoutManager.SpanSizeLookup() {
				@Override
				public int getSpanSize(int position) {
					if (isHeader(position)) {
						return ((GridLayoutManager) layoutManager).getSpanCount();
					}
					if (externalSpanSizeLookup != null) {
						return externalSpanSizeLookup.getSpanSize(position);
					}
					return 1;
				}
			};
			((GridLayoutManager) layoutManager).setSpanSizeLookup(gridSpanSizeLookup);
		} else {
			gridSpanSizeLookup = null;
		}
	}

    /*---------------------*/
    /* VIEW HOLDER METHODS */
    /*---------------------*/

	private static int HEADER_TYPE_FLAG = 0x80000000;

	// if you return that type FlexibleAdapter will hande it as a section
	// without a header
	protected static int SECTION_NO_HEADER_VIEW_TYPE = 0x08000000;

	/**
	 * Returns the view type for the header
	 *
	 * @param sectionIndex     section index
	 * @param sectionItemIndex child index in the section
	 */
	public abstract int getSectionViewType(int position, int sectionIndex, int sectionItemIndex);

	/**
	 * Returns the ViewHolder for a header
	 *
	 * @param parent   the RecyclerView
	 * @param viewType the ViewType
	 */
	public abstract RecyclerView.ViewHolder onCreateSectionHeaderViewHolder(
			ViewGroup parent, int viewType);

	/**
	 * Returns the ViewHolder for a header
	 *
	 * @param parent   the RecyclerView
	 * @param viewType the ViewType
	 */
	public abstract RecyclerView.ViewHolder onCreateSectionChildViewHolder(
			ViewGroup parent, int viewType);

	/**
	 * Bind a header view
	 *
	 * @param holder           the ViewHolder
	 * @param sectionIndex     section index
	 * @param sectionItemIndex child index in the section
	 */
	public abstract void onBindSectionViewHolder(ViewHolder holder, int position,
												 int sectionIndex, int sectionItemIndex,
												 boolean selected);

	@Override
	public int getItemViewType(int position) {
		int numHeaders = getHeadersCount();
		if (position >= numHeaders) {
			int adjPosition = position - numHeaders;
			int adapterCount = getRealItemCount();
			if (adjPosition < adapterCount) {
				position = adjPosition;
				if (mSectionAdapter != null) {
					final long expandablePosition = mPositionTranslator.getExpandablePosition(position);
					final int sectionIndex = SectionAdapterHelper.getPackedPositionSection(expandablePosition);
					final int sectionItemIndex = SectionAdapterHelper.getPackedPositionChild(expandablePosition);

					int result = getSectionViewType(position, sectionIndex, sectionItemIndex);
					if (sectionItemIndex == RecyclerView.NO_POSITION) {
						result |= HEADER_TYPE_FLAG;
					}
					return result;
				} else {
					return -1;
				}
			}
		}

		return ITEM_VIEW_TYPE_HEADER_OR_FOOTER;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

		if (viewType == ITEM_VIEW_TYPE_HEADER_OR_FOOTER) {
			HeaderViewHolder holder = new HeaderViewHolder(parent.getContext());
			holder.setIsRecyclable(false);

			return holder;
		}
		// TODO: how to handle non section adapter?
		if (viewType != -1 && (viewType & HEADER_TYPE_FLAG) != 0) {
			viewType &= ~HEADER_TYPE_FLAG;
			if (viewType == SECTION_NO_HEADER_VIEW_TYPE) {
				return new StickyHeaderViewHolder(mContext, null);
			} else {
				return new StickyHeaderViewHolder(mContext, onCreateSectionHeaderViewHolder(parent, viewType));
			}
		}
		return onCreateSectionChildViewHolder(parent, viewType);
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		// Header (negative positions will throw an IndexOutOfBoundsException)
		int numHeaders = getHeadersCount();
		if (position < numHeaders) {
			if (holder instanceof HeaderViewHolder) {
				HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
				final View view = mHeaderViewInfos.get(position).view;
				if (view.getParent() != headerHolder.layout) {
					headerHolder.layout.removeAllViews();
					if (view.getParent() instanceof ViewGroup) {
						((ViewGroup) view.getParent()).removeView(view);
					}
					headerHolder.layout.addView(view);
				}
				return;
			}
		}

		// Adapter
		final int adjPosition = position - numHeaders;
		int adapterCount = getRealItemCount();
		if (adjPosition < adapterCount) {
			if (mSectionAdapter != null) {
				final long expandablePosition = mPositionTranslator.getExpandablePosition(adjPosition);
				final int sectionIndex = SectionAdapterHelper.getPackedPositionSection(expandablePosition);
				final int sectionItemIndex = SectionAdapterHelper.getPackedPositionChild(expandablePosition);
//				Davideas: Seems unused cast
//				if (holder instanceof StickyHeaderViewHolder) {
//					holder = ((StickyHeaderViewHolder) holder).realItemHolder;
//				}
				if (holder != null) {
					// When user scrolls, this line binds the correct selection status
					final boolean isSelected = isSelected(adjPosition);
					holder.itemView.setActivated(isSelected);
					onBindSectionViewHolder(holder, adjPosition, sectionIndex, sectionItemIndex, isSelected);
					return;
				}

			}
		}
		if (mFooterViewInfos != null && holder instanceof HeaderViewHolder) {
			HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
			final View view = mFooterViewInfos.get(adjPosition - adapterCount).view;
			if (view.getParent() != headerHolder.layout && view.getParent() instanceof ViewGroup) {
				((ViewGroup) view.getParent()).removeView(view);
			}
			headerHolder.layout.removeAllViews();
			headerHolder.layout.addView(view);
		}
	}

	public void onStickyHeaderChange(int sectionIndex) {
		if (mStickyHeaderChangeListener != null) {
			mStickyHeaderChangeListener.onStickyHeaderChange(sectionIndex);
		}
	}

	/**
	 * Returns the current configuration to restore selections on Undo.
	 *
	 * @return true if selection will be restored, false otherwise
	 */
	public boolean isRestoreWithSelection() {
		return restoreSelection;
	}

	/**
	 * Gives the possibility to restore the selection on Undo, when
	 * {@link #restoreDeletedItems()} is called.
	 * <p>
	 * To use in combination with {@code ActionMode} in order to not disable it.
	 * </p>
	 * Default value is false.
	 *
	 * @param restoreSelection true to have restored items still selected, false to empty
	 *                         selections.
	 */
	public void setRestoreSelectionOnUndo(boolean restoreSelection) {
		this.restoreSelection = restoreSelection;
	}

    /*----------------*/
    /* FILTER METHODS */
    /*----------------*/

	public boolean hasSearchText() {
		return mSearchText != null && mSearchText.length() > 0;
	}

	public String getSearchText() {
		return mSearchText;
	}

	public void setSearchText(String searchText) {
		if (searchText != null)
			mSearchText = searchText.trim().toLowerCase(Locale.getDefault());
		else
			mSearchText = "";
	}

	/**
	 * <b>WATCH OUT! PASS ALWAYS A <u>COPY</u> OF THE ORIGINAL LIST</b>: due to
	 * internal mechanism, items are removed and/or added in order to animate
	 * items in the final list.
	 * <p>
	 * Same as {@link #filterItems(List)}, but with a delay in the execution,
	 * useful to grab more characters from user before starting the search.
	 * </p>
	 *
	 * @param unfilteredItems the list to filter
	 * @param delay           any non negative delay
	 * @see #filterObject(IFlexible, String)
	 */
	public void filterItems(@IntRange(from = 0) long delay) {
		// Make longer the timer for new coming deleted items
		mHandler.removeMessages(0);
		mHandler.sendMessageDelayed(Message.obtain(mHandler, 0), delay > 0 ? delay : 0);
	}

	/**
	 * <b>WATCH OUT! PASS ALWAYS A <u>COPY</u> OF THE ORIGINAL LIST</b>: due to
	 * internal mechanism, items are removed and/or added in order to animate
	 * items in the final list.
	 * <p>
	 * This method filters the provided list with the search text previously set
	 * with {@link #setSearchText(String)}.
	 * </p>
	 * <b>Note:</b> <br/>
	 * - This method calls {@link #filterObject(IFlexible, String)}. <br/>
	 * - If search text is empty or null, the provided list is the current list.
	 * <br/>
	 * - Any pending deleted items are always filtered out, but if restored,
	 * they will be displayed according to the current filter and in the correct
	 * positions. <br/>
	 * - <b>NEW!</b> Expandable items are picked up and displayed if at least a
	 * child is collected by the current filter. <br/>
	 * - <b>NEW!</b> Items are animated thanks to {@link #animateTo(List)}.
	 *
	 * @param unfilteredItems the list to filter
	 * @see #filterObject(IFlexible, String)
	 */
	public synchronized void filteritems(int startPosition, int count) {
		// enable flag: skip adjustpositions!
		filtering = true;
		if (hasSearchText()) {
			if (mOldSearchText.equalsIgnoreCase(mSearchText)) {
				mOldSearchText = mSearchText;
				return;
			}
			if (currentlyHiddenItems == null) {
				currentlyHiddenItems = new HashSet<Integer>();
			}
			for (int i = startPosition; i < count; i++) {
				if (shouldFilter(i, mSearchText)) {
					if (currentlyHiddenItems.add(i)) {
						notifyItemInserted(i);
					}
				} else if (currentlyHiddenItems.remove(i)) {
					notifyItemRemoved(i);
				}
			}
		} else if (currentlyHiddenItems != null) {
			applyAndAnimateRemovals(currentlyHiddenItems);
			currentlyHiddenItems = null;
		}

		// reset filtering flag
		filtering = false;
	}

	/**
	 * animate removal
	 */
	protected void applyAndAnimateAdditions(Set<Integer> hide) {
		for (Integer i : hide) {
			notifyItemInserted(i);
		}
	}

	/**
	 * animate insertion
	 */
	protected void applyAndAnimateRemovals(Set<Integer> hide) {
		for (Integer i : hide) {
			notifyItemRemoved(i);
		}
	}

	/**
	 * This method checks if the provided object is a type of
	 * {@link IFilterable} interface, if yes, performs the filter on the
	 * implemented method {@link IFilterable#filter(String)}.
	 * <p/>
	 * <b>NOTE:</b> <br/>
	 * - The item will be collected if the implemented method returns true.
	 * <br/>
	 * - {@code IExpandable} items are automatically picked up and displayed if
	 * at least a child is collected by the current filter: however, you also
	 * need to implement {@code IFilterable} interface on the
	 * {@code IExpandable} item and on the child type. What you DON'T NEED to
	 * implement is the scan for the children: this is already done :-) <br/>
	 * - If you don't want to implement the {@code IFilterable} interface on the
	 * items, then you can override this method to have another filter logic!
	 *
	 * @param item       the object to be inspected
	 * @param constraint constraint, that the object has to fulfil
	 * @return true, if the object returns true as well, and so if it should be
	 * in the filteredResult, false otherwise
	 */
	protected boolean shouldFilter(int position, String constraint) {
//		if (item instanceof IFilterable) {
//			IFilterable filterable = (IFilterable) item;
//			return filterable.filter(constraint);
//		}
		if (mSectionAdapter != null) {

		}
		return false;
	}

    /*---------------*/
    /* TOUCH METHODS */
    /*---------------*/

	/**
	 * Used by {@link FlexibleViewHolder#onTouch(View, MotionEvent)} to start
	 * Drag when HandleView is touched.
	 *
	 * @return the ItemTouchHelper instance already initialized.
	 */
	public final ItemTouchHelper getItemTouchHelper() {
		initializeItemTouchHelper();
		return mItemTouchHelper;
	}

	/**
	 * Returns whether ItemTouchHelper should start a drag and drop operation if
	 * an item is long pressed.
	 * <p/>
	 * Default value returns false.
	 *
	 * @return true if ItemTouchHelper should start dragging an item when it is
	 * long pressed, false otherwise. Default value is false.
	 */
	public boolean isLongPressDragEnabled() {
		return mItemTouchHelperCallback != null && mItemTouchHelperCallback.isLongPressDragEnabled();
	}

	/**
	 * Enable the Drag on LongPress on the entire ViewHolder.
	 * <p>
	 * <b>NOTE:</b> This will skip LongClick on the view in order to handle the
	 * LongPress, however the LongClick listener will be called if necessary in
	 * the new {@link FlexibleViewHolder#onActionStateChanged(int, int)}.
	 * </p>
	 * Default value is false.
	 *
	 * @param longPressDragEnabled true to activate, false otherwise
	 */
	public final void setLongPressDragEnabled(boolean longPressDragEnabled) {
		initializeItemTouchHelper();
		this.longPressDragEnabled = longPressDragEnabled;
		mItemTouchHelperCallback.setLongPressDragEnabled(longPressDragEnabled);
	}

	/**
	 * Enabled by default.
	 * <p>
	 * To use, it is sufficient to set the HandleView by calling
	 * {@link FlexibleViewHolder#setDragHandleView(View)}.
	 * </p>
	 *
	 * @return true if active, false otherwise
	 */
	public boolean isHandleDragEnabled() {
		return handleDragEnabled;
	}

	/**
	 * Enable/Disable the drag with handle.
	 * <p>
	 * Default value is true.
	 * </p>
	 *
	 * @param handleDragEnabled true to activate, false otherwise
	 */
	public void setHandleDragEnabled(boolean handleDragEnabled) {
		this.handleDragEnabled = handleDragEnabled;
	}

	/**
	 * Returns whether ItemTouchHelper should start a swipe operation if a
	 * pointer is swiped over the View.
	 * <p>
	 * Default value returns false.
	 * </p>
	 *
	 * @return true if ItemTouchHelper should start swiping an item when user
	 * swipes a pointer over the View, false otherwise. Default value is
	 * false.
	 */
	public final boolean isSwipeEnabled() {
		return mItemTouchHelperCallback != null && mItemTouchHelperCallback.isItemViewSwipeEnabled();
	}

	/**
	 * Enable the Swipe of the items
	 * <p>
	 * Default value is false.
	 * </p>
	 *
	 * @param swipeEnabled true to activate, false otherwise
	 */
	public final void setSwipeEnabled(boolean swipeEnabled) {
		initializeItemTouchHelper();
		this.swipeEnabled = swipeEnabled;
		mItemTouchHelperCallback.setSwipeEnabled(swipeEnabled);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean shouldMove(int fromPosition, int toPosition) {
//		 if (mItemMoveListener != null) {
//			 return mItemMoveListener.shouldMoveItem(fromPosition, toPosition);
//		 }
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@CallSuper
	public boolean onItemMove(int fromPosition, int toPosition) {
		// moveItem(fromPosition, toPosition);
		// After the swap, delegate further actions to the user
		if (mItemMoveListener != null) {
			mItemMoveListener.onItemMove(fromPosition, toPosition);
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@CallSuper
	public void onItemSwiped(int position, int direction) {
		// Delegate actions to the user
		if (mItemSwipeListener != null) {
			mItemSwipeListener.onItemSwipe(position, direction);
		}
	}

	private void initializeItemTouchHelper() {
		if (mItemTouchHelper == null) {
			mItemTouchHelperCallback = new ItemTouchHelperCallback(this);
			mItemTouchHelper = new ItemTouchHelper(mItemTouchHelperCallback);
			if (mRecyclerView != null) {
				mItemTouchHelper.attachToRecyclerView(mRecyclerView);
			}
		}
	}

    /*------------------*/
    /* SECTION FEATURES */
    /*------------------*/

	/**
	 * Returns the view sticky headers will be attached to
	 *
	 * @return FrameLayout the view
	 */
	public abstract FrameLayout getStickySectionHeadersHolder();

	public boolean collapseSection(int sectionIndex, boolean fromUser) {
		if (mPositionTranslator.collapseSection(sectionIndex)) {
			final long packedPosition = SectionAdapterHelper.getPackedPositionForSection(sectionIndex);
			final int flatPosition = mPositionTranslator.getFlatPosition(packedPosition);
			final int childCount = mPositionTranslator.getChildCount(sectionIndex);
			notifyItemRangeRemoved(flatPosition + 1, childCount);
			notifyItemChanged(flatPosition);//Davideas: this should not be done all the time, but only when requested
			return true;
		}
		return false;
	}

	public boolean expandSection(int sectionIndex, boolean fromUser) {
		if (mPositionTranslator.expandSection(sectionIndex)) {
			final long packedPosition = SectionAdapterHelper.getPackedPositionForSection(sectionIndex);
			final int flatPosition = mPositionTranslator.getFlatPosition(packedPosition);
			final int childCount = mPositionTranslator.getChildCount(sectionIndex);
			notifyItemRangeInserted(flatPosition + 1, childCount);
			notifyItemChanged(flatPosition);//Davideas: this should not be done all the time, but only when requested
			return true;
		}
		return false;
	}

	public boolean isSectionExpanded(int sectionIndex) {
		return mPositionTranslator.isSectionExpanded(sectionIndex);
	}

	public int getSectionPosition(int flatPosition) {
		int numHeaders = getHeadersCount();
		if (flatPosition >= numHeaders) {
			int adjPosition = flatPosition - numHeaders;
			int adapterCount = getRealItemCount();
			if (adjPosition < adapterCount) {
				flatPosition = adjPosition;
				return getFlatPosition(mPositionTranslator.getExpandablePosition(flatPosition));
			}
		}
		return -1;
	}

	public int getSectionIndex(int flatPosition) {
//        int numHeaders = getHeadersCount();
//        if (flatPosition >= numHeaders) {
//            int adjPosition = flatPosition - numHeaders;
//            int adapterCount = getRealItemCount();
//            if (adjPosition < adapterCount) {
//                flatPosition = adjPosition;
		final long expandablePosition = mPositionTranslator.getExpandablePosition(flatPosition);
		return SectionAdapterHelper.getPackedPositionSection(expandablePosition);
//            }
//        }
//        return -1;
	}

	public int getSectionItemIndex(int flatPosition) {
//        int numHeaders = getHeadersCount();
//        if (flatPosition >= numHeaders) {
//            int adjPosition = flatPosition - numHeaders;
//            int adapterCount = getRealItemCount();
//            if (adjPosition < adapterCount) {
//                flatPosition = adjPosition;
		final long expandablePosition = mPositionTranslator.getExpandablePosition(flatPosition);
		return SectionAdapterHelper.getPackedPositionChild(expandablePosition);
//            }
//        }
//        return -1;
	}

	public int getFlatPosition(long packedPosition) {
		int result = mPositionTranslator.getFlatPosition(packedPosition);
		return result + getHeadersCount();
	}

	public int getFlatPosition(int sectionIndex, int sectionItemIndex) {
		int result = mPositionTranslator.getFlatPosition(SectionAdapterHelper
				.getPackedPositionForChild(sectionIndex, sectionItemIndex));
		return result + getHeadersCount();
	}

	private void rebuildPositionTranslator() {
		if (mPositionTranslator != null) {
			int[] savedState = mPositionTranslator.getSavedStateArray();
			mPositionTranslator.build(mSectionAdapter, true);
			mPositionTranslator.restoreExpandedSectionItems(savedState, null);
		}
	}

	public void notifySectionDataSetChanged() {
		rebuildPositionTranslator();
		super.notifyDataSetChanged();
	}

	public void notifySectionItemRangeChanged(int sectionIndex,
											  int sectionItemIndex, int itemCount) {
		final long packedPosition = SectionAdapterHelper.getPackedPositionForChild(sectionIndex, sectionItemIndex);
		final int flatPosition = mPositionTranslator.getFlatPosition(packedPosition);
		this.notifyItemRangeChanged(flatPosition, itemCount);
	}

	public void notifySectionItemRangeInserted(int sectionIndex,
											   int sectionItemIndex, int itemCount) {
		final long packedPosition = SectionAdapterHelper.getPackedPositionForChild(sectionIndex, sectionItemIndex);
		final int flatPosition = mPositionTranslator.getFlatPosition(packedPosition);
		mPositionTranslator.insertChildItems(sectionIndex, sectionItemIndex, itemCount);
		super.notifyItemRangeInserted(flatPosition, itemCount);
//		mRecyclerView.scrollToPosition(flatPosition);
	}

	public void notifySectionItemRangeRemoved(int sectionIndex,
											  int sectionItemIndex, int itemCount) {
		final long packedPosition = SectionAdapterHelper.getPackedPositionForChild(sectionIndex, sectionItemIndex);
		final int flatPosition = mPositionTranslator.getFlatPosition(packedPosition);

		if (sectionItemIndex == RecyclerView.NO_POSITION) {
			mPositionTranslator.removeSectionItem(sectionIndex);
		} else {
			mPositionTranslator.removeChildItems(sectionIndex, sectionItemIndex, itemCount);
		}

		super.notifyItemRangeRemoved(flatPosition, itemCount);
	}

	public void notifySectionItemMoved(int fromSectionIndex, int fromSectionItemIndex,
									   int toSectionIndex, int toSectionItemIndex) {
		long packedPosition = SectionAdapterHelper.getPackedPositionForChild(fromSectionIndex, fromSectionItemIndex);
		final int fromFlatPosition = mPositionTranslator.getFlatPosition(packedPosition);
		packedPosition = SectionAdapterHelper.getPackedPositionForChild(toSectionIndex, toSectionItemIndex);
		final int toFlatPosition = mPositionTranslator.getFlatPosition(packedPosition);

		mPositionTranslator.moveChildItem(fromSectionIndex, fromSectionItemIndex, toSectionIndex, toSectionItemIndex);
		super.notifyItemMoved(fromFlatPosition, toFlatPosition);
	}

	public void notifySectionItemMoved(int fromPosition, int toPosition, int itemCount) {
		rebuildPositionTranslator();
		if (itemCount != 1) {
			throw new IllegalStateException("itemCount should be always 1  (actual: " + itemCount + ")");
		}
		super.notifyItemMoved(fromPosition, toPosition);
	}

	public LayoutParams getStickySectionHeadersLayoutParams() {
		FrameLayout.LayoutParams newParams = new FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		newParams.gravity = Gravity.TOP | Gravity.LEFT;
		return newParams;
	}

    /*----------------*/
    /* INSTANCE STATE */
    /*----------------*/

	/**
	 * Save the state of the current expanded items.
	 *
	 * @param outState Current state
	 */
	public void onSaveInstanceState(Bundle outState) {
		if (outState != null) {
			super.onSaveInstanceState(outState);
			outState.putBoolean(EXTRA_HEADERS, headersShown);
		}
	}

	/**
	 * Restore the previous state of the expanded items.
	 *
	 * @param savedInstanceState Previous state
	 */
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			super.onRestoreInstanceState(savedInstanceState);
			headersShown = savedInstanceState.getBoolean(EXTRA_HEADERS);
			if (headersShown)
				showAllHeaders();
		}
	}

    /*---------------*/
    /* INNER CLASSES */
    /*---------------*/

	/**
	 * @since 03/01/2016
	 */
	public interface OnUpdateListener {
		/**
		 * Called at startup and every time an item is inserted, removed or
		 * filtered.
		 *
		 * @param size the current number of items in the adapter, result of
		 *             {@link #getItemCount()}
		 */
		void onUpdateEmptyView(int size);
	}

	/**
	 * @since 29/11/2015
	 */
	public interface OnDeleteCompleteListener {
		/**
		 * Called when Undo timeout is over and removal must be committed in the
		 * user Database.
		 * <p>
		 * Due to Java Generic, it's too complicated and not well manageable if
		 * we pass the List&lt;T&gt; object.<br/>
		 * To get deleted items, use {@link #getDeletedItems()} from the
		 * implementation of this method.
		 * </p>
		 */
		void onDeleteConfirmed();
	}

	/**
	 * @since 26/01/2016
	 */
	public interface OnItemClickListener {
		/**
		 * Called when single tap occurs.
		 * <p>
		 * Delegates the click event to the listener and checks if selection
		 * MODE if SINGLE or MULTI is enabled in order to activate the ItemView.
		 * </p>
		 * For Expandable Views it will toggle the Expansion if configured so.
		 *
		 * @param position the adapter position of the item clicked
		 * @return true if the click should activate the ItemView, false for no
		 * change.
		 */
		boolean onItemClick(int position);
	}

	/**
	 * @since 26/01/2016
	 */
	public interface OnItemLongClickListener {
		/**
		 * Called when long tap occurs.
		 * <p>
		 * This method always calls {@link FlexibleViewHolder#toggleActivation}
		 * after listener event is consumed in order to activate the ItemView.
		 * </p>
		 * For Expandable Views it will collapse the View if configured so.
		 *
		 * @param position the adapter position of the item clicked
		 */
		void onItemLongClick(int position);
	}

	/**
	 * @since 26/01/2016
	 */
	public interface OnItemMoveListener {
		/**
		 * Called when the item would like to be swapped.
		 * <p>
		 * Delegate this permission to the developer.
		 * </p>
		 *
		 * @param fromPosition
		 *            the potential start position of the dragged item
		 * @param toPosition
		 *            the potential resolved position of the swapped item
		 * @return return true if the items can swap ({@code onItemMove()} will
		 *         be called), false otherwise (nothing happens)
		 * @see #onItemMove(int, int)
		 */
		// boolean shouldMoveItem(int fromPosition, int toPosition);

		/**
		 * Called when an item has been dragged far enough to trigger a move.
		 * <b>This is called every time an item is shifted</b>, and
		 * <strong>not</strong> at the end of a "drop" event.
		 * <p>
		 * The end of the "drop" event is instead handled by
		 * {@link FlexibleViewHolder#onItemReleased(int)}
		 * </p>
		 * .
		 *
		 * @param fromPosition the start position of the moved item
		 * @param toPosition   the resolved position of the moved item // * @see
		 *                     #shouldMoveItem(int, int)
		 */
		void onItemMove(int fromPosition, int toPosition);
	}

	/**
	 * @since 26/01/2016
	 */
	public interface OnItemSwipeListener {
		/**
		 * Called when swiping ended its animation and Item is not visible
		 * anymore.
		 *
		 * @param position  the position of the item swiped
		 * @param direction the direction to which the ViewHolder is swiped
		 */
		void onItemSwipe(int position, int direction);
	}

	/**
	 * @since 05/03/2016
	 */
	public interface OnStickyHeaderChangeListener {
		/**
		 * Called when the current sticky header changed
		 *
		 * @param sectionIndex the position of header
		 */
		void onStickyHeaderChange(int sectionIndex);
	}

	public class FixedViewInfo {
		/**
		 * The view to add to the list
		 */
		public View view;
		/**
		 * The data backing the view. This is returned from
		 * {@link ListAdapter#getItem(int)}.
		 */
		public Object data;
		/**
		 * <code>true</code> if the fixed view should be selectable in the list
		 */
		public boolean isSelectable;
	}

//	private class HeaderFrameLayout extends FrameLayout {
//
//		private final View shapeView;
//
//		public HeaderFrameLayout(View view) {
//			super(view.getContext());
//			this.shapeView = view;
//		}
//
//		@Override
//		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//			measureChildWithMargins(shapeView, widthMeasureSpec, 0, heightMeasureSpec, 0);
//			setMeasuredDimension(shapeView.getMeasuredWidth(), shapeView.getMeasuredHeight());
//		}
//	}

}