package eu.davidea.flexibleadapter.items;

/**
 * Generic implementation of {@link IFlexibleItem} interface with most useful methods to manage
 * selection and view holder methods.
 *
 * @author Davide Steduto
 * @since 20/01/2016 Created
 */
public abstract class AbstractFlexibleItem implements IFlexibleItem {

	/* Flags for the Adapter */
	boolean mEnabled = true,
			mHidden = false,
			mSelectable = true;

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

	/*---------------------*/
	/* VIEW HOLDER METHODS */
	/*---------------------*/

//	@Override
//	@IdRes
//	public abstract int getItemViewType();
//
//	@Override
//	@LayoutRes
//	public abstract int getLayoutRes();
//
//	@Override
//	public abstract VH getViewHolder(Inflater inflater, ViewGroup parent);
//
//	@Override
//	public abstract void bindViewHolder(VH holder);

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

}