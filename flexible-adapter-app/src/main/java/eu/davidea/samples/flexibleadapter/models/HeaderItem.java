package eu.davidea.samples.flexibleadapter.models;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractHeaderItem;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.flexibleadapter.items.ISectionable;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * This is a simple item with custom layout for headers.
 * <p>A Section should not contain others Sections!</p>
 * Headers are not Sectionable!
 */
public class HeaderItem extends AbstractHeaderItem<HeaderItem.HeaderViewHolder> implements IFilterable {

	private static final long serialVersionUID = -7408637077727563374L;

	private String id;
	private String title;
	private String subtitle;

	public HeaderItem(String id) {
		super();
		this.id = id;
		setDraggable(true);
	}

	@Override
	public boolean equals(Object inObject) {
		if (inObject instanceof HeaderItem) {
			HeaderItem inItem = (HeaderItem) inObject;
			return this.getId().equals(inItem.getId());
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
	public int getLayoutRes() {
		return R.layout.recycler_header_item;
	}

	@Override
	public HeaderViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
		return new HeaderViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void bindViewHolder(FlexibleAdapter adapter, HeaderViewHolder holder, int position, List payloads) {
		if (payloads.size() > 0) {
			Log.i(this.getClass().getSimpleName(), "HeaderItem Payload " + payloads);
		} else {
			holder.mTitle.setText(getTitle());
		}
		List<ISectionable> sectionableList = adapter.getSectionItems(this);
		String subTitle = (sectionableList.isEmpty() ? "Empty section" :
				sectionableList.size() + " section items");
		holder.mSubtitle.setText(subTitle);
	}

	@Override
	public boolean filter(String constraint) {
		return getTitle() != null && getTitle().toLowerCase().trim().contains(constraint);
	}

	static class HeaderViewHolder extends FlexibleViewHolder {

		public TextView mTitle;
		public TextView mSubtitle;

		public HeaderViewHolder(View view, FlexibleAdapter adapter) {
			super(view, adapter);
			mTitle = (TextView) view.findViewById(R.id.title);
			mSubtitle = (TextView) view.findViewById(R.id.subtitle);
			mTitle.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.d("HeaderTitle", "Registered internal click on Header TitleTextView! " + mTitle.getText() + " position=" + getFlexibleAdapterPosition());
				}
			});
		}
	}

	@Override
	public String toString() {
		return "HeaderItem[id=" + id +
				", title=" + title + "]";
	}

}