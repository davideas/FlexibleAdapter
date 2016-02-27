/*
 * Copyright 2016 Davide Steduto
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
package eu.davidea.viewholders;

import android.support.annotation.CallSuper;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;

import eu.davidea.flexibleadapter.FlexibleAdapter;


/**
 * ViewHolder for a Expandable Items. Holds callbacks which can be used to trigger expansion events.
 * <p>This class extends {@link FlexibleViewHolder}, which means it will benefit of all implemented
 * methods the lower class holds.</p>
 *
 * @author Davide Steduto
 * @since 16/01/2016 Created
 */
public abstract class ExpandableViewHolder extends FlexibleViewHolder {

	protected final FlexibleAdapter mAdapter;
//	private boolean mLongPressEnabled = false;

	/*--------------*/
	/* CONSTRUCTORS */
	/*--------------*/

	/**
	 * Default constructor with no ClickListener or TouchListener.
	 * <p><b>Note:</b> using this constructor, click events on the entire View will not have
	 * any effect.</p>
	 *
	 * @param view    The {@link View} being hosted in this ViewHolder
	 * @param adapter Adapter instance of type {@link FlexibleAdapter}
	 */
	public ExpandableViewHolder(View view, FlexibleAdapter adapter) {
		super(view, adapter);
		mAdapter = adapter;
	}

	/*--------------*/
	/* MAIN METHODS */
	/*--------------*/

	/**
	 * Allows to expand or collapse child views of this ItemView when {@link OnClickListener}
	 * event occurs on the entire view.
	 * <p>This method returns always true; Extend with "return false" to Not expand or collapse
	 * this ItemView onClick events.</p>
	 *
	 * @return always true, if not overridden
	 */
	protected boolean isViewExpandableOnClick() {
		return true;
	}

	/**
	 * Allows to collapse child views of this ItemView when {@link OnLongClickListener}
	 * event occurs on the entire view.
	 * <p>This method returns always true; Extend with "return false" to Not collapse this
	 * ItemView onLongClick events.</p>
	 *
	 * @return always true, if not overridden
	 */
	protected boolean isViewCollapsibleOnLongClick() {
		return true;
	}

	/**
	 * Expands or Collapses based on the current state.
	 */
	@CallSuper
	protected void toggleExpansion() {
		int position = getAdapterPosition();
//		if (mAdapter.isExpanded(position)) {
//			collapseView(position);
//		} else if (!mAdapter.isSelected(position)) {
//			expandView(position);
//		}
	}

	/**
	 * Triggers expansion of the Item.
	 */
	@CallSuper
	protected void expandView(int position) {
//		mAdapter.expand(position);
	}

	/**
	 * Triggers collapse of the Item.
	 */
	@CallSuper
	protected void collapseView(int position) {
//		mAdapter.collapse(position);
	}

	/*---------------------------------*/
	/* CUSTOM LISTENERS IMPLEMENTATION */
	/*---------------------------------*/

	/**
	 * Called when user taps once on the ItemView.
	 * <p><b>Note:</b> In Expandable version, it tries to expand, but before,
	 * it checks if the view {@link #isViewExpandableOnClick()}.</p>
	 *
	 * @param view the View that is the trigger for expansion
	 */
	@Override
	public void onClick(View view) {
		if (isViewExpandableOnClick()) {
			toggleExpansion();
		}
		super.onClick(view);
	}

	/**
	 * Called when user long taps on the ItemView.
	 * <p><b>Note:</b> In Expandable version, it tries to collapse, but before,
	 * it checks if the view {@link #isViewCollapsibleOnLongClick()}.</p>
	 *
	 * @param view the View that is the trigger for collapsing
	 */
	@Override
	public boolean onLongClick(View view) {
		int position = getAdapterPosition();
		if (isViewCollapsibleOnLongClick()) {
			collapseView(position);
		}
		return super.onLongClick(view);
	}

//	@Override
//	public boolean onTouch(View view, MotionEvent event) {
//		//We don't allow Drag of Children if a parent is selected
//		boolean expandable = mAdapter.isExpandable(getAdapterPosition());
//		if (expandable && !mAdapter.isAnyChildSelected() || !expandable && !mAdapter.isAnyParentSelected())
//			return super.onTouch(view, event);
//		return false;
//	}

	/**
	 * {@inheritDoc}
	 * <p><b>Note:</b> In the Expandable version, expanded items are forced to collapse.</p>
	 */
	@Override
	public void onActionStateChanged(int position, int actionState) {
//		if (mAdapter.isExpanded(getAdapterPosition())) {
//			collapseView(position);
//		}
		super.onActionStateChanged(position, actionState);
	}

}