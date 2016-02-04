package eu.davidea.examples.models;

import android.app.ExpandableListActivity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.List;

import eu.davidea.examples.flexibleadapter.ExampleAdapter;
import eu.davidea.examples.flexibleadapter.R;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractExpandableItem;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flipview.FlipView;
import eu.davidea.viewholders.ExpandableViewHolder;
import eu.davidea.viewholders.FlexibleViewHolder;

public class Item extends AbstractFlexibleItem<Item.ParentViewHolder>
		implements Serializable {

	private static final long serialVersionUID = -6882745111884490060L;

	private String id;
	private String title;
	private String subtitle;

	@Override
	public boolean equals(Object inObject) {
		if (inObject instanceof Item) {
			Item inItem = (Item) inObject;
			return this.id.equals(inItem.id);
		}
		return false;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSubtitle() {
		return subtitle;
	}

	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}

	@Override
	public String toString() {
		return "Item[" +
				"id=" + id +
				"title=" + title +
				super.toString() + ']';
	}

	@Override
	public int getLayoutRes() {
		return R.layout.recycler_expandable_row;
	}

	@Override
	public ParentViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
		return new ParentViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
	}

	@Override
	public void bindViewHolder(FlexibleAdapter adapter, ParentViewHolder holder, int position, List payloads) {
		super.bindViewHolder(adapter, holder, position, payloads);

		//ANIMATION EXAMPLE!! ImageView - Handle Flip Animation on Select ALL and Deselect ALL
		if (mSelectAll || mLastItemInActionMode) {
			//Reset the flags with delay
			holder.mFlipView.postDelayed(new Runnable() {
				@Override
				public void run() {
					mSelectAll = mLastItemInActionMode = false;
				}
			}, 200L);
			//Consume the Animation
			holder.mFlipView.flip(adapter.isSelected(position), 200L);
		} else {
			//Display the current flip status
			holder.mFlipView.flipSilently(adapter.isSelected(position));
		}

		//This "if-else" is just an example of what you can do with item animation
		if (adapter.isSelected(position)) {
			adapter.animateView(holder.itemView, position, true);
		} else {
			adapter.animateView(holder.itemView, position, false);
		}

		//In case of searchText matches with Title or with an Item's field
		// this will be highlighted
		if (adapter.hasSearchText()) {
			ExampleAdapter.setHighlightText(holder.itemView.getContext(),
					holder.mTitle, title, adapter.getSearchText());
			ExampleAdapter.setHighlightText(holder.itemView.getContext(),
					holder.mSubtitle, subtitle, adapter.getSearchText());
		} else {
			holder.mTitle.setText(title);
			holder.mSubtitle.setText(subtitle);
		}
	}

	/**
	 * This ViewHolder is expandable and collapsible.
	 */
	public static final class ParentViewHolder extends ExpandableViewHolder {

		FlipView mFlipView;
		TextView mTitle;
		TextView mSubtitle;
		ImageView mHandleView;
		Context mContext;

		public ParentViewHolder(View view, FlexibleAdapter adapter) {
			super(view, adapter);
			this.mContext = view.getContext();
			this.mTitle = (TextView) view.findViewById(R.id.title);
			this.mSubtitle = (TextView) view.findViewById(R.id.subtitle);
			this.mFlipView = (FlipView) view.findViewById(R.id.image);
			this.mFlipView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mAdapter.mItemLongClickListener.onItemLongClick(getAdapterPosition());
					Toast.makeText(mContext, "ImageClick on " + mTitle.getText() + " position " + getAdapterPosition(), Toast.LENGTH_SHORT).show();
					toggleActivation();
				}
			});
			this.mHandleView = (ImageView) view.findViewById(R.id.row_handle);
			setDragHandleView(mHandleView);
		}

		@Override
		public void onClick(View view) {
			Toast.makeText(mContext, "Click on " + mTitle.getText() + " position " + getAdapterPosition(), Toast.LENGTH_SHORT).show();
			super.onClick(view);
		}

		@Override
		public boolean onLongClick(View view) {
			Toast.makeText(mContext, "LongClick on " + mTitle.getText() + " position " + getAdapterPosition(), Toast.LENGTH_SHORT).show();
			return super.onLongClick(view);
		}

		@Override
		protected void toggleActivation() {
			super.toggleActivation();
			//Here we use a custom Animation inside the ItemView
			mFlipView.flip(mAdapter.isSelected(getAdapterPosition()));
		}
	}

}