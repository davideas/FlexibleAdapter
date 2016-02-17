package eu.davidea.flexibleadapter.items;

import android.support.v7.widget.RecyclerView;

/**
 * Abstract class for items that holds a header item.
 *
 * @param <VH>
 * @param <T>
 */
public abstract class AbstractSectionableItem<VH extends RecyclerView.ViewHolder, T extends IHeader>
		extends AbstractFlexibleItem<VH>
		implements ISectionable<VH, T> {

	/**
	 * The header of this item
	 */
	T header;

	@Override
	public T getHeader() {
		return header;
	}

	@Override
	public IFlexible setHeader(T header) {
		this.header = header;
		return this;
	}

}