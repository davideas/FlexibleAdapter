package eu.davidea.examples.models;

import android.content.Context;
import android.util.Log;
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
import eu.davidea.flipview.FlipView;
import eu.davidea.viewholders.ExpandableViewHolder;

public class SimpleItem extends AbstractExampleItem<SimpleItem.ParentViewHolder>
		implements Serializable {

	private static final long serialVersionUID = -6882745111884490060L;

	public SimpleItem(String id) {
		super(id);
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
	public void bindViewHolder(final FlexibleAdapter adapter, ParentViewHolder holder, int position, List payloads) {
		if (payloads.size() > 0){
			Log.i(this.getClass().getSimpleName(), "Payload " + payloads);
			List<AbstractExampleItem> list = (List<AbstractExampleItem>) payloads;
			for (AbstractExampleItem item : list) {
				if (this instanceof ExpandableItem)
					setSubtitle(adapter.getCurrentChildren(this).size() + " subItems");
				if (adapter.hasSearchText()) {
					ExampleAdapter.setHighlightText(holder.itemView.getContext(),
							holder.mSubtitle, getSubtitle(), adapter.getSearchText());
				} else {
					holder.mSubtitle.setText(getSubtitle());
				}
			}

		} else {
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

			//This "if-else" is just an example of what you can do with item animation
			if (adapter.isSelected(position)) {
				adapter.animateView(holder.itemView, position, true);
			} else {
				adapter.animateView(holder.itemView, position, false);
			}

			//In case of searchText matches with Title or with an SimpleItem's field
			// this will be highlighted
			if (this instanceof ExpandableItem)
				setSubtitle(adapter.getCurrentChildren(this).size() + " subItems");
			if (adapter.hasSearchText()) {
				ExampleAdapter.setHighlightText(holder.itemView.getContext(),
						holder.mTitle, getTitle(), adapter.getSearchText());
				ExampleAdapter.setHighlightText(holder.itemView.getContext(),
						holder.mSubtitle, getSubtitle(), adapter.getSearchText());
			} else {
				holder.mTitle.setText(getTitle());
				holder.mSubtitle.setText(getSubtitle());
			}
		}
	}

	/**
	 * This ViewHolder is expandable and collapsible.
	 */
	public static final class ParentViewHolder extends ExpandableViewHolder {

		public FlipView mFlipView;
		public TextView mTitle;
		public TextView mSubtitle;
		public ImageView mHandleView;
		public Context mContext;

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