package eu.davidea.examples.flexibleadapter.models;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.examples.flexibleadapter.R;
import eu.davidea.examples.flexibleadapter.models.ExpandableLevel1Item.L1ViewHolder;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.IExpandable;
import eu.davidea.flipview.FlipView;
import eu.davidea.viewholders.ExpandableViewHolder;

/**
 * This is an experiment to evaluate how a Section with header can also be expanded/collapsed.
 * <p>Here, it still benefits of the common fields declared in AbstractModelItem.</p>
 * It's important to note that, the ViewHolder must be specified in all &lt;diamond&gt; signature.
 */
public class ExpandableLevel1Item
		extends AbstractModelItem<L1ViewHolder>
		implements IExpandable<L1ViewHolder, SubItem> {

	private static final long serialVersionUID = -1882711111814491060L;

	/* Flags for FlexibleAdapter */
	private boolean mExpanded = false;

	/* subItems list */
	private List<SubItem> mSubItems;


	public ExpandableLevel1Item(String id) {
		super(id);
	}

	@Override
	public boolean isExpanded() {
		return mExpanded;
	}

	@Override
	public void setExpanded(boolean expanded) {
		mExpanded = expanded;
	}

	@Override
	public int getExpansionLevel() {
		return 1;
	}//This allows +1 level of expansion

	@Override
	public List<SubItem> getSubItems() {
		return mSubItems;
	}

	public final boolean hasSubItems() {
		return mSubItems!= null && mSubItems.size() > 0;
	}

	public boolean removeSubItem(SubItem item) {
		return item != null && mSubItems.remove(item);
	}

	public boolean removeSubItem(int position) {
		if (mSubItems != null && position >= 0 && position < mSubItems.size()) {
			mSubItems.remove(position);
			return true;
		}
		return false;
	}

	public void addSubItem(SubItem subItem) {
		if (mSubItems == null)
			mSubItems = new ArrayList<SubItem>();
		mSubItems.add(subItem);
	}

	public void addSubItem(int position, SubItem subItem) {
		if (mSubItems != null && position >= 0 && position < mSubItems.size()) {
			mSubItems.add(position, subItem);
		} else
			addSubItem(subItem);
	}

	@Override
	public int getLayoutRes() {
		return R.layout.recycler_expandable_row;
	}

	@Override
	public L1ViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
		return new L1ViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
	}

	@Override
	public void bindViewHolder(final FlexibleAdapter adapter, L1ViewHolder holder, int position, List payloads) {
		if (payloads.size() > 0) {
			Log.i(this.getClass().getSimpleName(), "ExpandableHeaderItem Payload " + payloads);
		} else {
			holder.mTitle.setText(getTitle());
		}
		setSubtitle(adapter.getCurrentChildren(this).size() + " subItems");
		holder.mSubtitle.setText(getSubtitle());

		//ANIMATION EXAMPLE!! ImageView - Handle Flip Animation on Select ALL and Deselect ALL
		if (adapter.isSelectAll() || adapter.isLastItemInActionMode()) {
			//Reset the flags with delay
			holder.itemView.postDelayed(new Runnable() {
				@Override
				public void run() {
					adapter.resetActionModeFlags();
				}
			}, 200L);
			//Consume the Animation
			holder.mFlipView.flip(adapter.isSelected(position), 200L);
		} else {
			//Display the current flip status
			holder.mFlipView.flipSilently(adapter.isSelected(position));
		}

		//This is just an example of what you can do with item animation
		adapter.animateView(holder.itemView, position, adapter.isSelected(position));
	}

	/**
	 * Provide a reference to the views for each data item.
	 * Complex data labels may need more than one view per item, and
	 * you provide access to all the views for a data item in a view holder.
	 */
	static class L1ViewHolder extends ExpandableViewHolder {

		public TextView mTitle;
		public TextView mSubtitle;
		public FlipView mFlipView;
		public Context mContext;

		public L1ViewHolder(View view, FlexibleAdapter adapter) {
			super(view, adapter);
			this.mContext = view.getContext();
			mTitle = (TextView) view.findViewById(R.id.title);
			mSubtitle = (TextView) view.findViewById(R.id.subtitle);
			this.mFlipView = (FlipView) view.findViewById(R.id.image);
			this.mFlipView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mAdapter.mItemLongClickListener.onItemLongClick(getAdapterPosition());
					Toast.makeText(mContext, "ImageClick on " + mTitle.getText() + " position " + getAdapterPosition(), Toast.LENGTH_SHORT).show();
					toggleActivation();
				}
			});

			View handleView = view.findViewById(R.id.row_handle);
			if (handleView != null)
				handleView.setVisibility(View.GONE);
		}

		@Override
		protected void toggleActivation() {
			super.toggleActivation();
			//Here we use a custom Animation inside the ItemView
			mFlipView.flip(mAdapter.isSelected(getAdapterPosition()));
		}

		@Override
		protected boolean isViewExpandableOnClick() {
			return true;
		}
	}

	@Override
	public String toString() {
		return "ExpandableLevel-1[" + super.toString() + "//SubItems" + mSubItems + "]";
	}

}