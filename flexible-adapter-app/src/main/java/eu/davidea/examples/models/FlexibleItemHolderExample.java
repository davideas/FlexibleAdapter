package eu.davidea.examples.models;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import eu.davidea.examples.flexibleadapter.R;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractSectionableItem;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.viewholders.ExpandableViewHolder;

/**
 * In case you need to display the same modelData in multiple RecyclerViews managed by different
 * Adapters, you can implement a derived IFlexible item to HOLD your data model object!
 *
 * <p>In this way you can separate the memory zones of the flags (enabled, expanded, hidden, selectable,
 * draggable, swipeable, etc...) used by an Adapter, to be independent by another Adapter.
 * For instance an item can be Shown and Expanded in a RV, while in the other RV can be Hidden or
 * Not Expanded!</p>
 *
 * Note: This object is not used in the demo.
 */
public class FlexibleItemHolderExample<Model> extends AbstractSectionableItem<FlexibleItemHolderExample.ViewHolder, HeaderItem>
		implements IFilterable {

	/**
	 * Your complex data model object
	 */
	Model modelData;

	public FlexibleItemHolderExample(Model modelData, HeaderItem header) {
		super(header);
		this.modelData = modelData;
	}

	public Model getModelData() {
		return modelData;
	}

	@Override
	public boolean equals(Object o) {
		//TODO FOR YOU: What is equals for you?
		return this == o;//basic implementation
	}

	@Override
	public int getLayoutRes() {
		return R.layout.recycler_expandable_row;
	}

	@Override
	public ViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
		return new ViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
	}

	@Override
	public void bindViewHolder(final FlexibleAdapter adapter, ViewHolder holder, int position, List payloads) {
		//TODO FOR YOU: Bind your VH
	}

	@Override
	public boolean filter(String constraint) {
		//TODO FOR YOU: Customize your filter logic
		//return modelData.getTitle().equals(constraint);
		return true;
	}

	/**
	 * This ViewHolder is expandable and collapsible.
	 */
	public static final class ViewHolder extends ExpandableViewHolder {

		public ViewHolder(View view, FlexibleAdapter adapter) {
			super(view, adapter);
			//TODO FOR YOU: Initialize the Views
		}
	}

}