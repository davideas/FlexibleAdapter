package eu.davidea.samples.anim;

import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;

public class FlipDownItemAnimator extends PendingItemAnimator {

    public FlipDownItemAnimator() {
        setAddDuration(1000);
        setRemoveDuration(500);
    }

    @Override
    protected boolean prepHolderForAnimateRemove(ViewHolder holder) {
        return true;
    }

    @Override
    protected ViewPropertyAnimatorCompat animateRemoveImpl(ViewHolder holder) {
        return ViewCompat.animate(holder.itemView)
                         .rotationY(90)
                         .translationX(-(holder.itemView.getMeasuredWidth() / 4))
                         .scaleX(0.5F)
                         .scaleY(0.5F)
                         .setInterpolator(new AccelerateInterpolator());
    }

    @Override
    protected void onRemoveCanceled(ViewHolder holder) {
        ViewCompat.setRotationY(holder.itemView, 0);
        ViewCompat.setTranslationX(holder.itemView, 0);
        ViewCompat.setScaleX(holder.itemView, 1);
        ViewCompat.setScaleY(holder.itemView, 1);
    }

    @Override
    protected boolean prepHolderForAnimateAdd(ViewHolder holder) {
        ViewCompat.setTranslationX(holder.itemView, -(holder.itemView.getMeasuredWidth() / 2));
        ViewCompat.setRotationY(holder.itemView, -90);
        return true;
    }

    @Override
    protected ViewPropertyAnimatorCompat animateAddImpl(ViewHolder holder) {
        return ViewCompat.animate(holder.itemView)
                         .rotationY(0)
                         .translationX(0)
                         .setInterpolator(new BounceInterpolator());
    }

    @Override
    protected void onAddCanceled(ViewHolder holder) {
        ViewCompat.setRotationY(holder.itemView, 0);
        ViewCompat.setTranslationX(holder.itemView, 0);
    }
}
