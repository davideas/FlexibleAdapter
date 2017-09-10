package eu.davidea.samples.flexibleadapter.items;

import android.content.Context;
import android.view.View;
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

    public String getLikes() {
        return Integer.toString(new Random().nextInt(1000));
    }

    @Override
    public int getLayoutRes() {
        return R.layout.recycler_instagram_item;
    }

    @Override
    public ViewHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new ViewHolder(view, adapter);
    }

    @Override
    public void bindViewHolder(final FlexibleAdapter adapter, ViewHolder holder, int position, List payloads) {
        Context context = holder.itemView.getContext();

        holder.mQuantityLikes.setText(context.getResources().getString(R.string.likes, getLikes()));
        holder.mImageFavourite.flipSilently(getStarred());

        // Load image via Glide
        Glide.clear(holder.mImage);
        Glide.with(context).load(url).crossFade(500).into(holder.mImage);
    }

    static final class ViewHolder extends FlexibleViewHolder {

        ImageView mImage;
        FlipView mImageFavourite;
        ImageView mImageComment;
        ImageView mImageShare;
        TextView mQuantityLikes;

        public ViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            this.mImage = view.findViewById(R.id.instagram_image);
            this.mImageFavourite = view.findViewById(R.id.instagram_image_like);
            this.mImageComment = view.findViewById(R.id.instagram_image_comment);
            this.mImageShare = view.findViewById(R.id.instagram_image_share);
            this.mQuantityLikes = view.findViewById(R.id.instagram_quantity_likes);
        }
    }

    @Override
    public String toString() {
        return "InstagramItem[" + super.toString() + "]";
    }

}