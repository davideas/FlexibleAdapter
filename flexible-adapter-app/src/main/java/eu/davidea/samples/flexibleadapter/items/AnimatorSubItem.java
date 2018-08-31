package eu.davidea.samples.flexibleadapter.items;

import android.animation.Animator;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.helpers.AnimatorHelper;
import eu.davidea.flexibleadapter.items.AbstractSectionableItem;
import eu.davidea.samples.flexibleadapter.R;
import eu.davidea.samples.flexibleadapter.services.DatabaseConfiguration;
import eu.davidea.utils.Utils;
import eu.davidea.viewholders.AnimatedViewHolder;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * If you don't have many fields in common better to extend directly from
 * {@link eu.davidea.flexibleadapter.items.AbstractSectionableItem} to benefit of the already
 * implemented methods (getter and setters).
 */
public class AnimatorSubItem extends AbstractSectionableItem<AnimatorSubItem.ChildViewHolder, AnimatorExpandableItem> {

    private String id;
    private String title;

    public AnimatorSubItem(String id, AnimatorExpandableItem header) {
        super(header);
        this.id = id;
        setSwipeable(true);
    }

    @Override
    public boolean equals(Object inObject) {
        if (inObject instanceof AnimatorSubItem) {
            AnimatorSubItem inItem = (AnimatorSubItem) inObject;
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

    @Override
    public int getLayoutRes() {
        return R.layout.recycler_animator_sub_item;
    }

    @Override
    public ChildViewHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new ChildViewHolder(view, adapter);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void bindViewHolder(FlexibleAdapter adapter, ChildViewHolder holder, int position, List payloads) {
        holder.mTitle.setText(getTitle());
    }

    /**
     * Provide a reference to the views for each data item.
     * Complex data labels may need more than one view per item, and
     * you provide access to all the views for a data item in a view holder.
     */
    static final class ChildViewHolder extends FlexibleViewHolder implements AnimatedViewHolder {

        public TextView mTitle;

        ChildViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            this.mTitle = view.findViewById(R.id.title);
        }

        @Override
        public float getActivationElevation() {
            return Utils.dpToPx(itemView.getContext(), 4f);
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

        @Override
        public boolean preAnimateAddImpl() {
            if (DatabaseConfiguration.subItemSpecificAnimation) {
                itemView.setRotationX(90);
                return true;
            }
            return false;
        }

        @Override
        public boolean preAnimateRemoveImpl() {
            return false;
        }

        @Override
        public boolean animateAddImpl(ViewPropertyAnimatorListener listener, long addDuration, int index) {
            if (DatabaseConfiguration.subItemSpecificAnimation) {
                ViewCompat.animate(itemView)
                          .rotationX(0)
                          .setDuration(addDuration)
                          .setInterpolator(new DecelerateInterpolator())
                          .setListener(listener)
                          .setStartDelay(index * 150L)
                          .start();
                return true;
            }
            return false;
        }

        @Override
        public boolean animateRemoveImpl(ViewPropertyAnimatorListener listener, long removeDuration, int index) {
            if (DatabaseConfiguration.subItemSpecificAnimation) {
                ViewCompat.animate(itemView)
                          .rotationX(90)
                          .setDuration(removeDuration)
                          .setInterpolator(new DecelerateInterpolator())
                          .setListener(listener)
                          .setStartDelay(index * 40L)
                          .start();
                return true;
            }
            return false;
        }
    }

    @Override
    public String toString() {
        return "SubItem[" + super.toString() + "]";
    }

}