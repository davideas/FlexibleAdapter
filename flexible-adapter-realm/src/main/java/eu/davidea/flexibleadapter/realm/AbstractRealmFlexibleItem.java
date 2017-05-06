package eu.davidea.flexibleadapter.realm;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.IFlexible;
import io.realm.RealmObject;

/**
 * This object can't be used right now:
 * TODO: #761 - Inheritance / Polymorphism - https://github.com/realm/realm-java/issues/761
 *
 * @author Davide
 * @since 06/05/2017
 */
public abstract class AbstractRealmFlexibleItem<VH extends RecyclerView.ViewHolder>
		extends RealmObject
		implements IFlexible<VH> {

	/* Item flags recognized by FlexibleAdapter */
	protected boolean mEnabled = true, mHidden = false,
			mSelectable = true, mDraggable = false, mSwipeable = false;

	/*---------------*/
	/* BASIC METHODS */
	/*---------------*/

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

	@Override
	public int getSpanSize(int spanCount, int position) {
		return 1;
	}

	@Override
	public boolean shouldNotifyChange(IFlexible newItem) {
		return true;
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public abstract int getLayoutRes();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public abstract VH createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public abstract  void bindViewHolder(FlexibleAdapter adapter, VH holder, int position, List payloads);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void unbindViewHolder(FlexibleAdapter adapter, VH holder, int position) {

	}

}