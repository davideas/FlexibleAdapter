package eu.davidea.flexibleadapter.items;

import android.support.annotation.CallSuper;
import android.support.v7.widget.RecyclerView;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;

/**
 * Generic implementation of {@link IFlexibleItem} interface with most useful methods to manage
 * selection and view holder methods.
 *
 * @author Davide Steduto
 * @since 20/01/2016 Created
 */
public abstract class AbstractFlexibleItem<VH extends RecyclerView.ViewHolder>
		implements IFlexibleItem<VH> {

	/* Item flags recognized by the FlexibleAdapter */
	boolean mEnabled = true, mHidden = false,
			mSelectable = true, mSelected = false,
			mDraggable = false, mSwipeable = false;

	/*---------------*/
	/* BASIC METHODS */
	/*---------------*/

	/**
	 * You <b>must</b> implement this method to compare items Identifiers.
	 * <p>Adapter needs this method to distinguish them and pick up correct items.</p>
	 *
	 * @param o Instance to compare
	 * @return true if items are equals, false otherwise.
	 */
	@Override
	public abstract boolean equals(Object o);

	@Override
	public boolean isEnabled() {
		return mEnabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		mEnabled = enabled;
	}

	@Override
	public boolean isHidden() {
		return mHidden;
	}

	@Override
	public void setHidden(boolean hidden) {
		mHidden = hidden;
	}

	/*--------------------*/
	/* SELECTABLE METHODS */
	/*--------------------*/

	@Override
	public boolean isSelectable() {
		return mSelectable;
	}

	@Override
	public void setSelectable(boolean selectable) {
		this.mSelectable = selectable;
	}

	@Override
	public boolean isSelected() {
		return mSelected;
	}

	@Override
	public void setSelected(boolean selected) {
		this.mSelected = selected;
	}

	/*-------------------*/
	/* TOUCHABLE METHODS */
	/*-------------------*/

	@Override
	public boolean isDraggable() {
		return mDraggable;
	}

	@Override
	public void setDraggable(boolean draggable) {
		mDraggable = draggable;
	}

	@Override
	public boolean isSwipeable() {
		return mSwipeable;
	}

	@Override
	public void setSwipeable(boolean swipeable) {
		mSwipeable = swipeable;
	}

	/*---------------------*/
	/* VIEW HOLDER METHODS */
	/*---------------------*/

	@Override
	@CallSuper
	public void bindViewHolder(FlexibleAdapter adapter, VH holder, int position, List payloads) {
		//When user scrolls, this line binds the correct selection status
		holder.itemView.setActivated(adapter.isSelected(position));
		//TODO: adapter.isSelected(position) or this.isSelected() ??
	}

}