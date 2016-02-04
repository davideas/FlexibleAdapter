package eu.davidea.flexibleadapter.items;

/**
 * Basic interface to manage operations like enabling, selecting, hiding, filtering on items.
 * <p>Implements this interface or use {@link AbstractFlexibleItem}.</p>
 *
 * @author Davide Steduto
 * @since 19/01/2016 Created
 */
public interface IFlexibleItem {

	/*---------*/
	/* METHODS */
	/*---------*/

	/**
	 * Return if the Item is enabled.
	 *
	 * @return (default) true for enabled item, false for disabled one.
	 */
	boolean isEnabled();

	/**
	 * Setter to change enabled behaviour.
	 *
	 * @param enabled false to disable all operations on this item
	 */
	void setEnabled(boolean enabled);

	/**
	 * Return if the Item is hidden.
	 *
	 * @return true for hidden item, (default) false for the shown one.
	 */
	boolean isHidden();

	/**
	 * Setter to change hidden behaviour. Useful while filtering this item.
	 * Default value is false.
	 *
	 * @param hidden true if this item should remain hidden, false otherwise
	 */
	void setHidden(boolean hidden);

	/*--------------------*/
	/* SELECTABLE METHODS */
	/*--------------------*/

	/**
	 * Return if the item can be selected.<br/>
	 *
	 * @return (default) true for a Selectable item, false otherwise
	 */
	boolean isSelectable();

	/**
	 * Setter to change selectable behaviour.
	 *
	 * @param selectable false to disable selection on this item
	 */
	void setSelectable(boolean selectable);

	/*-------------------*/
	/* TOUCHABLE METHODS */
	/*-------------------*/

	boolean isDraggable();

	void setDraggable(boolean draggable);

	boolean isSwipeable();

	void setSwipeable(boolean swipeable);

	/*---------------------*/
	/* VIEW HOLDER METHODS */
	/*---------------------*/

	/**
	 * Returns the type of the Item.<br/>
	 * Should represent an Integer identifier or a resource Id reference {@link android.R.id}.
	 *
	 * @return Integer identifier.
	 */
//	@IdRes
//	int getItemViewType();

	/**
	 * Returns the layout resource Id to bind for the given Item.<br/>
	 * Should identify a resource Layout reference {@link android.R.layout}.
	 *
	 * @return Layout identifier.
	 */
//	@LayoutRes
//	int getLayoutRes();

//	VH getViewHolder(Inflater inflater, ViewGroup parent);

//	void bindViewHolder(VH holder);

}