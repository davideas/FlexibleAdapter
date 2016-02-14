package eu.davidea.flexibleadapter.items;

import android.support.annotation.NonNull;
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
	 * The header of this item
	 */
	T header;

	/**
	 * If the header should be sticky on the top until next header comes and takes its place
	 */
	boolean headerSticky;

	/**
	 * Basic Constructor for Header items.
	 * <p>By default header cannot be selectable.</p>
	 *
	 * @param header       the item to which this header is attached
	 * @param showHeader   display header at the startup
	 * @param headerSticky make header sticky
	 */
	public AbstractSectionableItem(@NonNull T header, boolean showHeader, boolean headerSticky) {
		this.header = header;
		this.headerSticky = headerSticky;
		if (header != null) {
			header.setHidden(!showHeader);
			header.setSelectable(false);
		}
	}

	@Override
	public void setHeaderSticky(boolean headerSticky) {
		this.headerSticky = headerSticky;
	}

	@Override
	public boolean isHeaderSticky() {
		return headerSticky;
	}

	@Override
	public T getHeader() {
		return header;
	}

	@Override
	public void setHeader(T header) {
		this.header = header;
	}

}