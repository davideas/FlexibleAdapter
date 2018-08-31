package eu.davidea.samples.flexibleadapter.items;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractHeaderItem;
import eu.davidea.flipview.FlipView;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.viewholders.FlexibleViewHolder;

public class InstagramHeaderItem extends AbstractHeaderItem<InstagramHeaderItem.HeaderViewHolder> {

    private String id;
    private String title;
    private String subtitle;

    public InstagramHeaderItem(String id) {
        super();
        this.id = id;
    }

    @Override
    public boolean equals(Object inObject) {
        if (inObject instanceof InstagramHeaderItem) {
            InstagramHeaderItem inItem = (InstagramHeaderItem) inObject;
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
        return R.layout.recycler_instagram_header_item;
    }

    @Override
    public HeaderViewHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new HeaderViewHolder(view, adapter);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void bindViewHolder(FlexibleAdapter adapter, HeaderViewHolder holder, int position, List payloads) {
        if (payloads.size() > 0) {
            Log.d(this.getClass().getSimpleName(), "InstagramHeaderItem Payload " + payloads);
        } else {
            holder.mTitle.setText(getTitle());
        }
        holder.mSubtitle.setText(getSubtitle());
    }

    static class HeaderViewHolder extends FlexibleViewHolder {

        FlipView mAccountImage;
        TextView mTitle;
        TextView mSubtitle;

        public HeaderViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter, true);//True for sticky
            mAccountImage = view.findViewById(R.id.instagram_account_image);
            mAccountImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("InstagramHeaderItem", "Registered internal click on Header AccountImageView!" + " position=" + getFlexibleAdapterPosition());
                }
            });
            mTitle = view.findViewById(R.id.instagram_account_title);
            mSubtitle = view.findViewById(R.id.instagram_place_subtitle);
            mSubtitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("InstagramHeaderItem", "Registered internal click on Header SubTitleTextView! " + mSubtitle.getText() + " position=" + getFlexibleAdapterPosition());
                }
            });
        }
    }

    @Override
    public String toString() {
        return "InstagramHeaderItem[id=" + id +
                ", title=" + title + "]";
    }

}