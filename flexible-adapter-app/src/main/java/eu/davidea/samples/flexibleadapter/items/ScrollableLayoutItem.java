package eu.davidea.samples.flexibleadapter.items;

import android.animation.Animator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.helpers.AnimatorHelper;
import eu.davidea.flexibleadapter.utils.DrawableUtils;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.utils.Utils;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * Item dedicated to display which Layout is currently displayed.
 * <p>This item is a Scrollable Header.</p>
 */
public class ScrollableLayoutItem extends AbstractItem<ScrollableLayoutItem.LayoutViewHolder> {

    public ScrollableLayoutItem(String id) {
        super(id);
    }

    @Override
    public int getSpanSize(int spanCount, int position) {
        return spanCount;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.recycler_scrollable_layout_item;
    }

    @Override
    public LayoutViewHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new LayoutViewHolder(view, adapter);
    }

    @Override
    public void bindViewHolder(FlexibleAdapter adapter, LayoutViewHolder holder, int position, List payloads) {
        Context context = holder.itemView.getContext();
        DrawableUtils.setBackgroundCompat(holder.itemView, DrawableUtils.getRippleDrawable(
                DrawableUtils.getColorDrawable(context.getResources().getColor(R.color.material_color_amber_50)),
                DrawableUtils.getColorControlHighlight(context))
        );
        holder.mTitle.setText(Utils.fromHtmlCompat(getTitle()));
        holder.mSubtitle.setText(Utils.fromHtmlCompat(getSubtitle()));
    }

    public static class LayoutViewHolder extends FlexibleViewHolder {

        public TextView mTitle;
        public TextView mSubtitle;

        public LayoutViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter, true);
            mTitle = view.findViewById(R.id.title);
            mSubtitle = view.findViewById(R.id.subtitle);

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
        return "ScrollableLayoutItem[" + super.toString() + "]";
    }
}