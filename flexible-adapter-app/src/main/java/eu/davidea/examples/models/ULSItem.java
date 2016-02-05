package eu.davidea.examples.models;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.List;

import eu.davidea.examples.flexibleadapter.DatabaseService;
import eu.davidea.examples.flexibleadapter.R;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * User Learns Selection
 */
public class ULSItem extends AbstractItem<ULSItem.ExampleViewHolder> implements Serializable {

	private static final long serialVersionUID = -5041296095060813327L;

	@Override
	public boolean equals(Object inObject) {
		if (inObject instanceof ULSItem) {
			ULSItem inItem = (ULSItem) inObject;
			return this.getId().equals(inItem.getId());
		}
		return false;
	}

	@Override
	public int getLayoutRes() {
		return R.layout.recycler_uls_row;
	}

	@Override
	public ExampleViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
		return new ExampleViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
	}

	@Override
	public void bindViewHolder(FlexibleAdapter adapter, ExampleViewHolder holder, int position, List payloads) {
		holder.mImageView.setImageResource(R.drawable.ic_account_circle_white_24dp);
		holder.itemView.setActivated(true);
		holder.mTitle.setSelected(true);//For marquee
		holder.mTitle.setText(Html.fromHtml(getTitle()));
		holder.mSubtitle.setText(Html.fromHtml(getSubtitle()));
		adapter.animateView(holder.itemView, position, false);
	}

	/**
	 * Used for UserLearnsSelection.
	 */
	public static class ExampleViewHolder extends FlexibleViewHolder {

		ImageView mImageView;
		TextView mTitle;
		TextView mSubtitle;
		ImageView mDismissIcon;

		public ExampleViewHolder(View view, final FlexibleAdapter adapter) {
			super(view, adapter);
			mTitle = (TextView) view.findViewById(R.id.title);
			mSubtitle = (TextView) view.findViewById(R.id.subtitle);
			mImageView = (ImageView) view.findViewById(R.id.image);
			mDismissIcon = (ImageView) view.findViewById(R.id.dismiss_icon);
			mDismissIcon.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					//TODO FOR YOU: Save the boolean into Settings!
					DatabaseService.userLearnedSelection = true;
					adapter.removeItem(0);
				}
			});
		}
	}

}