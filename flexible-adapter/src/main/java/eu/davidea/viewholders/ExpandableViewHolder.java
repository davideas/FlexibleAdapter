package eu.davidea.viewholders;

import android.support.annotation.CallSuper;
import android.view.View;

import eu.davidea.flexibleadapter.FlexibleExpandableAdapter;


/**
 * ViewHolder for a Expandable Items. Holds callbacks which can be used to trigger expansion events.
 * <p>This class extends {@link FlexibleViewHolder}, which means it will benefit of all implemented
 * methods the lower class holds.</p>
 *
 * @author Davide Steduto
 * @since 16/01/2016 Created
 */
public abstract class ExpandableViewHolder extends FlexibleViewHolder {

	protected final FlexibleExpandableAdapter mAdapter;

	/*--------------*/
	/* CONSTRUCTORS */
	/*--------------*/

	/**
	 * Default constructor with no ClickListener or TouchListener.
	 * <p><b>Note:</b> using this constructor, click events on the entire View will not have
	 * any effect.</p>
	 *
	 * @param view    The {@link View} being hosted in this ViewHolder
	 * @param adapter Adapter instance of type {@link FlexibleExpandableAdapter}
	 */
	public ExpandableViewHolder(View view, FlexibleExpandableAdapter adapter) {
		super(view, adapter);
		mAdapter = adapter;
	}

	/*--------------*/
	/* MAIN METHODS */
	/*--------------*/

	/**
	 * Allow to expand or collapse child views of this ItemView when {@link View.OnClickListener}
	 * event occurs on the entire view.
	 * <p>This method returns always true; Extend with "return false" to Not expand or collapse
	 * this ItemView onClick events.</p>
	 */
	protected boolean isViewExpandableOnClick() {
		return true;
	}

	/**
	 * Allow to collapse child views of this ItemView when {@link View.OnLongClickListener}
	 * event occurs on the entire view.
	 * <p>This method returns always true; Extend with "return false" to Not collapse this
	 * ItemView onLongClick events.</p>
	 */
	protected boolean isViewCollapsibleOnLongClick() {
		return true;
	}

	/**
	 * Expand or Collapse based on the current state.
	 */
	@CallSuper
	protected void toggleExpansion() {
		int position = getAdapterPosition();
		if (mAdapter.isExpanded(position)) {
			collapseView(position);
		} else if (!mAdapter.isSelected(position)) {
			expandView(position);
		}
	}

	/**
	 * Triggers expansion of the Item.
	 */
	@CallSuper
	protected void expandView(int position) {
		mAdapter.expand(position);
	}

	/**
	 * Triggers collapse of the Item.
	 */
	@CallSuper
	protected void collapseView(int position) {
		mAdapter.collapse(position);
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
		if (isViewCollapsibleOnLongClick()) {
			collapseView(getAdapterPosition());
		}
		return super.onLongClick(view);
	}

//	@Override
//	public boolean onTouch(View view, MotionEvent event) {
//		//We don't allow Drag of Children if a parent is selected
//		if (!mAdapter.isExpandable(getAdapterPosition()) && !mAdapter.isAnyParentSelected())
//			return super.onTouch(view, event);
//		return true;
//	}

	/**
	 * {@inheritDoc}
	 * <p><b>Note:</b> In the Expandable version, expanded items are forced to collapse.</p>
	 */
	@Override
	public void onActionStateChanged(int position, int actionState) {
		if (mAdapter.isExpanded(getAdapterPosition())) {
			collapseView(position);
		}
		super.onActionStateChanged(position, actionState);
	}

}