package eu.davidea.samples.flexibleadapter.items;

import android.animation.Animator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.helpers.AnimatorHelper;
import eu.davidea.flexibleadapter.utils.DrawableUtils;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.samples.flexibleadapter.services.DatabaseConfiguration;
import eu.davidea.utils.Utils;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * Item dedicated only for User Learns Selection view (located always at the top in the Adapter).
 * <p>This item is a Scrollable Header.</p>
 */
public class ScrollableULSItem extends AbstractItem<ScrollableULSItem.ULSViewHolder> {

    public ScrollableULSItem(String id) {
        super(id);
    }

    @Override
    public int getSpanSize(int spanCount, int position) {
        return spanCount;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.recycler_scrollable_uls_item;
    }

    @Override
    public ULSViewHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new ULSViewHolder(view, adapter);
    }

    @Override
    public void bindViewHolder(FlexibleAdapter adapter, ULSViewHolder holder, int position, List payloads) {
        Context context = holder.itemView.getContext();
        DrawableUtils.setBackgroundCompat(holder.itemView, DrawableUtils.getRippleDrawable(
                DrawableUtils.getColorDrawable(context.getResources().getColor(R.color.material_color_purple_50)),
                DrawableUtils.getColorControlHighlight(context))
        );
        holder.mImageView.setImageResource(R.drawable.ic_account_circle_white_24dp);
        holder.itemView.setActivated(true);
        holder.mTitle.setSelected(true);//For marquee!!
        holder.mTitle.setText(Utils.fromHtmlCompat(getTitle()));
        holder.mSubtitle.setText(Utils.fromHtmlCompat(getSubtitle()));
    }

    /**
     * Used for UserLearnsSelection.
     */
    class ULSViewHolder extends FlexibleViewHolder {

        ImageView mImageView;
        TextView mTitle;
        TextView mSubtitle;
        ImageView mDismissIcon;

        ULSViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            mTitle = view.findViewById(R.id.title);
            mSubtitle = view.findViewById(R.id.subtitle);
            mImageView = view.findViewById(R.id.image);
            mDismissIcon = view.findViewById(R.id.dismiss_icon);
            mDismissIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DatabaseConfiguration.userLearnedSelection = true;
                    //Don't need anymore to set permanent deletion for Scrollable Headers and Footers
                    //mAdapter.setPermanentDelete(true);
                    //noinspection unchecked
                    mAdapter.removeScrollableHeader(ScrollableULSItem.this);
                    //mAdapter.setPermanentDelete(false);
                }
            });

            // Support for StaggeredGridLayoutManager
            setFullSpan(true);
        }

        @Override
        public void scrollAnimators(@NonNull List<Animator> animators, int position, boolean isForward) {
            AnimatorHelper.slideInFromTopAnimator(animators, itemView, mAdapter.getRecyclerView());
        }
    }

    @Override
    public String toString() {
        return "ScrollableULSItem[" + super.toString() + "]";
    }

}