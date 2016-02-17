package eu.davidea.flexibleadapter.items;

import android.support.v7.widget.RecyclerView;

/**
 * Generic implementation of {@link IHeader} interface.
 * <p>By default this item is hidden, not selectable, not sticky.</p>
 * This abstract class extends also {@link AbstractFlexibleItem}.
 *
 * @author Davide Steduto
 * @since 17/01/2016 Created
 */
public abstract class AbstractHeaderItem<VH extends RecyclerView.ViewHolder>
		extends AbstractFlexibleItem<VH>
		implements IHeader<VH> {

	private boolean sticky = false;

	public AbstractHeaderItem() {
		setHidden(true);
		setSelectable(false);
	}

	@Override
	public boolean isSticky() {
		return sticky;
	}

	@Override
	public IFlexible setSticky(boolean sticky) {
		this.sticky = sticky;
		return this;
	}

}