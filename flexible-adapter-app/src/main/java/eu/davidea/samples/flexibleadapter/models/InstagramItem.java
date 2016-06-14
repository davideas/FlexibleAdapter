package eu.davidea.samples.flexibleadapter.models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Random;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractSectionableItem;
import eu.davidea.flipview.FlipView;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.viewholders.FlexibleViewHolder;

public class InstagramItem extends AbstractSectionableItem<InstagramItem.ViewHolder, InstagramHeaderItem> {

	private String id;
	private String url;
	private int quantity;

	public InstagramItem(String id, InstagramHeaderItem header) {
		super(header);
		this.id = id;
		this.header = header;
	}

	@Override
	public boolean equals(Object inObject) {
		if (inObject instanceof InstagramItem) {
			InstagramItem inItem = (InstagramItem) inObject;
			return this.id.equals(inItem.id);
		}
		return false;
	}

	public InstagramItem withImageUrl(String url) {
		this.url = url;
		return this;
	}

	public InstagramItem withName(String name) {
		getHeader().setTitle(name);
		return this;
	}

	public InstagramItem withPlace(String place) {
		getHeader().setSubtitle(place);
		return this;
	}

	public boolean getStarred() {
		return new Random().nextBoolean();
	}

	public int getLikes() {
		return new Random().nextInt(1000);
	}

	@Override
	public int getLayoutRes() {
		return R.layout.recycler_instagram_item;
	}

	@Override
	public ViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater, ViewGroup parent) {
		return new ViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
	}

	@Override
	public void bindViewHolder(final FlexibleAdapter adapter, ViewHolder holder, int position, List payloads) {
		Context context = holder.itemView.getContext();

		holder.mQuantityLikes.setText(context.getResources().getString(R.string.likes, getLikes()));
		holder.mImageFavourite.flipSilently(getStarred());

		//Load image via Glide
		Glide.clear(holder.mImage);
		Glide.with(context).load(url).crossFade(500).into(holder.mImage);
	}

	static final class ViewHolder extends FlexibleViewHolder {

		public ImageView mImage;
		public FlipView mImageFavourite;
		public ImageView mImageComment;
		public ImageView mImageShare;
		public TextView mQuantityLikes;

		public ViewHolder(View view, FlexibleAdapter adapter) {
			super(view, adapter);
			this.mImage = (ImageView) view.findViewById(R.id.instagram_image);
			this.mImageFavourite = (FlipView) view.findViewById(R.id.instagram_image_like);
			this.mImageComment = (ImageView) view.findViewById(R.id.instagram_image_comment);
			this.mImageShare = (ImageView) view.findViewById(R.id.instagram_image_share);
			this.mQuantityLikes = (TextView) view.findViewById(R.id.instagram_quantity_likes);
		}

	}

	@Override
	public String toString() {
		return "InstagramItem[" + super.toString() + "]";
	}

}