package eu.davidea.viewholder;

import android.view.View;

import eu.davidea.flexibleadapter.FlexibleExpandableAdapter;


/**
 * ViewHolder for a Expandable Items.<br/>
 * Holds callbacks which can be used to trigger expansion events.
 *
 * @author Davide Steduto
 * @since 16/01/2016
 */
public abstract class ExpandableViewHolder extends FlexibleViewHolder {

	protected final FlexibleExpandableAdapter mAdapter;


	/**
	 * Default constructor.
	 *
	 * @param view The {@link View} being hosted in this ViewHolder
	 * @param adapter Instance of {@link FlexibleExpandableAdapter}
	 * @param listItemClickListener Instance of {@link OnListItemClickListener}
	 */
	public ExpandableViewHolder(View view,
								FlexibleExpandableAdapter adapter,
								OnListItemClickListener listItemClickListener) {

		super(view, adapter, listItemClickListener);
		mAdapter = adapter;
	}

	/**
	 * Allow to set a {@link View.OnClickListener} on the entire ItemView to trigger an expansion
	 * or collapsing event.
	 * <br/><br/>
	 * This method returns always true; Extend with "return false" to Not expand or collapse this
	 * ItemView onClick events.
	 */
	protected boolean isViewExpandableOnClick() {
		return true;
	}

	/**
	 * Allow to collapse child views of this ItemView when {@link View.OnLongClickListener}
	 * event occurs.
	 * <br/><br/>
	 * This method returns always true; Extend with "return false" to not collapse this ItemView
	 * onLongClick events.
	 */
	protected boolean isViewCollapsibleOnLongClick() {
		return true;
	}

	/**
	 * {@link View.OnClickListener} to listen for click events on the entire ItemView.
	 * <br/><br/>
	 * Only registered if {@link #isViewExpandableOnClick()} is true.
	 *
	 * @param view The View that is the trigger for expansion
	 */
	@Override
	public void onClick(View view) {
		if (isViewExpandableOnClick() && !mAdapter.isSelected(getAdapterPosition())) {
			toggleExpansion();
		}
		super.onClick(view);
	}

	@Override
	public boolean onLongClick(View view) {
		if (isViewCollapsibleOnLongClick() && mAdapter.isExpanded(getAdapterPosition())) {
			collapseView(getAdapterPosition());
		}
		return super.onLongClick(view);
	}

	/**
	 * Expand or Collapse based on the current state.
	 */
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
	protected void expandView(int position) {
		mAdapter.expand(position);
	}

	/**
	 * Triggers collapse of the Item.
	 */
	protected void collapseView(int position) {
		mAdapter.collapse(position);
	}

}