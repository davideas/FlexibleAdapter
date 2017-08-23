package eu.davidea.samples.flexibleadapter.items;

import android.animation.Animator;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.helpers.AnimatorHelper;
import eu.davidea.flexibleadapter.items.AbstractExpandableHeaderItem;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.samples.flexibleadapter.items.AnimatorExpandableItem.AnimatorExpandableViewHolder;
import eu.davidea.samples.flexibleadapter.services.DatabaseConfiguration;
import eu.davidea.viewholders.ExpandableViewHolder;

/**
 * This Section with header can also be expanded/collapsed.
 * <p>It's important to note that, the ViewHolder must be specified in all &lt;diamond&gt;
 * signature.</p>
 */
public class AnimatorExpandableItem
        extends AbstractExpandableHeaderItem<AnimatorExpandableViewHolder, AnimatorSubItem> {

    private String id;
    private String title;
    private String subtitle;

    public AnimatorExpandableItem(String id) {
        super();
        this.id = id;
        setExpanded(false);//Start collapsed
        setSwipeable(true);
    }

    @Override
    public boolean equals(Object inObject) {
        if (inObject instanceof AnimatorExpandableItem) {
            AnimatorExpandableItem inItem = (AnimatorExpandableItem) inObject;
            return this.id.equals(inItem.id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
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
        return R.layout.recycler_animator_expandable_item;
    }

    @Override
    public AnimatorExpandableViewHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new AnimatorExpandableViewHolder(view, adapter);
    }

    @Override
    public void bindViewHolder(FlexibleAdapter adapter, AnimatorExpandableViewHolder holder, int position, List payloads) {
        if (payloads.size() > 0) {
            Log.d(this.getClass().getSimpleName(), "AnimatorExpandableItem Payload " + payloads);
        } else {
            holder.mTitle.setText(getTitle());
        }
        setSubtitle(String.valueOf(adapter.getCurrentChildren(this).size()) +
                " subItems (" + (isExpanded() ? "expanded" : "collapsed") + ")");
        holder.mSubtitle.setText(getSubtitle());
    }

    /**
     * Provide a reference to the views for each data item.
     * Complex data labels may need more than one view per item, and
     * you provide access to all the views for a data item in a view holder.
     */
    static class AnimatorExpandableViewHolder extends ExpandableViewHolder {

        public TextView mTitle;
        public TextView mSubtitle;

        public AnimatorExpandableViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter, true);//True for sticky
            mTitle = view.findViewById(R.id.title);
            mSubtitle = view.findViewById(R.id.subtitle);

            // Support for StaggeredGridLayoutManager
            setFullSpan(true);
        }

        @Override
        protected boolean shouldNotifyParentOnClick() {
            // Let's notify the item has been expanded / collapsed
            return true;//default=false
        }

        @Override
        protected void expandView(int position) {
            super.expandView(position);
            // Let's notify the item has been expanded. Note: from 5.0.0-rc1 the next line becomes
            // obsolete, override the new method shouldNotifyParentOnClick() as showcased here
            //if (mAdapter.isExpanded(position)) mAdapter.notifyItemChanged(position, true);
        }

        @Override
        protected void collapseView(int position) {
            super.collapseView(position);
            // Let's notify the item has been expanded. Note: from 5.0.0-rc1 the next line becomes
            // obsolete, override the new method shouldNotifyParentOnClick() as showcased here
            //if (!mAdapter.isExpanded(position)) mAdapter.notifyItemChanged(position, true);
        }

        @Override
        public void scrollAnimators(@NonNull List<Animator> animators, int position, boolean isForward) {
            switch (DatabaseConfiguration.scrollAnimatorType) {
                case Scale:
                    AnimatorHelper.scaleAnimator(animators, itemView, 0f);
                    break;
                case SlideInTopBottom:
                    if (isForward) {
                        AnimatorHelper.slideInFromBottomAnimator(animators, itemView, mAdapter.getRecyclerView());
                    } else {
                        AnimatorHelper.slideInFromTopAnimator(animators, itemView, mAdapter.getRecyclerView());
                    }
                    break;
                case SlideInFromTop:
                    AnimatorHelper.slideInFromTopAnimator(animators, itemView, mAdapter.getRecyclerView());
                    break;
                case SlideInFromBottom:
                    AnimatorHelper.slideInFromBottomAnimator(animators, itemView, mAdapter.getRecyclerView());
                    break;
                case SlideInFromLeft:
                    AnimatorHelper.slideInFromLeftAnimator(animators, itemView, mAdapter.getRecyclerView(), 0.5f);
                    break;
                case SlideInFromRight:
                    AnimatorHelper.slideInFromRightAnimator(animators, itemView, mAdapter.getRecyclerView(), 0.5f);
                    break;
                default:
                    AnimatorHelper.alphaAnimator(animators, itemView, 0f);
                    break;
            }
        }
    }

    @Override
    public String toString() {
        return "AnimatorExpandableItem[id=" + id +
                ", title=" + title +
                ", SubItems" + mSubItems + "]";
    }

}