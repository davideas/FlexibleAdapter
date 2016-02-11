package eu.davidea.flexibleadapter.items;

import android.support.v7.widget.RecyclerView;

/**
 * Abstract class for an Header or Section. Holds the reference to the item to which it is attached.
 * <p>An Header or Section cannot be selected.</p>
 *
 *
 * @param <VH>
 * @param <T>
 */
public abstract class AbstractSectionableItem<VH extends RecyclerView.ViewHolder, T extends IFlexibleItem>
			extends AbstractFlexibleItem<VH>
			implements ISectionable<VH, T> {

	/**
	 * TODO: Header position
	 */
	int headerPosition;
	/**
	 * TODO: The position of the first item which the header/section will represent
	 */
	int firstPosition;
	/**
	 * The item to which this header is attached
	 */
	T attachedItem;
	/**
	 * If this header should be sticky on the top until next header comes and takes its place
	 */
	boolean sticky;

	/**
	 * Basic Constructor for Header items.
	 * <p>By default header cannot be selectable.</p>
	 *
	 * @param attachedItem   the item to which this header is attached
	 * @param shown          display header at the startup
	 * @param sticky         make header sticky
	 */
	public AbstractSectionableItem(T attachedItem, boolean shown, boolean sticky) {
		this.attachedItem = attachedItem;
		this.sticky = sticky;
		setHidden(!shown);
	}

	@Override
	public final boolean isSelectable() {
		return false;
	}

	@Override
	public void setSticky(boolean sticky) {
		this.sticky = sticky;
	}

	@Override
	public boolean isSticky() {
		return sticky;
	}

	@Override
	public T getAttachedItem() {
		return attachedItem;
	}

}