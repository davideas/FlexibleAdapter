package eu.davidea.viewholders;

import android.support.annotation.CallSuper;
import android.view.View;

import eu.davidea.flexibleadapter.FlexibleExpandableAdapter;


/**
 * ViewHolder for a Expandable Items. Holds callbacks which can be used to trigger expansion events.
 * <p/>
 * This class extends {@link FlexibleViewHolder}, which means it will benefit of all implemented
 * methods the lower class holds.
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
	 * Default constructor with no ClickListener or TouchListener.<p>
	 * <b>Note:</b> using this constructor, click events on the entire View will not have any effect.
	 *
	 * @param view    The {@link View} being hosted in this ViewHolder
	 * @param adapter Adapter instance of type {@link FlexibleExpandableAdapter}
	 */
	public ExpandableViewHolder(View view, FlexibleExpandableAdapter adapter) {
		this(view, adapter, null);
	}

	/**
	 * @param view                  The {@link View} being hosted in this ViewHolder
	 * @param adapter               Adapter instance of type {@link FlexibleExpandableAdapter}
	 * @param listItemClickListener ClickListener instance of type {@link OnListItemClickListener}
	 */
	public ExpandableViewHolder(View view, FlexibleExpandableAdapter adapter,
								OnListItemClickListener listItemClickListener) {
		this(view, adapter, listItemClickListener, null);
	}

	/**
	 * @param view                  The {@link View} being hosted in this ViewHolder
	 * @param adapter               Adapter instance of type {@link FlexibleExpandableAdapter}
	 * @param listItemClickListener ClickListener instance of type {@link OnListItemClickListener}
	 * @param listItemTouchListener TouchListener instance of type {@link OnListItemTouchListener}
	 */
	public ExpandableViewHolder(View view, FlexibleExpandableAdapter adapter,
								OnListItemClickListener listItemClickListener,
								OnListItemTouchListener listItemTouchListener) {
		super(view, adapter, listItemClickListener, listItemTouchListener);
		mAdapter = adapter;
	}

	/*--------------*/
	/* MAIN METHODS */
	/*--------------*/

	/**
	 * Allow to set a {@link View.OnClickListener} on the entire ItemView to trigger an expansion
	 * or collapsing event.
	 * <p/>
	 * This method returns always true; Extend with "return false" to Not expand or collapse this
	 * ItemView onClick events.
	 */
	protected boolean isViewExpandableOnClick() {
		return true;
	}

	/**
	 * Allow to collapse child views of this ItemView when {@link View.OnLongClickListener}
	 * event occurs.
	 * <p/>
	 * This method returns always true; Extend with "return false" to not collapse this ItemView
	 * onLongClick events.
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
	 * {@link View.OnClickListener} to listen for click events on the entire ItemView.
	 * <p/>
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

//	@Override
//	public boolean onTouch(View view, MotionEvent event) {
//		//We don't allow Drag of Children if a parent is selected
//		if (!mAdapter.isExpandable(getAdapterPosition()) && !mAdapter.isAnyParentSelected())
//			return super.onTouch(view, event);
//		return true;
//	}

	/**
	 * {@inheritDoc}<p>
	 * <b>Note:</b> In the Expandable version, expanded items are forced to collapse.
	 */
	@Override
	public void onItemTouched(int position, int actionState) {
		if (mAdapter.isExpanded(getAdapterPosition())) {
			collapseView(getAdapterPosition());
		}
		super.onItemTouched(position, actionState);
	}

}